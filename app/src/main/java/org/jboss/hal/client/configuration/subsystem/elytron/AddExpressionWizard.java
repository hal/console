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
package org.jboss.hal.client.configuration.subsystem.elytron;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.resources.Ids.ADD;
import static org.jboss.hal.resources.Ids.ELYTRON_EXPRESSION;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.RESOLVERS;

class AddExpressionWizard {

    private final Wizard<Context, State> wizard;

    AddExpressionWizard(OtherSettingsPresenter presenter,
            Metadata metadata,
            Resources resources) {
        String title = resources.messages().addResourceTitle(Names.EXPRESSION);

        this.wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(State.EXPRESSION, ExpressionStep.build(metadata))
                .addStep(State.RESOLVERS, ResolverStep.build(metadata.forComplexAttribute(ModelDescriptionConstants.RESOLVERS)))

                .onBack((context, state) -> {
                    State previous = null;
                    if (state == State.RESOLVERS) {
                        previous = State.EXPRESSION;
                    }
                    return previous;
                })
                .onNext((context, state) -> {
                    State next = null;
                    if (state == State.EXPRESSION) {
                        next = State.RESOLVERS;
                    }
                    return next;
                })
                .onFinish((w, context) -> {
                    ModelNode payload = context.modelNode;
                    payload.get(ModelDescriptionConstants.RESOLVERS).add(context.resolvers);

                    presenter.addExpressionEncryption(payload);
                })
                .build();
    }

    void show() {
        wizard.show();
    }

    // ------------------------------------------------------ context and state

    static class Context {
        ModelNode modelNode = new ModelNode();
        ModelNode resolvers = new ModelNode();
    }

    enum State {
        EXPRESSION, RESOLVERS
    }

    // ------------------------------------------------------ steps

    abstract static class AbstractStep extends WizardStep<Context, State> {
        protected final HTMLElement root;
        protected final Form<ModelNode> form;

        protected AbstractStep(String title, Form<ModelNode> form, Metadata metadata) {
            super(title);
            this.form = form;

            registerAttachable(form);

            root = div()
                    .add(p().textContent(metadata.getDescription().getDescription()))
                    .add(form).element();
        }

        @Override
        public HTMLElement element() {
            return root;
        }

        @Override
        protected void onShow(Context context) {
            form.edit(new ModelNode());
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
    }

    static class ExpressionStep extends AbstractStep {

        private ExpressionStep(String title, Form<ModelNode> form, Metadata metadata) {
            super(title, form, metadata);
        }

        public static ExpressionStep build(Metadata metadata) {
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ELYTRON_EXPRESSION, ADD, FORM), metadata)
                    .exclude(ModelDescriptionConstants.RESOLVERS)
                    .dontVerifyExcludes()
                    .build();

            return new ExpressionStep(Names.EXPRESSION, form, metadata);
        }

        @Override
        protected boolean onNext(Context context) {
            context.modelNode = form.getModel();
            return form.save();
        }
    }

    static class ResolverStep extends AbstractStep {

        private ResolverStep(String title, Form<ModelNode> form, Metadata metadata) {
            super(title, form, metadata);
        }

        public static ResolverStep build(Metadata resolversMetadata) {
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(ELYTRON_EXPRESSION, RESOLVERS, ADD, FORM),
                    resolversMetadata)
                            .build();

            return new ResolverStep(Names.RESOLVERS, form, resolversMetadata);
        }

        @Override
        protected boolean onNext(Context context) {
            context.resolvers = form.getModel();
            return form.save();
        }
    }
}
