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

	private ObjectMapper objectMapper = new ObjectMapper();
	InitRouteDefinitions(RouteDefinitionWriter routeDefinitionWriter) {
		this.routeDefinitionWriter = routeDefinitionWriter;
	}

	@PostConstruct
	public void init() throws URISyntaxException {
		for (int i = 0; i < 10; i++) {
			RouteDefinition routeDefinition = new RouteDefinition();
//			Map<String, Object> baseMap = new HashMap<>();
			Map<String, Object> map = new HashMap<>();
			map.put(RouteIndexesEnum.PATH.getValue(), "/api/full/v" + i + "/{segment}");
			map.put(RouteIndexesEnum.METHOD.getValue(), "GET,POST");
//			Map<String, Object> innerMap = new HashMap<>();
//			innerMap.put("auth", "123,456");
//			innerMap.put("other", "aaa,bbb");
//			map.put("Header", innerMap);
//			baseMap.put("BASE_INDEXES", objectMapper.convertValue(map, JsonNode.class));   //Cookie=chocolate, ch.p
			PredicateDefinition predicateDefinition = new PredicateDefinition("Cookie=chocolate, ch.p");
			routeDefinition.setId(String.valueOf(i));
			routeDefinition.setUri(new URI("http://127.0.0.1:9090"));
			routeDefinition.getPredicates().add(predicateDefinition);
			routeDefinition.setMetadata(map);
			this.routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		}
	}
}
