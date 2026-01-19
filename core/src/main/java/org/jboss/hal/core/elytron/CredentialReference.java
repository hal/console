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
package org.jboss.hal.core.elytron;

import java.util.function.Supplier;

import javax.inject.Inject;

import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.UIConstants.SHORT_TIMEOUT;

/**
 * Provides building blocks for dealing with the {@code credential-reference} complex attribute used in several resources across
 * subsystems.
 */
public class CredentialReference {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final ComplexAttributeOperations ca;
    private final Resources resources;

    public static final String[] ATTRIBUTES = new String[] {
            STORE,
            ALIAS,
            TYPE,
            CLEAR_TEXT
    };

    private static final String DOT = ".";
    private static final String STORE_PREFIXED = CREDENTIAL_REFERENCE + DOT + STORE;
    private static final String ALIAS_PREFIXED = CREDENTIAL_REFERENCE + DOT + ALIAS;
    private static final String TYPE_PREFIXED = CREDENTIAL_REFERENCE + DOT + TYPE;
    private static final String CLEAR_TEXT_PREFIXED = CREDENTIAL_REFERENCE + DOT + CLEAR_TEXT;

    public static final String[] ATTRIBUTES_PREFIXED = new String[] {
            STORE_PREFIXED,
            ALIAS_PREFIXED,
            TYPE_PREFIXED,
            CLEAR_TEXT_PREFIXED
    };

    @Inject
    public CredentialReference(EventBus eventBus, Dispatcher dispatcher, ComplexAttributeOperations ca,
            Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.ca = ca;
        this.resources = resources;
    }

    /**
     * Creates a form for the {@code credential-reference} complex attribute of a resource. The form is setup as a singleton
     * form to add, save, reset and remove the complex attribute.
     *
     * @param baseId base ID used for the generated form and add resource dialog
     * @param metadata the metadata of the resource which contains the {@code credential-reference} attribute
     * @param alternativeName the name of the alternative attribute
     * @param alternativeValue the value of the alternative attribute
     * @param address the fully qualified address of the resource used for the CRUD actions
     * @param callback the callback executed after the {@code credential-reference} attributes has been added, saved, reset or
     *        removed
     */
    public Form<ModelNode> form(String baseId, Metadata metadata, String alternativeName,
            Supplier<String> alternativeValue, Supplier<ResourceAddress> address, Callback callback) {

        return form(baseId, metadata, CREDENTIAL_REFERENCE, alternativeName, alternativeValue, null, address, null, callback);
    }

    /**
     * @see CredentialReference#form(String, Metadata, String, String, Supplier, Supplier, Supplier, Callback, Callback)
     */

    public Form<ModelNode> form(String baseId, Metadata metadata, String crName, String alternativeName,
            Supplier<String> alternativeValue, Supplier<ResourceAddress> address, Callback callback) {
        return form(baseId, metadata, crName, alternativeName, alternativeValue, null, address, null, callback);
    }

    /**
     * Creates a form for the {@code credential-reference} complex attribute of a resource. The form is setup as a singleton
     * form to add, save, reset and remove the complex attribute.
     *
     * @param baseId base ID used for the generated form and add resource dialog
     * @param metadata the metadata of the resource which contains the {@code credential-reference} attribute
     * @param crName the name of the credential-reference complex attribute
     * @param alternativeName the name of the alternative attribute
     * @param alternativeValue the value of the alternative attribute
     * @param ping the operation to check the presence of the credential reference
     * @param address the fully qualified address of the resource used for the CRUD actions
     * @param emptyAction the action to perform to add a credential reference from an empty state
     * @param callback the callback executed after the {@code credential-reference} attributes has been added, saved, reset or
     *        removed
     */
    public Form<ModelNode> form(String baseId, Metadata metadata, String crName, String alternativeName,
            Supplier<String> alternativeValue, Supplier<Operation> ping, Supplier<ResourceAddress> address,
            Callback emptyAction, Callback callback) {

        String credentialReferenceName = crName == null ? CREDENTIAL_REFERENCE : crName;
        Metadata crMetadata = metadata.forComplexAttribute(credentialReferenceName);

        EmptyState.Builder emptyStateBuilder = new EmptyState.Builder(
                Ids.build(baseId, credentialReferenceName, Ids.FORM, Ids.EMPTY),
                resources.constants().noResource());

        Callback defaultEmptyAction = () -> {
            if (alternativeName != null && alternativeValue != null &&
                    !Strings.isNullOrEmpty(alternativeValue.get())) {
                String alternativeLabel = new LabelBuilder().label(alternativeName);
                DialogFactory.showConfirmation(
                        resources.messages().addResourceTitle(Names.CREDENTIAL_REFERENCE),
                        resources.messages().credentialReferenceAddConfirmation(alternativeLabel),
                        () -> setTimeout(
                                o -> addCredentialReference(baseId, crMetadata, credentialReferenceName,
                                        alternativeName,
                                        address, callback),
                                SHORT_TIMEOUT));
            } else {
                addCredentialReference(baseId, crMetadata, credentialReferenceName, null, address,
                        callback);
            }
        };

        if (crMetadata.getSecurityContext().isWritable()) {
            emptyStateBuilder.primaryAction(resources.constants().add(), emptyAction == null ? defaultEmptyAction : emptyAction,
                    Constraint.executable(metadata.getTemplate(), ADD))
                    .description(resources.messages().noResource());
        } else {
            emptyStateBuilder.description(resources.constants().restricted());
        }
        EmptyState noCredentialReference = emptyStateBuilder.build();

        Supplier<Operation> defaultPing = () -> {
            ResourceAddress fqAddress = address.get();
            Operation operation = null;
            if (fqAddress != null && crMetadata.getSecurityContext().isReadable()) {
                operation = new Operation.Builder(address.get(), READ_ATTRIBUTE_OPERATION)
                        .param(NAME, credentialReferenceName).build();
            }
            return operation;
        };

        ModelNodeForm.Builder<ModelNode> formBuilder = new ModelNodeForm.Builder<>(
                Ids.build(baseId, credentialReferenceName, Ids.FORM), crMetadata)
                .include(ATTRIBUTES)
                .unsorted()
                .singleton(ping == null ? defaultPing : ping, noCredentialReference)
                .onSave(((f, changedValues) -> {
                    ResourceAddress fqa = address.get();
                    if (fqa != null) {
                        if (changedValues.isEmpty()) {
                            MessageEvent.fire(eventBus, Message.warning(resources.messages().noChanges()));
                            callback.execute();
                        } else {
                            ca.save(credentialReferenceName, Names.CREDENTIAL_REFERENCE, fqa, f.getModel(), callback);
                        }
                    } else {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().credentialReferenceAddressError()));
                    }
                }));

        // some credential-reference attributes are nillable=false, so only nillable=true may be removed
        if (crMetadata.getDescription().get(NILLABLE).asBoolean()) {
            formBuilder.prepareRemove(f -> {
                ResourceAddress fqAddress = address.get();
                if (fqAddress != null) {
                    ca.remove(credentialReferenceName, Names.CREDENTIAL_REFERENCE, fqAddress,
                            new Form.FinishRemove<ModelNode>(f) {
                                @Override
                                public void afterRemove(Form<ModelNode> form) {
                                    callback.execute();
                                }
                            });
                } else {
                    MessageEvent.fire(eventBus,
                            Message.error(resources.messages().credentialReferenceAddressError()));
                }
            });

        }

        Form<ModelNode> form = formBuilder.build();
        form.addFormValidation(new CrFormValuesValidation(resources));
        return form;
    }

    private void addCredentialReference(String baseId, Metadata crMetadata,
            String credentialReferenceName, String alternativeName,
            Supplier<ResourceAddress> address, Callback callback) {
        ResourceAddress fqAddress = address.get();
        if (fqAddress != null) {
            String id = Ids.build(baseId, credentialReferenceName, Ids.ADD);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, crMetadata)
                    .addOnly()
                    .include(STORE, ALIAS, CLEAR_TEXT, TYPE)
                    .unsorted()
                    .build();
            form.addFormValidation(new CrFormValuesValidation(resources));

            new AddResourceDialog(resources.messages().addResourceTitle(Names.CREDENTIAL_REFERENCE),
                    form, (name, model) -> {
                        if (alternativeName != null) {
                            Operation undefine = new Operation.Builder(fqAddress, UNDEFINE_ATTRIBUTE_OPERATION)
                                    .param(NAME, alternativeName)
                                    .build();
                            Operation write = new Operation.Builder(fqAddress, WRITE_ATTRIBUTE_OPERATION)
                                    .param(NAME, credentialReferenceName)
                                    .param(VALUE, model)
                                    .build();
                            dispatcher.execute(new Composite(undefine, write), (CompositeResult result) -> {
                                MessageEvent.fire(eventBus, Message.success(
                                        resources.messages().addSingleResourceSuccess(Names.CREDENTIAL_REFERENCE)));
                                callback.execute();
                            });

                        } else {
                            ca.add(credentialReferenceName, Names.CREDENTIAL_REFERENCE, fqAddress, model, callback);
                        }
                    }).show();

        } else {
            MessageEvent.fire(eventBus,
                    Message.error(resources.messages().credentialReferenceAddressError()));
        }
    }

    /**
     * Form validation which validates that only one of {@code credential-reference} and {@code <alternativeName>} is given.
     */
    public static class AlternativeValidation<T extends ModelNode> implements FormValidation<T> {

        private final String alternativeName;
        private final Supplier<ModelNode> credentialReferenceValue;
        private final Resources resources;

        public AlternativeValidation(String alternativeName,
                Supplier<ModelNode> credentialReferenceValue, Resources resources) {
            this.alternativeName = alternativeName;
            this.credentialReferenceValue = credentialReferenceValue;
            this.resources = resources;
        }

        @Override
        public ValidationResult validate(Form<T> form) {
            FormItem<String> formItem = form.getFormItem(alternativeName);
            if (formItem != null && !Strings.isNullOrEmpty(formItem.getValue()) && credentialReferenceValue.get()
                    .isDefined()) {
                formItem.showError(resources.messages()
                        .credentialReferenceValidationError(new LabelBuilder().label(alternativeName)));
                return ValidationResult.invalid(resources.messages().credentialReferenceConflict());
            }
            return ValidationResult.OK;
        }
    }

    /**
     * When adding or updating the credential-reference, we need to follow the following rules:
     * <ul>
     * <li>either 'clear-text' must be specified on its own, or</li>
     * <li>'store' needs to be specified with at least one of
     * <ul>
     * <li>'clear-text' or</li>
     * <li>'alias'</li>
     * </ul>
     * </li>
     * </ul>
     * See also https://docs.wildfly.org/25/WildFly_Elytron_Security.html#automatic-updates-of-credential-stores
     */
    public static class CrFormValuesValidation implements FormValidation<ModelNode> {

        private final Resources resources;
        private final boolean prefixed;
        private final String customName;

        public CrFormValuesValidation(Resources resources) {
            this(resources, false);
        }

        public CrFormValuesValidation(Resources resources, boolean prefixed) {
            this.resources = resources;
            this.prefixed = prefixed;
            this.customName = null;
        }

        public CrFormValuesValidation(Resources resources, String customName) {
            this.resources = resources;
            this.prefixed = false;
            this.customName = customName;
        }

        @Override
        public ValidationResult validate(Form<ModelNode> form) {
            FormItem<Object> storeItem = form
                    .getFormItem(prefixed ? STORE_PREFIXED : (customName != null ? customName + DOT + STORE : STORE));
            FormItem<Object> aliasItem = form
                    .getFormItem(prefixed ? ALIAS_PREFIXED : (customName != null ? customName + DOT + ALIAS : ALIAS));
            FormItem<Object> clearTextItem = form.getFormItem(
                    prefixed ? CLEAR_TEXT_PREFIXED : (customName != null ? customName + DOT + CLEAR_TEXT : CLEAR_TEXT));
            if (!clearTextItem.isEmpty() && storeItem.isEmpty() && aliasItem.isEmpty()) {
                // clear-text only not recommended mode
                return ValidationResult.OK;
            } else if (!storeItem.isEmpty() && (!clearTextItem.isEmpty() || !aliasItem.isEmpty())) {
                // store and alias, clear-text or both
                return ValidationResult.OK;
            } else {
                storeItem.showError(resources.messages().credentialReferenceInvalidCombination());
                aliasItem.showError(resources.messages().credentialReferenceInvalidCombination());
                clearTextItem.showError(resources.messages().credentialReferenceInvalidCombination());
                return ValidationResult.invalid(resources.messages().credentialReferenceValidationErrorValues());
            }
        }
    }
}
