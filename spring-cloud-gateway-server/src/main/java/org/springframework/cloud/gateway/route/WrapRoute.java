package org.springframework.cloud.gateway.route;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;

public class WrapRoute  implements Ordered {

	private final Route route;
	private List<PathPattern> requestPathPatterns = new ArrayList<>();
	public WrapRoute(Route route) {
		this.route = route;
		initRequestURI(route);
	}

	private void initRequestURI(Route route) {
		PathPatternParser pathPatternParser = new PathPatternParser();
		PathPattern pathPattern1 = pathPatternParser.parse("/api/v1/a/*");
		PathPattern pathPattern2 = pathPatternParser.parse("/api/v1/{name}");
		requestPathPatterns.add(pathPattern1);
		requestPathPatterns.add(pathPattern2);
	}


	private HttpMethod requestMethod;


	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}


	public static final Attribute<WrapRoute, PathPattern> REQUEST_PATH = new MultiValueAttribute<WrapRoute, PathPattern>("requestPathPatterns") {
		public Iterable<PathPattern> getValues(WrapRoute routePath, QueryOptions queryOptions) { return routePath.requestPathPatterns; }
	};

	public Route getRoute() {
		return route;
	}
}
