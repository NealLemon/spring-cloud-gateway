package org.springframework.cloud.gateway.sample.init;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

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


	private final RouteDefinitionWriter routeDefinitionWriter;


	InitRouteDefinitions(RouteDefinitionWriter routeDefinitionWriter) {
		this.routeDefinitionWriter = routeDefinitionWriter;
	}

	@PostConstruct
	public void init() throws URISyntaxException {
		for (int i = 0; i < 100000; i++) {
			RouteDefinition routeDefinition = new RouteDefinition();
			Map<String, Object> map = new HashMap<>();
			map.put("Path", "/api/full/v" + i + "/{segment}");
			map.put("Method", "GET");
			map.put("Query", "p1");
			routeDefinition.setId(String.valueOf(i));
			routeDefinition.setUri(new URI("http://127.0.0.1:9090"));
			routeDefinition.setMetadata(map);
			this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		}
	}
}
