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
package org.jboss.hal.client.runtime;

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreListItem;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.resources.CSS.pfIcon;

public class BootErrorsElement implements IsElement<HTMLElement>, Attachable {

    private final HTMLElement bootErrorsSection;
    private final Table<ModelNode> table;
    private final Form<ModelNode> form;
    private final EmptyState noBootErrors;
    private final HTMLElement root;

    public BootErrorsElement(AddressTemplate template, MetadataRegistry metadataRegistry, Capabilities capabilities,
            Resources resources) {

        // repackage the description and the value-type of the boot errors reply properties
        Metadata managementMetadata = metadataRegistry.lookup(template);
        String description = managementMetadata.getDescription().operations().description(READ_BOOT_ERRORS);
        ModelNode attributes = ModelNodeHelper.failSafeGet(
                managementMetadata.getDescription().operations().get(READ_BOOT_ERRORS),
                String.join("/", REPLY_PROPERTIES, VALUE_TYPE));
        ModelNode modelNode = new ModelNode();
        modelNode.get(DESCRIPTION).set(description);
        modelNode.get(ATTRIBUTES).set(attributes);
        ResourceDescription resourceDescription = new ResourceDescription(modelNode);
        Metadata metadata = new Metadata(template, () -> SecurityContext.READ_ONLY, resourceDescription, capabilities);

        table = new ModelNodeTable.Builder<>(Ids.BOOT_ERRORS_TABLE, metadata)
                .column(Ids.BOOT_ERRORS_ADDRESS_COLUMN, resources.constants().address(),
                        (cell, type, row, meta) -> {
                            ResourceAddress address = new ResourceAddress(ModelNodeHelper.failSafeGet(row,
                                    FAILED_OPERATION + "/" + ADDRESS));
                            return address.isDefined() ? address.toString() : Names.NOT_AVAILABLE;
                        })
                .column(Ids.BOOT_ERRORS_OPERATION_COLUMN, resources.constants().operation(),
                        (cell, type, row, meta) -> {
                            ModelNode operation = ModelNodeHelper.failSafeGet(row,
                                    FAILED_OPERATION + "/" + OPERATION);
                            return operation.isDefined() ? operation.asString() : Names.NOT_AVAILABLE;
                        })
                .build();

        form = new ModelNodeForm.Builder<>(Ids.BOOT_ERRORS_FORM, metadata)
                .readOnly()
                .customFormItem(FAILURE_DESCRIPTION, attributeDescription -> new PreTextItem(FAILURE_DESCRIPTION))
                .customFormItem(FAILED_SERVICES, attributeDescription -> new PreListItem(FAILED_SERVICES))
                .customFormItem(SERVICES_MISSING_DEPENDENCIES,
                        attributeDescription -> new PreListItem(SERVICES_MISSING_DEPENDENCIES, Names.MISSING_DEPENDENCIES))
                .unboundFormItem(new PreListItem(SERVICES_MISSING_TRANSITIVE_DEPENDENCIES,
                        Names.MISSING_TRANSITIVE_DEPENDENCIES))
                .unboundFormItem(new PreListItem(POSSIBLE_CAUSES))
                .unsorted()
                .build();

        noBootErrors = new EmptyState.Builder(Ids.BOOT_ERRORS_EMPTY, resources.constants().noBootErrors())
                .icon(pfIcon("ok"))
                .description(resources.messages().noBootErrors())
                .build();

        root = row()
                .add(column()
                        .add(bootErrorsSection = section()
                                .add(h(1).textContent(Names.BOOT_ERRORS))
                                .add(p().textContent(resources.messages().bootErrors()))
                                .add(table)
                                .add(form).element())
                        .add(noBootErrors))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();

        table.onSelectionChange(t -> {
            if (t.hasSelection()) {
                ModelNode row = t.selectedRow();
                form.view(row);

                // Depends on WFCORE-2530 to be fixed
                List<String> missingTransitiveDependencies = failSafeList(row,
                        MISSING_TRANSITIVE_DEPENDENCY_PROBLEMS + "/" + SERVICES_MISSING_TRANSITIVE_DEPENDENCIES)
                        .stream().map(ModelNode::asString).collect(toList());
                form.<List<String>> getFormItem(SERVICES_MISSING_TRANSITIVE_DEPENDENCIES).setValue(
                        missingTransitiveDependencies);

                // Depends on WFCORE-2530 to be fixed
                List<String> possibleCauses = failSafeList(row,
                        MISSING_TRANSITIVE_DEPENDENCY_PROBLEMS + "/" + POSSIBLE_CAUSES)
                        .stream().map(ModelNode::asString).collect(toList());
                form.<List<String>> getFormItem(POSSIBLE_CAUSES).setValue(possibleCauses);
            } else {
                form.clear();
            }
        });
    }

    public void update(List<ModelNode> bootErrors) {
        setVisible(bootErrorsSection, !bootErrors.isEmpty());
        setVisible(noBootErrors.element(), bootErrors.isEmpty());
        if (!bootErrors.isEmpty()) {
            table.update(bootErrors);
            form.clear();
        }
    }
}
