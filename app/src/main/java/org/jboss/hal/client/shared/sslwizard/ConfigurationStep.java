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
package org.jboss.hal.client.shared.sslwizard;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ConfigurationStep extends AbstractConfiguration {

    private ModelNode model = new ModelNode();
    private Map<String, List<String>> existingResources;
    private Resources resources;

    ConfigurationStep(Map<String, List<String>> existingResources, Resources resources, Environment environment,
            boolean undertowHttps, AddressTemplate template) {
        super(resources.constants().configuration(), environment, true, undertowHttps, template);
        this.existingResources = existingResources;
        this.resources = resources;

        model.setEmptyObject();

        getFormItem(KEY_STORE_RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        getFormItem(TRUST_STORE_RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
    }

    @Override
    public void reset(EnableSSLContext context) {
        addFormFieldValidator(() -> true, KEY_MANAGER, KEY_MANAGER);
        addFormFieldValidator(() -> true, SERVER_SSL_CONTEXT, SERVER_SSL_CONTEXT);

        // call the key-store validator only if user chooses to add a key-store
        addFormFieldValidator(() -> !context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS),
                KEY_STORE, KEY_STORE_NAME);

        // call the trust-manager validator, if user chooses mutual authentication
        addFormFieldValidator(() -> context.mutualAuthentication, TRUST_MANAGER, TRUST_MANAGER);

    }

    private void addFormFieldValidator(BooleanSupplier condition, String type, String attribute) {
        form.<String>getFormItem(attribute).addValidationHandler(fieldValue -> {
            ValidationResult validationResult = ValidationResult.OK;
            boolean validate = condition.getAsBoolean() && existingResources.get(type) != null
                    && existingResources.get(type).contains(fieldValue);
            if (validate) {
                validationResult = ValidationResult.invalid(resources.messages().duplicateResource(type));
            }
            return validationResult;
        });
    }

    @Override
    protected void onShow(final EnableSSLContext context) {
        super.onShow(context);
        form.edit(model);

        if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_CREATE)) {
            // create all resources
            getFormItem(KEY_STORE_NAME).setRequired(true);
            getFormItem(KEY_STORE_NAME).setEnabled(true);
            getFormItem(KEY_STORE_PASSWORD).setRequired(true);
            getFormItem(KEY_STORE_PATH).setRequired(true);

            // if the user goes back and forward, do not require an existing key-store
            getFormItem(KEY_STORE).setRequired(false);
            getFormItem(KEY_STORE).setEnabled(false);
            getFormItem(PRIVATE_KEY_ALIAS).setRequired(true);
            getFormItem(CAA_NAME).setRequired(false);
            getFormItem(CAA_ALIAS).setRequired(false);
            getFormItem(CAA_DOMAIN_NAMES).setRequired(false);

        } else if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_FILE_EXISTS)) {

            getFormItem(KEY_STORE_NAME).setRequired(true);
            getFormItem(KEY_STORE_NAME).setEnabled(true);
            getFormItem(KEY_STORE_PASSWORD).setRequired(true);
            getFormItem(KEY_STORE_PATH).setRequired(true);

            // if the user goes back and forward, do not require an existing key-store
            getFormItem(KEY_STORE).setRequired(false);
            getFormItem(KEY_STORE).setEnabled(false);
            getFormItem(PRIVATE_KEY_ALIAS).setRequired(false);

            getFormItem(CAA_NAME).setRequired(false);
            getFormItem(CAA_ALIAS).setRequired(false);
            getFormItem(CAA_DOMAIN_NAMES).setRequired(false);

        } else if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_OBTAIN_LETSENCRYPT)) {
            getFormItem(KEY_STORE_NAME).setRequired(true);
            getFormItem(KEY_STORE_NAME).setEnabled(true);
            getFormItem(KEY_STORE_PASSWORD).setRequired(true);
            getFormItem(KEY_STORE_PATH).setRequired(true);
            getFormItem(CAA_NAME).setRequired(true);
            getFormItem(CAA_ALIAS).setRequired(true);
            getFormItem(CAA_DOMAIN_NAMES).setRequired(true);
            getFormItem(PRIVATE_KEY_ALIAS).setRequired(true);

            // if the user goes back and forward, do not require an existing key-store
            getFormItem(KEY_STORE).setRequired(false);
            getFormItem(KEY_STORE).setEnabled(false);
        } else {
            getFormItem(KEY_STORE).setRequired(true);
            getFormItem(KEY_STORE).setEnabled(true);
            getFormItem(KEY_STORE_PASSWORD).setRequired(true);

            // reuse an existing elytron key-store
            getFormItem(KEY_STORE_NAME).setEnabled(false);
            getFormItem(KEY_STORE_NAME).setRequired(false);
            getFormItem(KEY_STORE_PATH).setRequired(false);
            getFormItem(PRIVATE_KEY_ALIAS).setRequired(false);
            getFormItem(CAA_NAME).setRequired(false);
            getFormItem(CAA_ALIAS).setRequired(false);
            getFormItem(CAA_DOMAIN_NAMES).setRequired(false);
        }

        getFormItem(KEY_MANAGER).setRequired(true);
        getFormItem(SERVER_SSL_CONTEXT).setRequired(true);

        if (context.mutualAuthentication) {
            getFormItem(CLIENT_CERTIFICATE_ALIAS).setRequired(true);
            getFormItem(CLIENT_CERTIFICATE_PATH).setRequired(true);
            getFormItem(TRUST_STORE_NAME).setRequired(true);
            getFormItem(TRUST_STORE_PATH).setRequired(true);
            getFormItem(TRUST_STORE_PASSWORD).setRequired(true);
            getFormItem(TRUST_MANAGER).setRequired(true);
            getFormItem(TRUST_MANAGER).setEnabled(true);
        } else {
            getFormItem(CLIENT_CERTIFICATE_ALIAS).setRequired(false);
            getFormItem(CLIENT_CERTIFICATE_PATH).setRequired(false);
            getFormItem(TRUST_STORE_NAME).setRequired(false);
            getFormItem(TRUST_STORE_PATH).setRequired(false);
            getFormItem(TRUST_STORE_PASSWORD).setRequired(false);
            getFormItem(TRUST_MANAGER).setRequired(false);
            getFormItem(TRUST_MANAGER).setEnabled(false);
        }
    }

    private FormItem getFormItem(String name) {
        return form.getFormItem(name);
    }

    @Override
    protected boolean onNext(final EnableSSLContext context) {
        boolean valid = form.save();
        if (valid) {
            // set default values, as metadata comes from an artifical resource description (ssl-mgmt-wizard.dmr)
            description.getAttributes(ATTRIBUTES).forEach(p -> {
                FormItem formItem = getFormItem(p.getName());
                if (p.getValue().hasDefined(DEFAULT) && formItem != null && formItem.isEmpty()) {
                    model.get(p.getName()).set(p.getValue().get(DEFAULT));
                }
            });
            context.model = model;
        }
        return valid;
    }
}
