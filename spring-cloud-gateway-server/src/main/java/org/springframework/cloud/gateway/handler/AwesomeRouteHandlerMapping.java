package org.springframework.cloud.gateway.handler;

import com.googlecode.cqengine.query.Query;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.route.AwesomeRouteLocator;
import org.springframework.cloud.gateway.route.WrapRoute;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.googlecode.cqengine.query.QueryFactory.matchesPath;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;
import static org.springframework.http.server.PathContainer.parsePath;

/**
 * @description: TODO
 * @author: Neal
 * @date: 2023/3/9
 **/
public class AwesomeRouteHandlerMapping extends AbstractHandlerMapping {

	private final FilteringWebHandler webHandler;

	private final AwesomeRouteLocator routeLocator;
	public AwesomeRouteHandlerMapping(FilteringWebHandler webHandler,
									  AwesomeRouteLocator routeLocator,
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
			Query<WrapRoute> query = matchesPath(WrapRoute.REQUEST_PATH, exchange.getRequest().getURI().getRawPath());
			return Mono.just(routeLocator.getCollectionRoutes().retrieve(query).uniqueResult())
					.flatMap( r -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
						exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, r.getRoute());
						exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getRoute().getId());
						return Mono.just(webHandler);
					}).switchIfEmpty(Mono.empty().then(Mono.fromRunnable(() -> {
						exchange.getAttributes().remove(GATEWAY_PREDICATE_ROUTE_ATTR);
					})));
		});
	}
}
