package org.springframework.cloud.gateway.route;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public class AwesomeRouteLocator implements Ordered, AwesomeRoutes, ApplicationListener<RefreshRoutesEvent>, ApplicationEventPublisherAware {

	private static final Log log = LogFactory.getLog(AwesomeRouteLocator.class);

	private final RouteLocator delegate;

	private final IndexedCollection<WrapRoute> collection =  new ConcurrentIndexedCollection<WrapRoute>();
	private ApplicationEventPublisher applicationEventPublisher;

	public AwesomeRouteLocator(RouteLocator delegate) {
		this.delegate = delegate;
		if(collection.isEmpty()) {
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
			fetch().collect(Collectors.toList()).subscribe(
					list -> Flux.fromIterable(list).materialize().collect(Collectors.toList()).subscribe(signals -> {
						applicationEventPublisher.publishEvent(new RefreshRoutesResultEvent(this));
						//TODO init QCengine
					}, this::handleRefreshError), this::handleRefreshError);
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
