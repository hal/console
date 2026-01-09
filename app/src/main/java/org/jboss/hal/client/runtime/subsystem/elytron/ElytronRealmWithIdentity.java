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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pullRight;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.PAGES;

public class ElytronRealmWithIdentity implements IsElement<HTMLElement>, Attachable {

    private Table<NamedNode> table;
    private Form<NamedNode> form;
    private Form<ModelNode> identityForm;
    private Pages realmPages;
    private RealmsPresenter presenter;
    private Metadata metadata;
    private String selectedRealm;
    private String selectedIdentity;
    private String baseId;
    private String title;
    private Map<String, List<String>> originalAttributes = new HashMap<>();

    ElytronRealmWithIdentity(String baseId, Resources resources, Metadata metadata, String title) {
        this.baseId = baseId;
        this.metadata = metadata;
        this.title = title;
        LabelBuilder labelBuilder = new LabelBuilder();
        AddressTemplate template = metadata.getTemplate();
        table = new ModelNodeTable.Builder<NamedNode>(id(TABLE), metadata)
                .button(resources.constants().addIdentity(), table -> presenter.addIdentity(template,
                        metadata, table.selectedRow().getName()),
                        Constraint.executable(template, ADD_IDENTITY))
                .nameColumn()
                .column(new InlineAction<>(resources.constants().editIdentity(), this::showIdentityPage))
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement mainSection = section()
                .add(h(1).textContent(title))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        Metadata identityMetadata = metadata.forOperation(READ_IDENTITY);
        SafeHtml identityAttributeHelp = resources.messages().identityAttributeHelp();
        IdentityAttributeItem identityAttribute = new IdentityAttributeItem(ATTRIBUTES, labelBuilder.label(ATTRIBUTES));
        identityForm = new ModelNodeForm.Builder<>(Ids.build(baseId, IDENTITY, FORM), identityMetadata)
                .unboundFormItem(identityAttribute, 1, identityAttributeHelp)
                .onSave((form1, changedValues) -> presenter.saveIdentity(metadata, selectedRealm, selectedIdentity,
                        originalAttributes, identityAttribute.getValue(), success -> {
                            if (success) {
                                identityForm.getFormItem(ATTRIBUTES).setValue(identityAttribute.getValue());
                                originalAttributes = new HashMap<>(identityAttribute.getValue());
                            }
                        }))
                .build();
        // cannot change identity in UI, as there is no managed operation to change it
        identityForm.getFormItem(IDENTITY).setEnabled(false);

        HTMLButtonElement setPasswordBtn = button().id(SET_PASSWORD)
                .textContent(resources.constants().setPassword())
                .css(Button.DEFAULT_CSS, pullRight).element();
        bind(setPasswordBtn, click, ev -> presenter.launchSetPasswordWizard(metadata, selectedRealm, selectedIdentity));
        HTMLButtonElement removeIdentityBtn = button().id(REMOVE_IDENTITY)
                .textContent(resources.constants().remove())
                .css(Button.DEFAULT_CSS, pullRight)
                .title(resources.constants().removeIdentity()).element();
        bind(removeIdentityBtn, click, ev -> presenter.removeIdentity(metadata, selectedRealm, selectedIdentity, success -> {
            if (success) {
                realmPages.showPage(id(PAGE));
            }
        }));

        HTMLElement identitySection = section()
                .add(div()
                        .add(h(1).textContent(labelBuilder.label(IDENTITY)))
                        .add(p().textContent(resources.messages().identityDescription()))
                        .add(setPasswordBtn)
                        .add(removeIdentityBtn))
                .add(identityForm).element();

        realmPages = new Pages(id(PAGES), id(PAGE), mainSection);
        realmPages.addPage(id(PAGE), id(baseId),
                () -> title + ": " + selectedRealm,
                () -> labelBuilder.label(IDENTITY) + ": " + selectedIdentity, identitySection);
    }

    private String id(String... id) {
        return Ids.build(baseId, id);
    }

    private void showIdentityPage(NamedNode realm) {
        selectedRealm = realm.getName();

        // updates the identity form items (inner page)
        presenter.editIdentity(metadata, selectedRealm, title, result -> {
            this.selectedIdentity = result.get(NAME).asString();
            ModelNode modelNode = new ModelNode();
            modelNode.get(IDENTITY).set(selectedIdentity);
            identityForm.clear();
            identityForm.view(modelNode);

            if (result.hasDefined(ATTRIBUTES)) {
                Map<String, List<String>> attributes = new HashMap<>();
                result.get(ATTRIBUTES).asPropertyList().forEach(prop -> {
                    List<String> attrs = prop.getValue().asList().stream().map(ModelNode::asString).collect(toList());
                    attributes.put(prop.getName(), attrs);
                });
                originalAttributes = new HashMap<>(attributes);
                identityForm.getFormItem(ATTRIBUTES).setValue(attributes);
            }
            realmPages.showPage(id(baseId));
        });

    }

    public void setPresenter(RealmsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public HTMLElement element() {
        return realmPages.element();
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        identityForm.attach();

        table.enableButton(0, false);
        table.bindForm(form);
        table.onSelectionChange(table -> table.enableButton(0, table.hasSelection()));
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
        identityForm.detach();
    }

    public void update(List<NamedNode> items) {
        form.clear();
        identityForm.clear();
        table.update(items);
    }
}
