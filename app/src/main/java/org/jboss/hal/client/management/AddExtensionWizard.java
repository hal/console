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

import elemental.dom.Element;
import elemental.json.JsonObject;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.management.AddExtensionWizard.State.REVIEW;
import static org.jboss.hal.client.management.AddExtensionWizard.State.URL;

/**
 * @author Harald Pehl
 */
class AddExtensionWizard {

    static class Context {

        String url;
        int status;
        JsonObject extensionJson;
    }


    enum State {
        URL, REVIEW
    }


    static class UrlStep extends WizardStep<Context, State> {

        UrlStep() {
            super("URL");
        }

        @Override
        public Element asElement() {
            return null;
        }
    }


    static class ReviewStep extends WizardStep<Context, State> {

        ReviewStep(final Resources resources) {
            super(resources.constants().review());
        }

        @Override
        public Element asElement() {
            return null;
        }
    }


    private final Wizard<Context, State> wizard;

    public AddExtensionWizard(final Resources resources) {
        String title = resources.messages().addResourceTitle(Names.EXTENSION);
        wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(URL, new UrlStep())
                .addStep(REVIEW, new ReviewStep(resources))

                .onBack((context, currentState) -> currentState == REVIEW ? URL : null)
                .onNext((context, currentState) -> currentState == URL ? REVIEW : null)

                .stayOpenAfterFinish()
                .onFinish(new Wizard.FinishCallback<Context, State>() {
                    @Override
                    public void onFinish(final Wizard<Context, State> wizard, final Context context) {

                    }
                })
                .build();
    }
}
