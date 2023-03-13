package org.springframework.cloud.gateway.route;

import com.googlecode.cqengine.IndexedCollection;

public interface AwesomeRoutes extends RouteLocator{

	IndexedCollection<WrapRoute> getCollectionRoutes();
}
