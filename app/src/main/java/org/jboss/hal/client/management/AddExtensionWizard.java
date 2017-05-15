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
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import elemental.html.InputElement;
import elemental.json.JsonObject;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.AsyncStep;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.ballroom.wizard.WorkflowCallback;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.extension.InstalledExtensionResources;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.hal.client.management.AddExtensionWizard.State.REVIEW;
import static org.jboss.hal.client.management.AddExtensionWizard.State.URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class AddExtensionWizard {

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

        private static final String URL_INPUT = "urlInput";

        private final ExtensionRegistry extensionRegistry;
        private final InputElement urlInput;
        private final Element root;

        UrlStep(ExtensionRegistry extensionRegistry) {
            super(Names.URL);
            this.extensionRegistry = extensionRegistry;

            // @formatter:off
            String id = Ids.uniqueId();
            Elements.Builder builder = new Elements.Builder()
                .div().css(form, formHorizontal, editing)
                    .div().css(formGroup)
                        .label().css(controlLabel, halFormLabel).attr(UIConstants.FOR, id)
                            .textContent(Names.URL)
                        .end()
                        .div().css(halFormInput)
                            .input(InputType.url).id(id).css(formControl).rememberAs(URL_INPUT)
                        .end()
                    .end()
                .end();
            // @formatter:on

            urlInput = builder.referenceFor(URL_INPUT);
            root = builder.build();
        }

        @Override
        public Element asElement() {
            return root;
        }

        @Override
        public void reset(final Context context) {
            urlInput.setValue("");
        }

        @Override
        protected void onShow(final Context context) {
            urlInput.setValue(context.url);
        }

        @Override
        public void onNext(final Context context, final WorkflowCallback callback) {
            context.url = urlInput.getValue();
            wizard().showProgress("Processing extension metadata", SafeHtmlUtils.fromString("Processing..."));

            extensionRegistry.ping(context.url, (status, json) -> {
                if (status >= 200 && status < 400) {
                    if (isValid(json)) {
                        context.extension = InstalledExtension.fromJson(context.url, json);
                        callback.proceed();
                    } else {
                        wizard().showError("Invalid metadata", SafeHtmlUtils.fromString("Invalid metadata"), false);
                    }

                } else if (status == 415) {
                    wizard().showError("No JSON", SafeHtmlUtils.fromString("No JSON"), false);

                } else if (status == 500) {
                    wizard().showError("Error parsing JSON", SafeHtmlUtils.fromString("Error parsing JSON"), false);

                } else if (status == 503) {
                    wizard().showError("Communication Error", SafeHtmlUtils.fromString("Communication Error"), false);

                } else {
                    wizard().showError("Unknown Error", SafeHtmlUtils.fromString("Unknown Error"), false);
                }
            });
        }

        private boolean isValid(JsonObject json) {
            return json.hasKey(NAME) && json.hasKey(SCRIPT) && json.hasKey(EXTENSION_POINT);
        }
    }


    static class ReviewStep extends WizardStep<Context, State> {

        private static final InstalledExtensionResources RESOURCES = GWT.create(InstalledExtensionResources.class);
        private final Form<InstalledExtension> form;

        ReviewStep(final Resources resources) {
            super(resources.constants().review());
            Metadata metadata = Metadata.staticDescription(RESOURCES.installedExtension());
            form = new ModelNodeForm.Builder<InstalledExtension>(Ids.EXTENSION_REVIEW_FORM, metadata)
                    .include(NAME, VERSION, DESCRIPTION, SCRIPT, STYLESHEETS, EXTENSION_POINT, AUTHOR, LICENSE,
                            HOMEPAGE)
                    .unsorted()
                    .readOnly()
                    .build();
            registerAttachable(form);
        }

        @Override
        public Element asElement() {
            return form.asElement();
        }

        @Override
        public void reset(final Context context) {
            form.clear();
        }

        @Override
        protected void onShow(final Context context) {
            form.view(context.extension);
        }
    }


    // ------------------------------------------------------ wizard delegate

    private final Wizard<Context, State> wizard;

    AddExtensionWizard(final ExtensionColumn column,
            final ExtensionRegistry extensionRegistry,
            final ExtensionStorage extensionStorage,
            final Resources resources) {

        String title = resources.messages().addResourceTitle(Names.EXTENSION);
        wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(URL, new UrlStep(extensionRegistry))
                .addStep(REVIEW, new ReviewStep(resources))

                .onBack((context, currentState) -> currentState == REVIEW ? URL : null)
                .onNext((context, currentState) -> currentState == URL ? REVIEW : null)

                .onFinish((wzd, context) -> {
                    extensionStorage.add(context.extension);
                    extensionRegistry.inject(context.extension.getFqScript(), context.extension.getFqStylesheets());
                    column.refresh(context.extension.getName());
                })
                .build();
    }

    public void show() {wizard.show();}
}
