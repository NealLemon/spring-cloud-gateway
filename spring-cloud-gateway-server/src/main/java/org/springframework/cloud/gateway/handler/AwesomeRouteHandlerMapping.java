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

package org.springframework.cloud.gateway.handler;

import java.util.Map;
import java.util.Set;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.filter.FilteringResultSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.route.AwesomeRoutes;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.in;
import static com.googlecode.cqengine.query.QueryFactory.matchesPath;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REACTOR_CONTEXT_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @description: new way to route API
 * @author: Neal
 * @date: 2023/3/9
 **/
public class AwesomeRouteHandlerMapping extends AbstractHandlerMapping {

	private final FilteringWebHandler webHandler;

	private final AwesomeRoutes routeLocator;

	public AwesomeRouteHandlerMapping(FilteringWebHandler webHandler, AwesomeRoutes routeLocator,
			GlobalCorsProperties globalCorsProperties, Environment environment) {
		this.webHandler = webHandler;
		this.routeLocator = routeLocator;
		setOrder(environment.getProperty(GatewayProperties.PREFIX + ".handler-mapping.order", Integer.class, 1));
		setCorsConfigurations(globalCorsProperties.getCorsConfigurations());

	}

	@Override
	protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {

		return Mono.deferContextual(contextView -> {
			exchange.getAttributes().put(GATEWAY_REACTOR_CONTEXT_ATTR, contextView);
			Query<Route> query = and(in(Route.HTTP_METHOD_ATTRIBUTE, exchange.getRequest().getMethod()),
					matchesPath(Route.REQUEST_PATH, exchange.getRequest().getURI().getRawPath()));
			return Flux.fromIterable(routeLocator.getCollectionRoutes().retrieve(query)).concatMap(route -> Mono.just(route).filterWhen(r -> r.getPredicate().apply(exchange))
					// instead of immediately stopping main flux due to error, log and
					// swallow it
					.doOnError(e -> logger.error("Error applying predicate for route: " + route.getId(), e))
					.onErrorResume(e -> Mono.empty())).next().flatMap(r -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
						exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r);
						exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
						return Mono.just(webHandler);
					}).switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
					})));
		});
	}

}
