package uk.bl.datacitestats;


import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.guice.SelfInjectingServerResourceModule;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

import uk.bl.datacitestats.rest.AdminResource;
import uk.bl.datacitestats.rest.CacheFilter;
import uk.bl.datacitestats.rest.DOIPrefixResource;
import uk.bl.datacitestats.rest.StatsResource;

import com.google.inject.Guice;

/**
 * Main restlet application. Inits the injector and creates routes. Also sets up
 * webjars
 * 
 * @author tom
 * 
 */
public class StatsApplication extends Application {

	@Override
	public Restlet createInboundRoot() {
		Guice.createInjector(new GuiceConfigModule(getContext().getParameters()),
				new SelfInjectingServerResourceModule(),new javax.cache.annotation.impl.guice.module.CacheAnnotationsModule());
		Router root = new Router(this.getContext());
		root.attach("/stats/{type}", StatsResource.class);
		root.attach("/stats/{type}/{prefix}", StatsResource.class);
		root.attach("/stats/{type}/{prefix}/{suffix}", StatsResource.class);
		root.attach("/dois/prefixes", DOIPrefixResource.class);

		root.attach("/admin/{action}", AdminResource.class);

		final Directory dir = new Directory(getContext(), "clap://class/META-INF/resources/webjars");
		Filter cache = new CacheFilter(getContext(), dir);
		root.attach("/webjars", cache);

		return root;
	}

}
