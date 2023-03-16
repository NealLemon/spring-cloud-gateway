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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
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
	private Set<String> requestParameters;

	// headers
	private MultiValueMap<String, String> requestHeaders;

	private PathPatternParser pathPatternParser = PathPatternParser.defaultInstance;

	private final List<PathPattern> requestPathPatterns = new ArrayList<>();

	public WrapRoute(Route route) {
		this.route = route;
		initIndexes(route);
	}

	/**
	 * 初始化查询变量.
	 * @param route original route
	 */
	private void initIndexes(Route route) {
		Map<String, Object> metadata = route.getMetadata();
		//装配Path
		Object pathObj = metadata.get("Path");
		if (null != pathObj) {
			String[] pathArr = String.valueOf(pathObj).split(",");
			for (int i = 0; i < pathArr.length; i++) {
				PathPattern pathPattern = pathPatternParser.parse(pathArr[i]);
				requestPathPatterns.add(pathPattern);
			}
		}
		//装配Parameters
		Object queryObj = metadata.get("Query");
		if (null != queryObj) {
			String[] parameters = String.valueOf(metadata.get("Query")).split(",");
			requestParameters = new HashSet<>(parameters.length);
			for (int i = 0; i < parameters.length; i++) {
				requestParameters.add(parameters[i]);
			}
		}
		//装配Method
		Object methodObj = metadata.get("Method");
		requestMethod = HttpMethod.valueOf(String.valueOf(methodObj));
		//TODO 装配headers

	}


	@Override
	public int getOrder() {
		return route.getOrder();
	}

	/**
	 * API index.
	 */
	public static final Attribute<WrapRoute, PathPattern> REQUEST_PATH = new MultiValueAttribute<WrapRoute, PathPattern>(
			"requestPathPatterns") {
		public Iterable<PathPattern> getValues(WrapRoute routePath, QueryOptions queryOptions) {
			return routePath.requestPathPatterns;
		}
	};

	/**
	 * Primary Key ID Index.
	 */
	public static final SimpleAttribute<WrapRoute, String> ID_INDEX = new SimpleAttribute<WrapRoute, String>("routeId") {
		@Override
		public String getValue(WrapRoute wrapRoute, QueryOptions queryOptions) {
			return wrapRoute.getRoute().getId();
		}
	};

	/**
	 * Method index.
	 */
	public static final Attribute<WrapRoute, HttpMethod> HTTP_METHOD_ATTRIBUTE = new SimpleAttribute<WrapRoute, HttpMethod>("requestMethod") { public HttpMethod getValue(WrapRoute wrapRoute, QueryOptions queryOptions) {
			return wrapRoute.requestMethod;
		}
	};

	/**
	 * Parameters Index.
	 */
	public static final Attribute<WrapRoute, Set<String>> REQUEST_PARAMETERS = new SimpleNullableAttribute<WrapRoute, Set<String>>("requestParameters") {
		@Override
		public Set<String> getValue(WrapRoute wrapRoute, QueryOptions queryOptions) {
			return wrapRoute.requestParameters;
		}
	};

	public Route getRoute() {
		return route;
	}


	public MultiValueMap<String, String> getRequestHeaders() {
		return requestHeaders;
	}
}
