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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.ButtonHandler;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.AttributeCollection;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.Iterables;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.jgroups.JGroupsPresenter.AUTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TOKEN;

public class ProtocolElement implements IsElement<HTMLElement>, Attachable, HasPresenter<JGroupsPresenter> {

    protected final Table<NamedNode> table;
    protected final Resources resources;
    private final String resourceName;
    private final String resourceId;
    private Form<NamedNode> form;
    private Form<ModelNode> tokenForm;
    protected JGroupsPresenter presenter;
    private HTMLElement section;
    private HTMLElement formContainer;
    private final AddressTemplate template;
    private String currentProtocolName;

    ProtocolElement(Metadata metadata, TableButtonFactory tableButtonFactory,
            Resources resources,
            AddressTemplate template, String resourceName, String resourceId) {
        this.resources = resources;
        this.template = template;
        this.resourceName = resourceName;
        this.resourceId = resourceId;

        ButtonHandler<NamedNode> launchWizard = (Table<NamedNode> table) -> {
            Set<String> current = table.getRows().stream()
                    .map(NamedNode::getName)
                    .collect(Collectors.toSet());
            presenter.addProtocol(current);
        };

        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(resourceId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(template, launchWizard))
                .button(tableButtonFactory.remove(template,
                        table -> presenter.removeResource(template, table.selectedRow().getName(), resourceName)))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .build();
        form = createForm(metadata);

        section = section()
                .add(h(1).textContent(resourceName))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(formContainer = div().add(form).element()).element();
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();

        // we cannot use table.bind since the form might be changing
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                adjustAndView(table.selectedRow());
            } else {
                form.clear();
            }
        });
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
        form.clear();
        table.enableButton(1, !models.isEmpty());
    }

    private void adjustAndView(NamedNode selectedRow) {
        String protocolName = selectedRow.getName();
        String metadataName = protocolName;
        String token = null;
        if (protocolName.equals(AUTH)) {
            token = selectedRow.get(TOKEN).asProperty().getName();
            metadataName = AUTH + " (" + token + ")";
        }
        Metadata metadata = presenter.getProtocolMetadata(metadataName);
        Set<String> attributeNames = metadata.getDescription().attributes().stream()
                .map(Property::getName).collect(Collectors.toSet());

        // most protocols have the same attributes -> form doesn't need replacing
        if (attributeNames.size() == Iterables.size(form.getFormItems()) &&
                !Iterables.any(form.getFormItems(), item -> !attributeNames.contains(item.getName())) &&
                !(AUTH.equals(protocolName)) || (AUTH.equals(currentProtocolName))) {
            currentProtocolName = protocolName;
            form.view(selectedRow);
            return;
        }

        form.detach();
        if (protocolName.equals(AUTH) && tokenForm != null) {
            tokenForm.detach();
        }

        currentProtocolName = protocolName;
        form = createForm(metadata);

        HTMLElement formElement;

        if (protocolName.equals(AUTH)) {
            String tokenType = new LabelBuilder().label(TOKEN + ": " + token);
            Metadata tokenMetadata = presenter.getAuthTokenMetadata(token);
            AddressTemplate tokenTemplate = template.append(TOKEN + "=" + token);
            Tabs tabs = new Tabs(Ids.build(resourceName, Ids.TAB_CONTAINER));
            tabs.add(Ids.TAB, Names.PROTOCOL, form.element());

            tokenForm = new ModelNodeForm.Builder<>(Ids.build(resourceName, TOKEN, Ids.FORM), tokenMetadata)
                    .onSave((form, changedValues) -> presenter
                            .saveResource(tokenTemplate, protocolName, changedValues,
                                    tokenMetadata,
                                    resources.messages().modifySingleResourceSuccess(TOKEN)))
                    .prepareReset(
                            form -> presenter.resetSingleton(tokenTemplate.replaceWildcards(protocolName), tokenType, form,
                                    tokenMetadata))
                    .build();

            addCrValidation(resources, tokenForm, tokenMetadata.getDescription().attributes());
            tabs.add(Ids.build(TOKEN, Ids.TAB), tokenType, tokenForm.element());
            formElement = tabs.element();
        } else {
            formElement = form.element();
        }

        formContainer.replaceChildren(formElement);
        form.attach();
        form.view(selectedRow);
        if (protocolName.equals(AUTH)) {
            tokenForm.attach();
            tokenForm.view(selectedRow.get(TOKEN).get(token));
        }
    }

    private Form<NamedNode> createForm(Metadata metadata) {
        return new ModelNodeForm.Builder<NamedNode>(Ids.build(resourceId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter
                        .saveResource(template, table.selectedRow().getName(), changedValues, metadata,
                                resources.messages().modifySingleResourceSuccess(resourceName)))
                .prepareReset(form -> presenter.resetResource(template, table.selectedRow().getName(), resourceName, form,
                        metadata))
                .build();
    }

    protected static void addCrValidation(Resources resources, Form<ModelNode> form, AttributeCollection attributes) {
        for (Property prop : attributes) {
            if (prop.getName().endsWith("reference")) {
                form.addFormValidation(new CredentialReference.CrFormValuesValidation(resources, prop.getName()));
            }
        }
    }
}