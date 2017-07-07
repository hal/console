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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Names.CUSTOM_POLICY;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class PolicyElement implements IsElement<HTMLElement>, Attachable, HasPresenter<OtherSettingsPresenter> {

    private final Table<NamedNode> policyTable;
    private final Form<NamedNode> policyForm;
    private final Table<ModelNode> customPolicyTable;
    private final Form<ModelNode> customPolicyForm;
    private final Table<ModelNode> jaccPolicyTable;
    private final Form<ModelNode> jaccPolicyForm;
    private final Pages pages;
    private OtherSettingsPresenter presenter;
    private String selectedPolicy;
    private int customPolicyIndex = -1;
    private int jaccPolicyIndex = -1;

    PolicyElement(final Metadata metadata, final TableButtonFactory tableButtonFactory, final Resources resources) {

        policyTable = new ModelNodeTable.Builder<NamedNode>(Ids.ELYTRON_POLICY_TABLE, metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addPolicy()))
                .button(tableButtonFactory.remove(Names.POLICY, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadPolicy()))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .column(CUSTOM_POLICY, this::showCustomPolicy, "10em") //NON-NLS
                .column(Names.JACC_POLICY, this::showJaccPolicy, "10em") //NON-NLS
                .build();

        policyForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_POLICY_FORM, metadata)
                .onSave((form, changedValues) -> presenter.savePolicy(form.getModel().getName(), changedValues))
                .build();

        HTMLElement policySection = section()
                .add(h(1).textContent(Names.POLICY))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(policyTable)
                .add(policyForm)
                .asElement();

        // custom policy
        Metadata customPolicyMetadata = metadata.forComplexAttribute(ModelDescriptionConstants.CUSTOM_POLICY);
        customPolicyTable = new ModelNodeTable.Builder<>(Ids.ELYTRON_CUSTOM_POLICY_TABLE, customPolicyMetadata)
                .button(tableButtonFactory.add(customPolicyMetadata.getTemplate(),
                        table -> presenter.addCustomPolicy(selectedPolicy)))
                .button(tableButtonFactory.remove(customPolicyMetadata.getTemplate(),
                        table -> presenter.removeCustomPolicy(selectedPolicy, customPolicyIndex)))
                .column(NAME)
                .build();
        customPolicyForm = new ModelNodeForm.Builder<>(Ids.ELYTRON_CUSTOM_POLICY_FORM, customPolicyMetadata)
                .onSave(((form, changedValues) -> presenter.saveCustomPolicy(selectedPolicy,
                        form.getModel().get(HAL_INDEX).asInt(), changedValues)))
                .unsorted()
                .build();
        HTMLElement customPolicySection = section()
                .add(h(1).textContent(Names.CUSTOM_POLICY))
                .add(p().textContent(customPolicyMetadata.getDescription().getDescription()))
                .addAll(customPolicyTable, customPolicyForm)
                .asElement();

        // jacc policy
        Metadata jaccPolicyMetadata = metadata.forComplexAttribute(ModelDescriptionConstants.JACC_POLICY);
        jaccPolicyTable = new ModelNodeTable.Builder<>(Ids.ELYTRON_JACC_POLICY_TABLE, jaccPolicyMetadata)
                .button(tableButtonFactory.add(jaccPolicyMetadata.getTemplate(),
                        table -> presenter.addJaccPolicy(selectedPolicy)))
                .button(tableButtonFactory.remove(jaccPolicyMetadata.getTemplate(),
                        table -> presenter.removeJaccmPolicy(selectedPolicy, jaccPolicyIndex)))
                .column(NAME)
                .build();
        jaccPolicyForm = new ModelNodeForm.Builder<>(Ids.ELYTRON_JACC_POLICY_FORM, jaccPolicyMetadata)
                .onSave(((form, changedValues) -> presenter.saveJaccPolicy(selectedPolicy,
                        form.getModel().get(HAL_INDEX).asInt(), changedValues)))
                .unsorted()
                .build();
        HTMLElement jaccPolicySection = section()
                .add(h(1).textContent(Names.JACC_POLICY))
                .add(p().textContent(jaccPolicyMetadata.getDescription().getDescription()))
                .addAll(jaccPolicyTable, jaccPolicyForm)
                .asElement();

        pages = new Pages(Ids.ELYTRON_POLICY_PAGE, policySection);
        pages.addPage(Ids.ELYTRON_POLICY_PAGE, Ids.ELYTRON_CUSTOM_POLICY_PAGE,
                () -> Names.POLICY + ": " + selectedPolicy,
                () -> Names.CUSTOM_POLICY,
                customPolicySection);
        pages.addPage(Ids.ELYTRON_POLICY_PAGE, Ids.ELYTRON_JACC_POLICY_PAGE,
                () -> Names.POLICY + ": " + selectedPolicy,
                () -> Names.JACC_POLICY,
                jaccPolicySection);
    }

    private void showCustomPolicy(final NamedNode policyNode) {
        selectedPolicy = policyNode.getName();
        List<ModelNode> customPolicyNodes = failSafeList(policyNode, ModelDescriptionConstants.CUSTOM_POLICY);
        storeIndex(customPolicyNodes);
        customPolicyTable.update(customPolicyNodes, node -> Ids.build(node.get(NAME).asString()));
        pages.showPage(Ids.ELYTRON_CUSTOM_POLICY_PAGE);
    }

    private void showJaccPolicy(final NamedNode policyNode) {
        selectedPolicy = policyNode.getName();
        List<ModelNode> jaccPolicyNodes = failSafeList(policyNode, ModelDescriptionConstants.JACC_POLICY);
        storeIndex(jaccPolicyNodes);
        jaccPolicyTable.update(jaccPolicyNodes, node -> Ids.build(node.get(NAME).asString()));
        pages.showPage(Ids.ELYTRON_JACC_POLICY_PAGE);
    }

    @Override
    public HTMLElement asElement() {
        return pages.asElement();
    }

    @Override
    public void attach() {
        policyTable.attach();
        policyForm.attach();
        policyTable.bindForm(policyForm);

        customPolicyTable.attach();
        customPolicyForm.attach();
        customPolicyTable.bindForm(customPolicyForm);

        jaccPolicyTable.attach();
        jaccPolicyForm.attach();
        jaccPolicyTable.bindForm(jaccPolicyForm);

        customPolicyTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                customPolicyIndex = table.selectedRow().get(HAL_INDEX).asInt();
            } else {
                customPolicyIndex = -1;
                customPolicyForm.clear();
            }
        });
        jaccPolicyTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                jaccPolicyIndex = table.selectedRow().get(HAL_INDEX).asInt();
            } else {
                jaccPolicyIndex = -1;
                jaccPolicyForm.clear();
            }
        });
    }

    @Override
    public void setPresenter(final OtherSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        policyForm.clear();
        policyTable.update(nodes);

        if (Ids.ELYTRON_CUSTOM_POLICY_PAGE.equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(item -> selectedPolicy.equals(item.getName()))
                    .findFirst()
                    .ifPresent(this::showCustomPolicy);
        } else if (Ids.ELYTRON_JACC_POLICY_PAGE.equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(item -> selectedPolicy.equals(item.getName()))
                    .findFirst()
                    .ifPresent(this::showJaccPolicy);
        }
    }

}
