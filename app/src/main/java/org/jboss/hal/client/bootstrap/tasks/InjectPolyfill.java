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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

import elemental2.dom.HTMLScriptElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.js.Browser;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static elemental2.dom.DomGlobal.document;

public class InjectPolyfill implements BootstrapTask {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(InjectPolyfill.class);
    private static final String PROMISE_POLYFILL = "js/polyfill";

    private final Environment environment;

    @Inject
    public InjectPolyfill(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Completable call() {
        if (Browser.isIE()) {
            String src = PROMISE_POLYFILL + (environment.isProductionMode() ? ".min.js" : ".js");
            logger.debug("Inject IE polyfill: '{}'", src);
            HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
            script.src = src;
            document.head.appendChild(script);
        }
        return Completable.complete();
    }
}
