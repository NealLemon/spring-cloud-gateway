package org.springframework.cloud.gateway.sample.init;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Neal
 * @date: 2023/3/14
 **/
@Component
public class InitRouteDefinitions {


	private RouteDefinitionWriter routeDefinitionWriter;

	InitRouteDefinitions(RouteDefinitionWriter routeDefinitionWriter) throws URISyntaxException {
		this.routeDefinitionWriter = routeDefinitionWriter;
		for(int i = 0; i < 100000; i++) {
			RouteDefinition routeDefinition = new RouteDefinition();
			Map<String,Object> map = new HashMap<>();
			if(i%2 == 0) {
				map.put("path","/api/full/v"+i+"/{hello}");
			}else{
				map.put("path","/api/full/v"+i+"/{world}");
			}
			routeDefinition.setId("pathPredicate" + i);
			routeDefinition.setUri(new URI("http://127.0.0.1:9090"));
			routeDefinition.setMetadata(map);
			routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		}
	}
}
