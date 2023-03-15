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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author Neal
 */
public class WrapRoute implements Ordered {
	// original route
	private final Route route;
	// method
	private HttpMethod requestMethod;

	//parameters
	private MultiValueMap<String,String> requestParameters;

	// headers
	private MultiValueMap<String, String> requestHeaders;
	private PathPatternParser pathPatternParser = PathPatternParser.defaultInstance;

	private final List<PathPattern> requestPathPatterns = new ArrayList<>();

	public WrapRoute(Route route) {
		this.route = route;
		initRequestURI(route);
	}

	private void initRequestURI(Route route) {
		Map<String, Object> metadata = route.getMetadata();
		String path = String.valueOf(metadata.get("path"));
		PathPattern pathPattern1 = pathPatternParser.parse(path);
		requestPathPatterns.add(pathPattern1);
	}


	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	/**
	 * index.
	 */
	public static final Attribute<WrapRoute, PathPattern> REQUEST_PATH = new MultiValueAttribute<WrapRoute, PathPattern>(
			"requestPathPatterns") {
		public Iterable<PathPattern> getValues(WrapRoute routePath, QueryOptions queryOptions) {
			return routePath.requestPathPatterns;
		}
	};

	/**
	 * index.
	 */
	public static final Attribute<WrapRoute, HttpMethod> HTTP_METHOD_ATTRIBUTE = new SimpleAttribute<WrapRoute, HttpMethod>("requestMethod") { public HttpMethod getValue(WrapRoute wrapRoute, QueryOptions queryOptions) {
			return wrapRoute.requestMethod;
		}
	};

	public Route getRoute() {
		return route;
	}

}
