package org.jboss.hal.standalone;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

@ApplicationScoped
public class Main {

    public void init(@Observes Router router) {
        StaticHandler noCache = StaticHandler.create().setCachingEnabled(false);
        StaticHandler staticHandler = StaticHandler.create();

        router.getWithRegex(".*nocache.*").order(0).handler(noCache);
        router.get().order(1).handler(staticHandler);
    }
}
