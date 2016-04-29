/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.standalone;

import java.util.logging.Logger;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;

import static io.undertow.predicate.Predicates.not;
import static io.undertow.predicate.Predicates.suffixes;

/**
 * Start a web server for the console at <a href="http://localhost:9090">http://localhost:9090</a>. Please make sure to
 * add {@code http://localhost:9090} as allowed origin in WildFly.
 *
 * @author Harald Pehl
 */
public class Main {

    @SuppressWarnings("HardCodedStringLiteral")
    public static void main(String[] args) {

        ClassPathResourceManager resource = new ClassPathResourceManager(Main.class.getClassLoader(), "hal");
        ResourceHandler handler = new ResourceHandler(resource)
                .setResourceManager(resource)
                .setDirectoryListingEnabled(false)
                .setCachable(not(suffixes(".nocache.js", "index.html")));

        Undertow server = Undertow.builder()
                .addHttpListener(9090, "localhost")
                .setHandler(handler)
                .build();
        server.start();
        Logger.getLogger("HAL").info("Serving console from http://localhost:9090");
    }
}
