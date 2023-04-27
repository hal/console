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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.base.Strings;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTHENTICATION_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTION_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PASSWORD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USER_NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.move;

class ConnectionStep extends WizardStep<Context, State> {

    private final ModelNodeForm<ModelNode> form;

    private List<String> credRefAttrs = asList(STORE, ALIAS, CLEAR_TEXT, TYPE);
    private List<String> otherAttrs = asList(USER_NAME, PASSWORD, AUTHENTICATION_CONTEXT);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    ConnectionStep(Metadata metadata, Resources resources, boolean xa) {
        super(Names.CONNECTION);

        List<String> attributes = new ArrayList<>();
        if (!xa) {
            attributes.add(CONNECTION_URL);
        }
        attributes.addAll(otherAttrs);
        attributes.addAll(credRefAttrs);

        // split credential reference into individual attributes
        Metadata connMetadata = metadata.forComplexAttribute(CREDENTIAL_REFERENCE, true);
        if (!xa) {
            metadata.copyAttribute(CONNECTION_URL, connMetadata);
        }
        for (String attr : otherAttrs) {
            metadata.copyAttribute(attr, connMetadata);
        }

        form = new ModelNodeForm.Builder<>(Ids.DATA_SOURCE_CONNECTION_FORM, connMetadata)
                .include(attributes)
                .unsorted()
                .onSave((form, changedValues) -> {
                    changedValues.forEach((k, v) -> {
                        // record changes as long as it isn't related to cred-ref
                        if (!credRefAttrs.contains(k)) {
                            wizard().getContext().recordChange(k, v);
                        }
                    });
                })
                .build();

        form.addFormValidation(new CredentialReference.AlternativeValidation(PASSWORD, () -> {
            // supply credential reference but do not alter model
            ModelNode credRef = new ModelNode();
            for (String credRefAttr : credRefAttrs) {
                String value = form.<String> getFormItem(credRefAttr).getValue();
                if (!Strings.isNullOrEmpty(value)) {
                    credRef.get(credRefAttr).set(value);
                }
            }
            return credRef;
        }, resources));

        registerAttachable(form);
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    protected void onShow(Context context) {
        ModelNode connection = new ModelNode();
        if (context.dataSource.hasDefined(CONNECTION_URL)) {
            copyAttr(CONNECTION_URL, context.dataSource, connection);
        }
        if (context.dataSource.hasDefined(CREDENTIAL_REFERENCE)) {
            ModelNode credRef = context.dataSource.get(CREDENTIAL_REFERENCE);
            for (String attr : credRefAttrs) {
                copyAttr(attr, credRef, connection);
            }
        }
        for (String attr : otherAttrs) {
            if (context.dataSource.hasDefined(attr)) {
                copyAttr(attr, context.dataSource, connection);
            }
        }

        form.edit(connection);
    }

    @Override
    protected boolean onNext(Context context) {
        boolean valid = form.save();
        if (valid) {
            ModelNode dataSource = form.getModel();
            // re-create credential reference
            for (String credRefAttr : credRefAttrs) {
                move(dataSource, credRefAttr, CREDENTIAL_REFERENCE + "/" + credRefAttr);
            }

            if (dataSource.has(CONNECTION_URL)) {
                copyAttr(CONNECTION_URL, dataSource, context.dataSource);
            }

            List<String> attrs = new ArrayList<>(otherAttrs);
            attrs.add(CREDENTIAL_REFERENCE);

            for (String attr : attrs) {
                context.dataSource.remove(attr);
                if (dataSource.hasDefined(attr)) {
                    copyAttr(attr, dataSource, context.dataSource);
                }
            }
        }
        return valid;
    }

    @Override
    protected boolean onBack(Context context) {
        form.cancel();
        return true;
    }

    @Override
    protected boolean onCancel(Context context) {
        form.cancel();
        return true;
    }

    private void copyAttr(String name, ModelNode from, ModelNode to) {
        to.get(name).set(from.get(name));
    }
}
