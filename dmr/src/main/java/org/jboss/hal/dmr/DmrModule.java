/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.ProcessStateProcessor;
import org.jboss.hal.dmr.dispatch.ResponseHeadersProcessors;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.spi.GinModule;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

@GinModule
public class DmrModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(Macros.class).in(Singleton.class);
        bind(ProcessStateProcessor.class).in(Singleton.class);
        bind(ResponseHeadersProcessors.class).in(Singleton.class);

        bind(Dispatcher.class);
    }
}
