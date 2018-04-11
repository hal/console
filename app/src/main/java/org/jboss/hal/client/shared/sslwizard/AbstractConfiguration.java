/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.shared.sslwizard;

import com.google.gwt.core.client.GWT;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHRElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURE_PORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURE_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRUST_MANAGER;
import static org.jboss.hal.meta.security.SecurityContext.RWX;
import static org.jboss.hal.resources.Ids.MANAGEMENT;

public class AbstractConfiguration extends WizardStep<EnableSSLContext, EnableSSLState> {

    public static final AddressTemplate SOCKET_BINDING_GROUP_TEMPLATE = AddressTemplate.of(
            "/socket-binding-group=*/socket-binding=*");

    static final String KEY_STORE_NAME = "key-store-name";
    static final String KEY_STORE_PASSWORD = "key-store-password";
    static final String KEY_STORE_PATH = "key-store-path";
    static final String KEY_STORE_RELATIVE_TO = "key-store-relative-to";
    static final String KEY_STORE_TYPE = "key-store-type";
    static final String PRIVATE_KEY_ALIAS = "key-alias";
    static final String PRIVATE_KEY_ALGORITHM = "key-algorithm";
    static final String PRIVATE_KEY_VALIDITY = "key-validity";
    static final String PRIVATE_KEY_DN_C = "key-dn-country";
    static final String PRIVATE_KEY_DN_ST = "key-dn-state";
    static final String PRIVATE_KEY_DN_L = "key-dn-locality";
    static final String PRIVATE_KEY_DN_O = "key-dn-organization";
    static final String PRIVATE_KEY_DN_OU = "key-dn-organizational-unit";
    static final String PRIVATE_KEY_DN_CN = "key-dn-common-name";
    static final String TRUST_STORE_NAME = "trust-store-name";
    static final String TRUST_STORE_PASSWORD = "trust-store-password";
    static final String TRUST_STORE_PATH = "trust-store-path";
    static final String TRUST_STORE_RELATIVE_TO = "trust-store-relative-to";
    static final String TRUST_STORE_TYPE = "trust-store-type";
    static final String CLIENT_CERTIFICATE_PATH = "client-certificate-path";
    static final String CLIENT_CERTIFICATE_ALIAS = "client-certificate-alias";
    static final String CLIENT_CERTIFICATE_TRUST = "client-certificate-trust-cacerts";
    static final String CLIENT_CERTIFICATE_VALIDATE = "client-certificate-validate";
    private static final EnableSSLResources RESOURCES = GWT.create(EnableSSLResources.class);

    protected ResourceDescription description;
    final Form<ModelNode> form;
    private boolean editMode;

    AbstractConfiguration(String title, Environment environment, boolean editMode, boolean undertowHttps,
            AddressTemplate template) {
        super(title);
        this.editMode = editMode;

        String mode = editMode ? "edit" : "read";
        String id = Ids.build(MANAGEMENT, "enable-ssl", mode, Ids.FORM);
        description = StaticResourceDescription.from(RESOURCES.enableSslWizard());
        // the button to enable/disable ssl has a contraint to write-attribute to ssl-context
        // then, even as the metadata has RWX permission, it can only be created when he has permission to the above constraint
        Metadata metadata = new Metadata(template, () -> RWX, new ResourceDescription(description),
                new Capabilities(environment));
        ModelNodeForm.Builder builder = new ModelNodeForm.Builder<>(id, metadata)
                .unsorted();

        if (!undertowHttps) {
            if (environment.isStandalone()) {
                builder.exclude(SECURE_PORT);
            } else {
                builder.exclude(SECURE_SOCKET_BINDING);
            }
        } else {
            builder.exclude(SECURE_SOCKET_BINDING);
            builder.exclude(SECURE_PORT);
        }

        form = builder.build();
        registerAttachable(form);
    }

    @Override
    public HTMLElement asElement() {
        return form.asElement();
    }

    @Override
    protected void onShow(final EnableSSLContext context) {
        if (context.strategy == EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS) {
            show(KEY_STORE);
            show(KEY_STORE_PASSWORD);

            hide(KEY_STORE_NAME);
            hide(KEY_STORE_PATH);
            hide(KEY_STORE_RELATIVE_TO);
            hide(KEY_STORE_TYPE);
            hide(PRIVATE_KEY_ALIAS);
            hide(PRIVATE_KEY_ALGORITHM);
            hide(PRIVATE_KEY_DN_CN);
            hide(PRIVATE_KEY_DN_OU);
            hide(PRIVATE_KEY_DN_O);
            hide(PRIVATE_KEY_DN_L);
            hide(PRIVATE_KEY_DN_ST);
            hide(PRIVATE_KEY_DN_C);
            hide(PRIVATE_KEY_VALIDITY);

        } else if (context.strategy == EnableSSLContext.Strategy.KEYSTORE_FILE_EXISTS) {
            show(KEY_STORE_NAME);
            show(KEY_STORE_PASSWORD);
            show(KEY_STORE_PATH);
            show(KEY_STORE_RELATIVE_TO);
            show(KEY_STORE_TYPE);

            hide(KEY_STORE);
            hide(PRIVATE_KEY_ALIAS);
            hide(PRIVATE_KEY_ALGORITHM);
            hide(PRIVATE_KEY_DN_CN);
            hide(PRIVATE_KEY_DN_OU);
            hide(PRIVATE_KEY_DN_O);
            hide(PRIVATE_KEY_DN_L);
            hide(PRIVATE_KEY_DN_ST);
            hide(PRIVATE_KEY_DN_C);
            hide(PRIVATE_KEY_VALIDITY);
        } else {
            hide(KEY_STORE);

            show(KEY_STORE_NAME);
            show(KEY_STORE_PASSWORD);
            show(KEY_STORE_PATH);
            show(KEY_STORE_RELATIVE_TO);
            show(KEY_STORE_TYPE);
            show(PRIVATE_KEY_ALIAS);
            show(PRIVATE_KEY_ALGORITHM);
            show(PRIVATE_KEY_DN_CN);
            show(PRIVATE_KEY_DN_OU);
            show(PRIVATE_KEY_DN_O);
            show(PRIVATE_KEY_DN_L);
            show(PRIVATE_KEY_DN_ST);
            show(PRIVATE_KEY_DN_C);
            show(PRIVATE_KEY_VALIDITY);
        }

        if (context.mutualAuthentication) {
            show(CLIENT_CERTIFICATE_ALIAS);
            show(CLIENT_CERTIFICATE_PATH);
            show(TRUST_STORE_NAME);
            show(TRUST_STORE_PASSWORD);
            show(TRUST_STORE_PATH);
            show(TRUST_STORE_RELATIVE_TO);
            show(TRUST_STORE_TYPE);
            show(CLIENT_CERTIFICATE_TRUST);
            show(CLIENT_CERTIFICATE_VALIDATE);
            show(TRUST_MANAGER);
        } else {
            hide(CLIENT_CERTIFICATE_ALIAS);
            hide(CLIENT_CERTIFICATE_PATH);
            hide(TRUST_STORE_NAME);
            hide(TRUST_STORE_PASSWORD);
            hide(TRUST_STORE_PATH);
            hide(TRUST_STORE_RELATIVE_TO);
            hide(TRUST_STORE_TYPE);
            hide(CLIENT_CERTIFICATE_TRUST);
            hide(CLIENT_CERTIFICATE_VALIDATE);
            hide(TRUST_MANAGER);
        }
    }

    // show the form item element and the <hr> form item separator (read-only mode)
    private void show(String name) {
        FormItem item = form.getFormItem(name);
        Form.State state = Form.State.READONLY;
        if (editMode) {
            state = Form.State.EDITING;
        }
        HTMLElement itemElem = item.asElement(state);
        Elements.setVisible(itemElem, true);
        if (!editMode) {
            // for read-only mode, there are the <hr> separators, we should hide it too
            Element hrElem = itemElem.nextElementSibling;
            if (hrElem instanceof HTMLHRElement) {
                HTMLHRElement hre = (HTMLHRElement) hrElem;
                Elements.setVisible(hre, true);
            }
        }
    }

    // hide the form item element and the <hr> form item separator (read-only mode)
    private void hide(String name) {
        FormItem item = form.getFormItem(name);
        Form.State state = Form.State.READONLY;
        if (editMode) {
            state = Form.State.EDITING;
        }
        HTMLElement formItemElement = item.asElement(state);
        Elements.setVisible(formItemElement, false);
        if (!editMode) {
            // for read-only mode, there are the <hr> separators, we should hide it too
            Element separatorElement = formItemElement.nextElementSibling;
            if (separatorElement instanceof HTMLHRElement) {
                HTMLHRElement hre = (HTMLHRElement) separatorElement;
                Elements.setVisible(hre, false);
            }
        }
    }

    @Override
    protected boolean onBack(EnableSSLContext context) {
        if (editMode) {
            form.cancel();
        }
        return true;
    }

    @Override
    protected boolean onCancel(EnableSSLContext context) {
        if (editMode) {
            form.cancel();
        }
        return true;
    }

    native String randomString() /*-{
        return Math.random().toString(36).substring(2, 8);
    }-*/;
}
