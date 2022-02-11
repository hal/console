/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

@MbuiView
@SuppressWarnings({ "HardCodedStringLiteral", "WeakerAccess", "unused", "DuplicateStringLiteralInspection" })
public abstract class SecurityView extends MbuiViewImpl<SecurityPresenter> implements SecurityPresenter.MyView {

    public static SecurityView create(final MbuiContext mbuiContext) {
        return new Mbui_SecurityView(mbuiContext);
    }

    @MbuiElement("security-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("security-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("security-elytron-key-manager-table") Table<NamedNode> keyManagerTable;
    @MbuiElement("security-elytron-key-manager-form") Form<NamedNode> keyManagerForm;
    @MbuiElement("security-elytron-key-store-table") Table<NamedNode> keyStoreTable;
    @MbuiElement("security-elytron-key-store-form") Form<NamedNode> keyStoreForm;
    @MbuiElement("security-elytron-realm-table") Table<NamedNode> realmTable;
    @MbuiElement("security-elytron-realm-form") Form<NamedNode> realmForm;
    @MbuiElement("security-elytron-trust-manager-table") Table<NamedNode> trustManagerTable;
    @MbuiElement("security-elytron-trust-manager-form") Form<NamedNode> trustManagerForm;
    @MbuiElement("security-elytron-trust-store-table") Table<NamedNode> trustStoreTable;
    @MbuiElement("security-elytron-trust-store-form") Form<NamedNode> trustStoreForm;
    @MbuiElement("security-vault-form") Form<ModelNode> vaultForm;

    SecurityView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);

        keyManagerForm.clear();
        keyStoreForm.clear();
        realmForm.clear();
        trustManagerForm.clear();
        trustStoreForm.clear();

        keyManagerTable.update(asNamedNodes(failSafePropertyList(payload, "elytron-key-manager")));
        keyStoreTable.update(asNamedNodes(failSafePropertyList(payload, "elytron-key-store")));
        realmTable.update(asNamedNodes(failSafePropertyList(payload, "elytron-realm")));
        trustManagerTable.update(asNamedNodes(failSafePropertyList(payload, "elytron-trust-manager")));
        trustStoreTable.update(asNamedNodes(failSafePropertyList(payload, "elytron-trust-store")));

        vaultForm.view(failSafeGet(payload, "vault/classic"));
    }
}
