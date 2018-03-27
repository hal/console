/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.management.sslwizard;

import org.jboss.hal.dmr.ModelNode;

class EnableSSLContext {

    enum Strategy {

        // asks the user to create a key-store and generate a self-signed key
        KEYSTORE_CREATE,
        // the java key store file exists in the filesystem, asks to create a key-store elytron resource
        KEYSTORE_FILE_EXISTS,
        // an elytron key-store already exists, and will be used.
        KEYSTORE_RESOURCE_EXISTS;

    }

    Boolean mutualAuthentication;
    Strategy strategy;
    ModelNode model;
    int securePort;
}
