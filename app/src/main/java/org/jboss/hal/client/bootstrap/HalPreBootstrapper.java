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
import elemental.client.Browser;
import org.jboss.hal.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("unused")
public class HalPreBootstrapper implements PreBootstrapper {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Logger logger = LoggerFactory.getLogger(HalPreBootstrapper.class);

    @Override
    public void onPreBootstrap() {
        GWT.setUncaughtExceptionHandler(e -> {
            LoadingPanel.get().off();
            //noinspection HardCodedStringLiteral
            logger.error("Bootstrap error in {}: {}", HalPreBootstrapper.class.getSimpleName(), e.getMessage());
            Browser.getDocument().getBody().appendChild(
                    BootstrapFailed.create(CONSTANTS.bootstrapException(), e.getMessage()).asElement());
        });
    }
}
