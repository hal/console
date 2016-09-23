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
package org.jboss.hal.dmr;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.DomainProcessStateProcessor;
import org.jboss.hal.dmr.dispatch.ProcessStateProcessor;
import org.jboss.hal.dmr.dispatch.StandaloneProcessStateProcessor;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.spi.GinModule;

/**
 * @author Harald Pehl
 */
@GinModule
public class DmrModule extends AbstractGinModule {

    private final ProcessStateProcessor standalone;
    private final ProcessStateProcessor domain;

    public DmrModule() {
        standalone = new StandaloneProcessStateProcessor();
        domain = new DomainProcessStateProcessor();
    }

    @Override
    protected void configure() {
        bind(Dispatcher.class);
        bind(Macros.class).in(Singleton.class);
    }

    @Provides
    public ProcessStateProcessor provideProcessStateProcessor(Environment environment) {
        if (environment.isStandalone()) {
            return standalone;
        } else {
            return domain;
        }
    }
}
