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
package org.jboss.hal.client.accesscontrol;

import javax.inject.Inject;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.URLItem;
import org.jboss.hal.config.keycloak.Keycloak;
import org.jboss.hal.config.keycloak.KeycloakHolder;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.USER;

public class AccessControlSsoView extends HalViewImpl implements AccessControlSsoPresenter.MyView {

    private AccessControlSsoPresenter presenter;
    private final Alert warning;
    private Form<ModelNode> form;

    @Inject
    public AccessControlSsoView(AccessControl accessControl, Resources resources, KeycloakHolder keycloakHolder) {
        this.warning = new Alert(Icons.WARNING, resources.messages().simpleProviderWarning(),
                resources.constants().enableRbac(),
                event -> {
                    accessControl.switchProvider();
                    presenter.onReset();
                });

        Keycloak.UserProfile userProfile = keycloakHolder.getKeycloak().userProfile;
        TextBoxItem user = new TextBoxItem(USER);
        if (userProfile.firstName != null) {
            String userValue = userProfile.firstName + " " + userProfile.lastName + " <" + userProfile.email + ">";
            user.setValue(userValue);
        }
        TextBoxItem userName = new TextBoxItem(USERNAME);
        userName.setValue(userProfile.username);
        URLItem authServerUrl = new URLItem(KEYCLOAK_SERVER_URL);
        TextBoxItem realm = new TextBoxItem(REALM);
        URLItem accountUrl = new URLItem("account-url");
        accountUrl.setValue(keycloakHolder.getKeycloak().createAccountUrl());
        PreTextItem realmPublicKey = new PreTextItem(REALM_PUBLIC_KEY);

        form = new ModelNodeForm.Builder<>(Ids.build(KEYCLOAK, FORM), Metadata.empty())
                .readOnly()
                .unboundFormItem(userName)
                .unboundFormItem(user)
                .unboundFormItem(authServerUrl)
                .unboundFormItem(accountUrl)
                .unboundFormItem(realm)
                .unboundFormItem(realmPublicKey)
                .build();

        registerAttachable(form);

        HTMLElement layout = div()
                .add(h(1).textContent(Names.ACCESS_CONTROL))
                .add(p().textContent(resources.messages().accessControlSsoDescription()))
                .add(warning)
                .add(form).element();

        initElement(row()
                .add(column()
                        .addAll(layout)));
    }

    @Override
    public void setPresenter(AccessControlSsoPresenter presenter) {
        this.presenter = presenter;
    }

    private void toggleWarningVisible() {
        Elements.setVisible(warning.element(), presenter.getEnvironment().getAccessControlProvider() == SIMPLE);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(ModelNode payload) {
        form.getFormItem(KEYCLOAK_SERVER_URL).setValue(payload.get(KEYCLOAK_SERVER_URL).asString());
        form.getFormItem(REALM).setValue(payload.get(REALM).asString());
        form.getFormItem(REALM_PUBLIC_KEY).setValue(payload.get(REALM_PUBLIC_KEY).asString());
        toggleWarningVisible();
    }

}
