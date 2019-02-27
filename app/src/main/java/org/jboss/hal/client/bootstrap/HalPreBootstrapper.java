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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.PreBootstrapper;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.client.logging.LogConfiguration;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;

public class HalPreBootstrapper implements PreBootstrapper {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(HalPreBootstrapper.class);

    @Override
    public void onPreBootstrap() {
        LogConfiguration.configure();
        GWT.setUncaughtExceptionHandler(e -> {
            LoadingPanel.get().off();
            String errorMessage = e != null ? e.getMessage() : Names.NOT_AVAILABLE;
            logger.error("Uncaught bootstrap error: {}", errorMessage);
            if (!document.body.hasChildNodes()) {
                Elements.removeChildrenFrom(document.body);
                document.body.appendChild(BootstrapFailed.create(errorMessage, Endpoints.INSTANCE).element());
            }
        });
    }
}
