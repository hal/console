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
package org.jboss.hal.client.configuration.subsystem.securitymanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.securitymanager.AddressTemplates.DEPLOYMENT_PERMISSIONS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;
import static org.jboss.hal.resources.CSS.marginTopLarge;

public class SecurityManagerView extends HalViewImpl implements SecurityManagerPresenter.MyView {

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final VerticalNavigation navigation;
    private final Map<Permission, EmptyState> emptyStates;
    private final Map<Permission, HTMLElement> masterDetails;
    private final Map<Permission, Table<ModelNode>> tables;
    private final Map<Permission, Form<ModelNode>> forms;
    private SecurityManagerPresenter presenter;

    @Inject
    public SecurityManagerView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.navigation = new VerticalNavigation();
        this.emptyStates = new HashMap<>();
        this.masterDetails = new HashMap<>();
        this.tables = new HashMap<>();
        this.forms = new HashMap<>();

        createPermissionUI(Permission.MINIMUM_PERMISSIONS);
        createPermissionUI(Permission.MAXIMUM_PERMISSIONS);

        registerAttachable(navigation);
        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private void createPermissionUI(Permission permission) {
        Metadata metadata = metadataRegistry.lookup(DEPLOYMENT_PERMISSIONS_TEMPLATE)
                .forComplexAttribute(permission.resource);
        Constraint constraint = Constraint.writable(DEPLOYMENT_PERMISSIONS_TEMPLATE, permission.resource);

        EmptyState emptyState = new EmptyState.Builder(Ids.DEPLOYMENT_PERMISSIONS_EMPTY, Names.DEPLOYMENT_PERMISSIONS)
                .description(resources.messages().noDeploymentPermissions())
                .primaryAction(resources.constants().add(), () -> presenter.addDeploymentPermissions(), constraint)
                .build();
        emptyState.element().classList.add(marginTopLarge);
        emptyStates.put(permission, emptyState);

        Table<ModelNode> table = new ModelNodeTable.Builder<>(Ids.build(permission.baseId, Ids.TABLE), metadata)
                .button(resources.constants().add(), t -> presenter.addPermission(permission),
                        constraint)
                .button(resources.constants().remove(),
                        t -> presenter.removePermission(permission, t.selectedRow().get(HAL_INDEX).asInt()),
                        Scope.SELECTED, constraint)
                .column(CLASS)
                .build();
        tables.put(permission, table);
        registerAttachable(table);

        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(permission.baseId, Ids.FORM), metadata)
                .onSave((f, changedValues) -> presenter.savePermission(permission, f.getModel().get(HAL_INDEX).asInt(),
                        changedValues))
                .build();
        forms.put(permission, form);
        registerAttachable(form);

        HTMLElement masterDetail;
        HTMLElement section = section()
                .add(emptyState)
                .add(masterDetail = div()
                        .add(h(1).textContent(permission.type))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(table)
                        .add(form)
                        .get())
                .get();
        masterDetails.put(permission, masterDetail);
        navigation.addPrimary(Ids.build(permission.baseId, Ids.ITEM), permission.type, permission.icon, section);
    }

    @Override
    public void attach() {
        super.attach();
        tables.forEach((permission, table) -> table.bindForm(forms.get(permission)));
    }

    @Override
    public void setPresenter(SecurityManagerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(boolean defined, Map<Permission, List<ModelNode>> permissions) {
        for (Permission permission : Permission.values()) {
            Elements.setVisible(emptyStates.get(permission).element(), !defined);
            Elements.setVisible(masterDetails.get(permission), defined);
        }
        permissions.forEach((permission, nodes) -> {
            forms.get(permission).clear();
            tables.get(permission).update(nodes, modelNode -> modelNode.get(CLASS).asString());
        });
    }
}
