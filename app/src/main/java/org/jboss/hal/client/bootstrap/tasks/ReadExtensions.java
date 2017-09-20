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

import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import rx.Completable;

public class ReadExtensions implements BootstrapTask {

    private final ExtensionRegistry extensionRegistry;
    private final ExtensionStorage extensionStorage;

    @Inject
    public ReadExtensions(ExtensionRegistry extensionRegistry, ExtensionStorage extensionStorage) {
        this.extensionRegistry = extensionRegistry;
        this.extensionStorage = extensionStorage;
    }

    @Override
    public Completable call() {
        // TODO Load server side extensions from /core-service=management/console-extension=*
        for (InstalledExtension extension : extensionStorage.list()) {
            extensionRegistry.inject(extension.getFqScript(), extension.getFqStylesheets());
        }
        return Completable.complete();
    }
}
