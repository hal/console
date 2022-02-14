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
package org.jboss.hal.client.configuration.subsystem.elytron;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;

class ElytronSubsystemPreview extends PreviewContent<StaticItem> {

    private final CrudOperations crud;
    private final PreviewAttributes<ModelNode> attributes;

    @SuppressWarnings("HardCodedStringLiteral")
    ElytronSubsystemPreview(CrudOperations crud, Resources resources) {
        super(resources.constants().globalSettings());
        this.crud = crud;
        this.attributes = new PreviewAttributes<>(new ModelNode(),
                asList("default-authentication-context",
                        "disallowed-providers",
                        "initial-providers",
                        "final-providers",
                        "security-properties"));

        previewBuilder().addAll(attributes);
    }

    @Override
    public void update(final StaticItem item) {
        crud.read(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, attributes::refresh);
    }
}
