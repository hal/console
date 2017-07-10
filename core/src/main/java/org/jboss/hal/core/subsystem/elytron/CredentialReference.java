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
package org.jboss.hal.core.subsystem.elytron;

import java.util.function.Supplier;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Provides building blocks for dealing with the {@code credential-reference} complex attribute used in several
 * resources across subsystems.
 */
// TODO Add a way to validate the alternatives between 'credential-reference' and other attributes *across* forms
public class CredentialReference {

    private static class MainFormValidation<T extends ModelNode> implements FormValidation<T> {

        private final Form<T> mainForm;
        private final Form<ModelNode> crForm;
        private final Resources resources;
        private final String crAlternative;

        private MainFormValidation(Form<T> mainForm, Form<ModelNode> crForm, String crAlternative,
                Resources resources) {
            this.mainForm = mainForm;
            this.crForm = crForm;
            this.resources = resources;
            this.crAlternative = crAlternative;
        }

        @Override
        public ValidationResult validate(Form<T> form) {
            return CredentialReference.validate(mainForm, crForm, crAlternative, resources);
        }
    }


    private static class CrFormValidation<T extends ModelNode> implements FormValidation<ModelNode> {

        private final Form<T> mainForm;
        private final Form<ModelNode> crForm;
        private final Resources resources;
        private final String crAlternative;

        private CrFormValidation(Form<T> mainForm, Form<ModelNode> crForm, String crAlternative, Resources resources) {
            this.mainForm = mainForm;
            this.crForm = crForm;
            this.resources = resources;
            this.crAlternative = crAlternative;
        }

        @Override
        public ValidationResult validate(Form<ModelNode> form) {
            return CredentialReference.validate(mainForm, crForm, crAlternative, resources);
        }
    }


    private static <T extends ModelNode> ValidationResult validate(Form<T> mainForm, Form<ModelNode> crForm,
            String crAlternative, Resources resources) {
        String crAlternativeValue = null;
        FormItem<Object> formItem = mainForm.getFormItem(crAlternative);
        if (formItem != null) {
            crAlternativeValue = String.valueOf(formItem.getValue());
        }
        if (!Strings.isNullOrEmpty(crAlternativeValue) && crDefined(crForm)) {
            ValidationResult.invalid(
                    resources.messages().credentialReferenceValidationError(new LabelBuilder().label(crAlternative)));
        }
        return ValidationResult.OK;
    }

    private static boolean crDefined(Form<ModelNode> crForm) {
        ModelNode model = crForm.getModel();
        return isDefined(model, STORE) ||
                isDefined(model, ALIAS) ||
                isDefined(model, TYPE) ||
                isDefined(model, CLEAR_TEXT);
    }

    private static boolean isDefined(ModelNode model, String attribute) {
        return model.hasDefined(attribute) && !Strings.isNullOrEmpty(model.get(attribute).asString());
    }


    private final ComplexAttributeOperations ca;
    private final EventBus eventBus;
    private final Resources resources;

    @Inject
    public CredentialReference(ComplexAttributeOperations ca, EventBus eventBus, Resources resources) {
        this.ca = ca;
        this.eventBus = eventBus;
        this.resources = resources;
    }

    /**
     * Creates a form for the {@code credential-reference} complex attribute of a resource. The form is setup as a
     * singleton form to add, save, reset and remove the complex attribute.
     *
     * @param baseId   base ID used for the form and the add resource dialog
     * @param metadata the metadata of the resource which contains the {@code credential-reference} attribute
     * @param address  the fully qualified address of the resource
     * @param callback the callback executed after the {@code credential-reference} attributes has been added, save,
     *                 reset or removed
     */
    public Form<ModelNode> form(String baseId, Metadata metadata, Supplier<ResourceAddress> address,
            Callback callback) {
        Metadata crMetadata = metadata.forComplexAttribute(CREDENTIAL_REFERENCE);
        return new ModelNodeForm.Builder<>(Ids.build(baseId, CREDENTIAL_REFERENCE, Ids.FORM_SUFFIX), crMetadata)
                .singleton(
                        () -> {
                            ResourceAddress fqAddress = address.get();
                            return fqAddress != null ? new Operation.Builder(address.get(), READ_ATTRIBUTE_OPERATION)
                                    .param(NAME, CREDENTIAL_REFERENCE).build() : null;
                        },
                        () -> {
                            ResourceAddress fqAddress = address.get();
                            if (fqAddress != null) {
                                String id = Ids.build(baseId, CREDENTIAL_REFERENCE, Ids.ADD_SUFFIX);
                                Form<ModelNode> form = new ModelNodeForm.Builder<>(id, crMetadata)
                                        .addOnly()
                                        .include(STORE, ALIAS, TYPE, CLEAR_TEXT)
                                        .unsorted()
                                        .build();
                                new AddResourceDialog(resources.messages().addResourceTitle(Names.CREDENTIAL_REFERENCE),
                                        form, (name, model) -> ca.add(CREDENTIAL_REFERENCE, Names.CREDENTIAL_REFERENCE,
                                        fqAddress, model, callback)).show();
                            } else {
                                MessageEvent.fire(eventBus,
                                        Message.error(resources.messages().credentialReferenceAddressError()));
                            }
                        })
                .onSave(((form, changedValues) -> {
                    ResourceAddress fqa = address.get();
                    if (fqa != null) {
                        ca.save(CREDENTIAL_REFERENCE, Names.CREDENTIAL_REFERENCE, fqa, changedValues,
                                crMetadata, callback);
                    } else {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().credentialReferenceAddressError()));
                    }
                }))
                .prepareReset(form -> {
                    ResourceAddress faAddress = address.get();
                    if (faAddress != null) {
                        ca.reset(CREDENTIAL_REFERENCE, Names.CREDENTIAL_REFERENCE, faAddress, crMetadata, form,
                                new Form.FinishReset<ModelNode>(form) {
                                    @Override
                                    public void afterReset(Form<ModelNode> form) {
                                        callback.execute();
                                    }
                                });
                    } else {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().credentialReferenceAddressError()));
                    }
                })
                .prepareRemove(form -> {
                    ResourceAddress fqAddress = address.get();
                    if (fqAddress != null) {
                        ca.remove(CREDENTIAL_REFERENCE, Names.CREDENTIAL_REFERENCE, fqAddress,
                                new Form.FinishRemove<ModelNode>(form) {
                                    @Override
                                    public void afterRemove(Form<ModelNode> form) {
                                        callback.execute();
                                    }
                                });
                    } else {
                        MessageEvent.fire(eventBus,
                                Message.error(resources.messages().credentialReferenceAddressError()));
                    }
                })
                .build();
    }

    public <T extends ModelNode> void addValidation(Form<T> mainForm, Form<ModelNode> crForm, String crAlternative) {
        if (mainForm.getFormItem(crAlternative) != null) {
            mainForm.addFormValidation(new MainFormValidation<>(mainForm, crForm, crAlternative, resources));
            crForm.addFormValidation(new CrFormValidation<>(mainForm, crForm, crAlternative, resources));
        }
    }
}
