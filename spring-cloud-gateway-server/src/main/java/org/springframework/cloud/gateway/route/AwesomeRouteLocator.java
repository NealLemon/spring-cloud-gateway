/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.route;

import java.util.stream.Collectors;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.standingquery.StandingQueryIndex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

public class AwesomeRouteLocator
		implements Ordered, AwesomeRoutes, ApplicationListener<RefreshRoutesEvent>, ApplicationEventPublisherAware {

	private static final Log log = LogFactory.getLog(AwesomeRouteLocator.class);

	private final RouteLocator delegate;

	private final IndexedCollection<WrapRoute> collection = new ConcurrentIndexedCollection<WrapRoute>();

	private ApplicationEventPublisher applicationEventPublisher;

	public AwesomeRouteLocator(RouteLocator delegate) {
		this.delegate = delegate;
		if (collection.isEmpty()) {
			fetch().doOnNext(route -> {
				WrapRoute wrapRoute = new WrapRoute(route);
				collection.add(wrapRoute);
			}).subscribe();
		}
	}

	@Override
	public Flux<Route> getRoutes() {
		return this.delegate.getRoutes();
	}

	private Flux<Route> fetch() {
		return this.delegate.getRoutes().sort(AnnotationAwareOrderComparator.INSTANCE);
	}

	@Override
	public void onApplicationEvent(RefreshRoutesEvent event) {
		try {
//			fetch().collect(Collectors.toList()).subscribe(
//					list -> Flux.fromIterable(list).materialize().collect(Collectors.toList()).subscribe(signals -> {
//						applicationEventPublisher.publishEvent(new RefreshRoutesResultEvent(this));
//						// TODO init QCengine
//					}, this::handleRefreshError), this::handleRefreshError);
			collection.clear();
			fetch().doOnNext(route -> {
				WrapRoute wrapRoute = new WrapRoute(route);
				collection.add(wrapRoute);
			}).subscribe();
		}
		catch (Throwable e) {
			handleRefreshError(e);
		}
	}

	private void handleRefreshError(Throwable throwable) {
		if (log.isErrorEnabled()) {
			log.error("Refresh routes error !!!", throwable);
		}
		applicationEventPublisher.publishEvent(new RefreshRoutesResultEvent(this, throwable));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public IndexedCollection<WrapRoute> getCollectionRoutes() {
		return collection;
	}

}
