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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import javax.inject.Inject;

import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Resources;

public class ResourceAdapterView extends BaseView<ResourceAdapterPresenter> implements ResourceAdapterPresenter.MyView {

    @Inject
    public ResourceAdapterView(MetadataRegistry metadataRegistry, Resources resources) {
        super(metadataRegistry, resources);
    }
}
