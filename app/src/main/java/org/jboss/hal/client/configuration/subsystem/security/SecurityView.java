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
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"HardCodedStringLiteral", "WeakerAccess", "unused", "DuplicateStringLiteralInspection"})
public class SecurityView extends MbuiViewImpl<SecurityPresenter> implements SecurityPresenter.MyView {

    public static SecurityView create(final MbuiContext mbuiContext) {
        return new Mbui_SecurityView(mbuiContext);
    }

    @MbuiElement("security-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("security-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("security-elytron-key-manager-table") DataTable<NamedNode> keyManagerTable;
    @MbuiElement("security-elytron-key-manager-form") Form<NamedNode> keyManagerForm;
    @MbuiElement("security-elytron-key-store-table") DataTable<NamedNode> keyStoreTable;
    @MbuiElement("security-elytron-key-store-form") Form<NamedNode> keyStoreForm;
    @MbuiElement("security-elytron-realm-table") DataTable<NamedNode> realmTable;
    @MbuiElement("security-elytron-realm-form") Form<NamedNode> realmForm;
    @MbuiElement("security-elytron-trust-manager-table") DataTable<NamedNode> trustManagerTable;
    @MbuiElement("security-elytron-trust-manager-form") Form<NamedNode> trustManagerForm;
    @MbuiElement("security-elytron-trust-store-table") DataTable<NamedNode> trustStoreTable;
    @MbuiElement("security-elytron-trust-store-form") Form<NamedNode> trustStoreForm;
    @MbuiElement("security-vault-form") FailSafeForm<ModelNode> vaultForm;

    SecurityView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);
        
        keyManagerTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, "elytron-key-manager")))
                .refresh(RefreshMode.RESET);
        keyManagerForm.clear();
        keyStoreTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, "elytron-key-store")))
                .refresh(RefreshMode.RESET);
        keyStoreForm.clear();

        realmTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, "elytron-realm")))
                .refresh(RefreshMode.RESET);
        realmForm.clear();
        
        trustManagerTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, "elytron-trust-manager")))
                .refresh(RefreshMode.RESET);
        trustManagerForm.clear();
        trustStoreTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(payload, "elytron-trust-store")))
                .refresh(RefreshMode.RESET);
        trustStoreForm.clear();

        vaultForm.view(failSafeGet(payload, "vault/classic"));
    }
}
