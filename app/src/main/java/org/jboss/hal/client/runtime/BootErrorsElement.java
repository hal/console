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
package org.jboss.hal.client.runtime;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreListItem;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.RefreshMode;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
public class BootErrorsElement implements IsElement, Attachable {

    private static final String BOOT_ERRORS_SECTION = "bootErrorsSection";

    private final Element bootErrorsSection;
    private final DataTable<ModelNode> table;
    private final Form<ModelNode> form;
    private final EmptyState noBootErrors;
    private final Element root;

    public BootErrorsElement(final AddressTemplate template,
            final MetadataRegistry metadataRegistry, final Capabilities capabilities,
            final Resources resources) {

        // repackage the description and the value-type of the boot errors reply properties
        Metadata managementMetadata = metadataRegistry.lookup(template);
        ModelNode description = ModelNodeHelper.failSafeGet(managementMetadata.getDescription(),
                String.join("/", OPERATIONS, READ_BOOT_ERRORS, DESCRIPTION));
        ModelNode attributes = ModelNodeHelper.failSafeGet(managementMetadata.getDescription(),
                String.join("/", OPERATIONS, READ_BOOT_ERRORS, REPLY_PROPERTIES, VALUE_TYPE));
        ModelNode modelNode = new ModelNode();
        modelNode.get(DESCRIPTION).set(description);
        modelNode.get(ATTRIBUTES).set(attributes);
        Metadata metadata = new Metadata(template, SecurityContext.READ_ONLY,
                new ResourceDescription(modelNode), capabilities);

        Options<ModelNode> options = new ModelNodeTable.Builder<>(metadata)
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
        table = new ModelNodeTable<>(Ids.BOOT_ERRORS_TABLE, options);

        form = new ModelNodeForm.Builder<>(Ids.BOOT_ERRORS_FORM, metadata)
                .viewOnly()
                .customFormItem(FAILURE_DESCRIPTION, attributeDescription -> new PreTextItem(FAILURE_DESCRIPTION))
                .customFormItem(FAILED_SERVICES, attributeDescription -> new PreListItem(FAILED_SERVICES))
                .customFormItem(SERVICES_MISSING_DEPENDENCIES, attributeDescription ->
                        new PreListItem(SERVICES_MISSING_DEPENDENCIES, Names.MISSING_DEPENDENCIES))
                .unboundFormItem(new PreListItem(SERVICES_MISSING_TRANSITIVE_DEPENDENCIES,
                        Names.MISSING_TRANSITIVE_DEPENDENCIES))
                .unboundFormItem(new PreListItem(POSSIBLE_CAUSES))
                .unsorted()
                .build();

        noBootErrors = new EmptyState.Builder(resources.constants().noBootErrors())
                .icon(pfIcon("ok"))
                .description(resources.messages().noBootErrors())
                .build();

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .section().rememberAs(BOOT_ERRORS_SECTION)
                        .h(1).textContent(Names.BOOT_ERRORS).end()
                        .p().textContent(resources.messages().bootErrors()).end()
                        .add(table.asElement())
                        .add(form.asElement())
                    .end()
                    .add(noBootErrors)
                .end()
            .end();
        // @formatter:on

        this.bootErrorsSection = layoutBuilder.referenceFor(BOOT_ERRORS_SECTION);
        this.root = layoutBuilder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();

        table.api().onSelectionChange(api -> {
            if (api.hasSelection()) {
                ModelNode row = api.selectedRow();
                form.view(row);

                // Depends on WFCORE-2530 to be fixed
                List<String> missingTransitiveDependencies = failSafeList(row,
                        MISSING_TRANSITIVE_DEPENDENCY_PROBLEMS + "/" + SERVICES_MISSING_TRANSITIVE_DEPENDENCIES)
                        .stream().map(ModelNode::asString).collect(toList());
                form.<List<String>>getFormItem(SERVICES_MISSING_TRANSITIVE_DEPENDENCIES).setValue(
                        missingTransitiveDependencies);

                // Depends on WFCORE-2530 to be fixed
                List<String> possibleCauses = failSafeList(row,
                        MISSING_TRANSITIVE_DEPENDENCY_PROBLEMS + "/" + POSSIBLE_CAUSES)
                        .stream().map(ModelNode::asString).collect(toList());
                form.<List<String>>getFormItem(POSSIBLE_CAUSES).setValue(possibleCauses);
            } else {
                form.clear();
            }
        });
    }

    public void update(final List<ModelNode> bootErrors) {
        Elements.setVisible(bootErrorsSection, !bootErrors.isEmpty());
        Elements.setVisible(noBootErrors.asElement(), bootErrors.isEmpty());
        if (!bootErrors.isEmpty()) {
            table.api().clear().add(bootErrors).refresh(RefreshMode.RESET);
            form.clear();
        }
    }
}
