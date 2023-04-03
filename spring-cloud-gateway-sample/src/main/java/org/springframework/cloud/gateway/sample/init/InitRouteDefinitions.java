package org.springframework.cloud.gateway.sample.init;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteIndexesEnum;
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
		for(int i = 0; i < 100000; i++) {
			RouteDefinition routeDefinition = new RouteDefinition();
			String path  = "/api/*/v"+i+"/test";
			routeDefinition.setId(String.valueOf(i));
			routeDefinition.setUri(new URI("http://127.0.0.1:9090"));
			PredicateDefinition p1 = new PredicateDefinition("Path="+path);
			PredicateDefinition p2 = new PredicateDefinition("Query=p1");
			PredicateDefinition p3 = new PredicateDefinition("Method=GET");
			PredicateDefinition p4 = new PredicateDefinition("Header=auth,123");
			routeDefinition.getPredicates().add(p1);
			routeDefinition.getPredicates().add(p2);
			routeDefinition.getPredicates().add(p3);
			routeDefinition.getPredicates().add(p4);
			routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		}
	}
}
