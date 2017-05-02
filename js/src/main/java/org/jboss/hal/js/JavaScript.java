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
package org.jboss.hal.js;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class JavaScript implements EntryPoint {

    private static final JavaScriptGinjector GINJECTOR = GWT.create(JavaScriptGinjector.class);

    @Override
    public void onModuleLoad() {
        Scheduler.get().scheduleDeferred(() -> {
            DelayedBindRegistry.bind(GINJECTOR);
            GINJECTOR.getEndpoints().useBase("http://localhost:9990"); // use as default
        });
    }
}
