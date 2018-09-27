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
package org.jboss.hal.client.configuration.subsystem.resourceadapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Strings;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.client.configuration.subsystem.resourceadapter.ResourceAdapter.AdapterType;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.resourceadapter.AddressTemplates.RESOURCE_ADAPTER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

@AsyncColumn(Ids.RESOURCE_ADAPTER)
@Requires(RESOURCE_ADAPTER_ADDRESS)
public class ResourceAdapterColumn extends FinderColumn<ResourceAdapter> {

    @Inject
    public ResourceAdapterColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            Places places,
            Resources resources) {

        super(new Builder<ResourceAdapter>(finder, Ids.RESOURCE_ADAPTER, Names.RESOURCE_ADAPTER)

                .itemsProvider((context, callback) ->
                        crud.readChildren(RESOURCE_ADAPTER_SUBSYSTEM_TEMPLATE, RESOURCE_ADAPTER, children -> {
                            List<ResourceAdapter> resourceAdapters = children.stream()
                                    .map(ResourceAdapter::new)
                                    .collect(toList());
                            callback.onSuccess(resourceAdapters);
                        }))

                .withFilter()
                .filterDescription(resources.messages().resourceAdapterColumnFilterDescription())
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(item -> new ResourceAdapterPreview(item, resources))
        );

        addColumnAction(columnActionFactory.add(
                Ids.RESOURCE_ADAPTER_ADD,
                Names.RESOURCE_ADAPTER,
                RESOURCE_ADAPTER_TEMPLATE,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(RESOURCE_ADAPTER_TEMPLATE);
                    ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(
                            Ids.RESOURCE_ADAPTER_FORM, metadata)
                            .fromRequestProperties()
                            .unboundFormItem(new NameItem())
                            .include(ARCHIVE, MODULE)
                            .build();
                    form.addFormValidation(f -> {
                        FormItem<String> archiveItem = f.getFormItem(ARCHIVE);
                        FormItem<String> moduleItem = f.getFormItem(MODULE);
                        String archive = archiveItem.getValue();
                        String module = moduleItem.getValue();
                        if (Strings.isNullOrEmpty(archive) && Strings.isNullOrEmpty(module)) {
                            LabelBuilder labelBuilder = new LabelBuilder();
                            return ValidationResult.invalid(resources.messages()
                                    .atLeastOneIsRequired(labelBuilder.enumeration(asList(ARCHIVE, MODULE),
                                            resources.constants().or())));
                        }
                        return ValidationResult.OK;
                    });
                    AddResourceDialog dialog = new AddResourceDialog(
                            resources.messages().addResourceTitle(Names.RESOURCE_ADAPTER), form,
                            (name, model) -> crud.add(Names.RESOURCE_ADAPTER, name, RESOURCE_ADAPTER_TEMPLATE, model,
                                    (n, a) -> refresh(Ids.resourceAdapter(n))));
                    dialog.getForm().<String>getFormItem(NAME).addValidationHandler(createUniqueValidation());
                    dialog.show();
                }));

        setItemRenderer(item -> new ItemDisplay<ResourceAdapter>() {
            @Override
            public String getId() {
                return Ids.resourceAdapter(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                if (item.hasTransactionSupport()) {
                    return ItemDisplay
                            .withSubtitle(item.getName(), item.get(TRANSACTION_SUPPORT).asString());
                }
                return null;
            }

            @Override
            public HTMLElement getIcon() {
                return item.getAdapterType() == AdapterType.ARCHIVE
                        ? span().css(fontAwesome("archive")).asElement()
                        : span().css(fontAwesome("cubes")).asElement();
            }

            @Override
            public String getTooltip() {
                return item.getAdapterType() == AdapterType.ARCHIVE ? Names.ARCHIVE : Names.MODULE;
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(item.getName());
                data.add(item.getAdapterType().name().toLowerCase());
                if (item.hasTransactionSupport()) {
                    data.add(item.get(TRANSACTION_SUPPORT).asString());
                }
                return String.join(" ", data);
            }

            @Override
            public List<ItemAction<ResourceAdapter>> actions() {
                List<ItemAction<ResourceAdapter>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.RESOURCE_ADAPTER)
                        .with(NAME, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.RESOURCE_ADAPTER, item.getName(),
                        AddressTemplates.RESOURCE_ADAPTER_TEMPLATE, ResourceAdapterColumn.this));
                return actions;
            }
        });
    }
}
