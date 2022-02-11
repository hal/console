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
package org.jboss.hal.client.patching.wizard;

import java.util.List;

import org.jboss.hal.client.shared.uploadwizard.UploadContext;
import org.jboss.hal.dmr.Property;

public class PatchContext extends UploadContext {

    // used only on rollback wizard
    String patchId;
    boolean rollbackTo;
    boolean resetConfiguration;

    // shared properties used on apply patch and rollback
    boolean overrideAll;
    boolean overrideModules;
    public List<String> override;
    public List<String> preserve;

    // properties used by check running servers step
    boolean restartServers;
    public List<Property> servers;
}
