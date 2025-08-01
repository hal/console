/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.installer;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;

import static java.util.Collections.emptyList;

/** Common context used by all update manager wizards */
class UpdateManagerContext {

    boolean prepared;
    UpdateItem updateItem;
    List<ModelNode> updates;
    String workDir;
    List<String> missingCerts;
    List<CertificateInfo> missingCertInfos;
    Operation listOperation;
    String nodeType;

    UpdateManagerContext() {
        this(emptyList(), null);
    }

    UpdateManagerContext(final List<ModelNode> updates) {
        this(updates, null);
    }

    UpdateManagerContext(final List<ModelNode> updates, final UpdateItem updateItem) {
        this.updateItem = updateItem;
        this.updates = updates;
    }

    public UpdateManagerContext(List<String> missingCerts, List<CertificateInfo> missingCertInfos,
            Operation listOperation, String nodeType) {
        this.missingCerts = missingCerts;
        this.missingCertInfos = missingCertInfos;
        this.listOperation = listOperation;
        this.nodeType = nodeType;
        // updates list will be modified later - don't use emptyList()
        this.updates = new ArrayList<>();
    }

    public UpdateManagerContext(UpdateItem updateItem, List<String> missingCerts, List<CertificateInfo> missingCertInfos,
            Operation listOperation, String nodeType) {
        this(missingCerts, missingCertInfos, listOperation, nodeType);
        this.updateItem = updateItem;
    }
}
