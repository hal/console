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
package org.jboss.hal.client.management;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.extension.InstalledExtensionResources;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.js.JsonObject;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Urls;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class AddExtensionWizard {

    private final Wizard<Context, State> wizard;

    AddExtensionWizard(ExtensionColumn column,
            EventBus eventBus,
            ExtensionRegistry extensionRegistry,
            ExtensionStorage extensionStorage,
            Resources resources) {

        String title = resources.messages().addResourceTitle(Names.EXTENSION);
        wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(State.URL, new UrlStep(extensionRegistry, resources))
                .addStep(State.REVIEW, new ReviewStep(resources))

                .onBack((context, currentState) -> currentState == State.REVIEW ? State.URL : null)
                .onNext((context, currentState) -> currentState == State.URL ? State.REVIEW : null)

                .onFinish((wzd, context) -> {
                    extensionStorage.add(context.extension);
                    extensionRegistry.inject(context.extension.getFqScript(), context.extension.getFqStylesheets());
                    column.refresh(context.extension.getName());
                    MessageEvent.fire(eventBus, Message.success(
                            resources.messages().addResourceSuccess(Names.EXTENSION, context.extension.getName())));
                })
                .build();
    }

    public void show() {
        wizard.show();
    }


    // ------------------------------------------------------ context and state


    static class Context {

        String url = "";
        InstalledExtension extension;
    }


    enum State {
        URL, REVIEW
    }


    // ------------------------------------------------------ steps


    static class UrlStep extends WizardStep<Context, State> implements AsyncStep<Context> {

        private final ExtensionRegistry extensionRegistry;
        private final Resources resources;
        private final TextBoxItem urlItem;
        private final Form<ModelNode> form;

        UrlStep(ExtensionRegistry extensionRegistry, Resources resources) {
            super(Names.URL);
            this.extensionRegistry = extensionRegistry;
            this.resources = resources;

            urlItem = new TextBoxItem(ModelDescriptionConstants.URL);
            urlItem.setRequired(true);

            ((HTMLInputElement) urlItem.asElement(EDITING).querySelector("input[type=text]")).type = "url"; //NON-NLS
            form = new ModelNodeForm.Builder<>(Ids.EXTENSION_URL_FORM, Metadata.empty())
                    .unboundFormItem(urlItem, 0, resources.messages().extensionUrl())
                    .addOnly()
                    .build();
            registerAttachable(form);
        }

        @Override
        public HTMLElement asElement() {
            return form.asElement();
        }

        @Override
        public void reset(Context context) {
            form.edit(new ModelNode());
            urlItem.clearValue();
        }

        @Override
        protected void onShow(Context context) {
            urlItem.setValue(context.url);
        }

        @Override
        public void onNext(Context context, WorkflowCallback callback) {
            if (form.save()) {
                wizard().showProgress(resources.constants().extensionProcessing(),
                        resources.messages().extensionProcessing());

                context.url = urlItem.getValue();
                extensionRegistry.verifyMetadata(context.url, (status, json) -> {
                    switch (status) {
                        case 404:
                            wizard().showError(resources.constants().extensionNotFound(),
                                    resources.messages().extensionNotFound(), false);
                            break;

                        case 415:
                            wizard().showError(resources.constants().unsupportedFileType(),
                                    resources.messages().extensionNoJson(), false);
                            break;
                        case 500:
                            wizard().showError(resources.constants().invalidJson(),
                                    resources.messages().invalidExtensionJson(), false);
                            break;

                        case 503:
                            wizard().showError(resources.constants().networkError(),
                                    resources.messages().extensionNetworkError(Endpoints.getBaseUrl(), context.url),
                                    false);
                            break;

                        default: {
                            if (status >= 200 && status < 400) {
                                if (isValid(json)) {
                                    context.extension = InstalledExtension.fromJson(context.url, json);
                                    callback.proceed();
                                } else {
                                    wizard().showError(resources.constants().invalidMetadata(),
                                            resources.messages().invalidExtensionMetadata(Urls.EXTENSION_DOCUMENTATION),
                                            false);
                                }
                            } else if (status >= 400 && status < 1000) {
                                wizard().showError(resources.constants().extensionError(),
                                        resources.messages().extensionError(status),
                                        false);
                            } else {
                                wizard().showError(resources.constants().unknownError(),
                                        resources.messages().unknownError(), false);
                            }
                            break;
                        }
                    }
                });
            }
        }

        private boolean isValid(JsonObject json) {
            return json.hasKey(NAME) && json.hasKey(SCRIPT) && json.hasKey(EXTENSION_POINT);
        }
    }


    static class ReviewStep extends WizardStep<Context, State> {

        private static final InstalledExtensionResources RESOURCES = GWT.create(InstalledExtensionResources.class);
        private final Form<InstalledExtension> form;

        ReviewStep(Resources resources) {
            super(resources.constants().review());
            Metadata metadata = Metadata.staticDescription(RESOURCES.installedExtension());
            form = new ModelNodeForm.Builder<InstalledExtension>(Ids.EXTENSION_REVIEW_FORM, metadata)
                    .include(NAME, VERSION, DESCRIPTION, ModelDescriptionConstants.URL, SCRIPT, STYLESHEETS,
                            EXTENSION_POINT, AUTHOR, HOMEPAGE, LICENSE)
                    .unsorted()
                    .readOnly()
                    .build();
            registerAttachable(form);
        }

        @Override
        public HTMLElement asElement() {
            return form.asElement();
        }

        @Override
        public void reset(Context context) {
            form.clear();
        }

        @Override
        protected void onShow(Context context) {
            form.view(context.extension);
        }
    }
}
