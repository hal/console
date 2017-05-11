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
package org.jboss.hal.client.bootstrap.functions;

import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;

/**
 * @author Harald Pehl
 */
public class ReadExtensions implements BootstrapFunction {

    private final ExtensionRegistry extensionRegistry;
    private final ExtensionStorage extensionStorage;

    @Inject
    public ReadExtensions(final ExtensionRegistry extensionRegistry, final ExtensionStorage extensionStorage) {
        this.extensionRegistry = extensionRegistry;
        this.extensionStorage = extensionStorage;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        // TODO Load server side extensions from /core-service=management/console-extension=*
        extensionStorage.list().forEach(extensionRegistry::inject);
        control.proceed();
    }

    @Override
    public String name() {
        return "Bootstrap[ReadExtensions]";
    }
}
