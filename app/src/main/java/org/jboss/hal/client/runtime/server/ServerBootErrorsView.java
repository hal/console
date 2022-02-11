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
package org.jboss.hal.client.runtime.server;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.client.runtime.BootErrorsElement;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.runtime.server.ServerBootErrorsPresenter.MANAGEMENT_TEMPLATE;

public class ServerBootErrorsView extends HalViewImpl implements ServerBootErrorsPresenter.MyView {

    private final BootErrorsElement bootErrorsElement;

    @Inject
    public ServerBootErrorsView(MetadataRegistry metadataRegistry, Capabilities capabilities,
            Resources resources) {
        bootErrorsElement = new BootErrorsElement(MANAGEMENT_TEMPLATE, metadataRegistry, capabilities, resources);
        registerAttachable(bootErrorsElement);
        initElement(bootErrorsElement.element());
    }

    @Override
    public void update(List<ModelNode> bootErrors) {
        bootErrorsElement.update(bootErrors);
    }
}
