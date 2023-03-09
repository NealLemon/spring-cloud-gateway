package org.springframework.cloud.gateway.handler;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;

/**
 * @description: TODO
 * @author: Neal
 * @date: 2023/3/9
 **/
public class RouteCQengineHandlerMapping extends AbstractHandlerMapping {

	private final FilteringWebHandler webHandler;

	private final RouteLocator routeLocator;

	public RouteCQengineHandlerMapping(FilteringWebHandler webHandler,
									   RouteLocator routeLocator,
									   GlobalCorsProperties globalCorsProperties,
									   Environment environment) {
		this.webHandler = webHandler;
		this.routeLocator = routeLocator;
		setOrder(environment.getProperty(GatewayProperties.PREFIX + ".handler-mapping.order", Integer.class, 1));
		setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
	}

	@Override
	protected Mono<?> getHandlerInternal(ServerWebExchange exchange) {
		return Mono.deferContextual(contextView -> {
			exchange.getAttributes().put(GATEWAY_REACTOR_CONTEXT_ATTR, contextView);
			return routeLocator.getRoutes().concatMap(route -> Mono.just(route).filter(r -> r.getId().equals("test_router"))).next()
					.flatMap((Function<Route, Mono<?>>) r -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
						exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r);
						return Mono.just(webHandler);
					}).switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
					})));
		});
	}
}
