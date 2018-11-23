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
package org.jboss.hal.client.configuration.subsystem.microprofile;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.InputType.radio;
import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.CONFIG_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.resources.UIConstants.NAME;
import static org.jboss.hal.resources.UIConstants.VALUE;

class AddConfigSourceWizard {

    private final MicroProfileConfigPresenter presenter;
    private final Resources resources;
    private final Wizard<Context, State> wizard;

    AddConfigSourceWizard(MicroProfileConfigPresenter presenter,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        this.presenter = presenter;
        this.resources = resources;
        String title = resources.messages().addResourceTitle(Names.CONFIG_SOURCE);
        this.wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(State.SOURCE, new SourceStep(metadataRegistry))
                .addStep(State.CLASS_INPUT, new ClassInputStep(resources))
                .addStep(State.DIR_INPUT, new DirInputStep(resources))
                .addStep(State.PROPERTIES_INPUT, new PropertiesInputStep(resources))
                .addStep(State.ORDINAL, new OrdinalStep())

                .onBack((context, state) -> {
                    State previous = null;
                    switch (state) {
                        case SOURCE:
                            break;
                        case CLASS_INPUT:
                        case DIR_INPUT:
                        case PROPERTIES_INPUT:
                            previous = State.SOURCE;
                            break;
                        case ORDINAL:
                            switch (context.source) {
                                case CLASS:
                                    previous = State.CLASS_INPUT;
                                    break;
                                case DIR:
                                    previous = State.DIR_INPUT;
                                    break;
                                case PROPERTIES:
                                    previous = State.PROPERTIES_INPUT;
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    return previous;
                })
                .onNext((context, state) -> {
                    State next = null;
                    switch (state) {
                        case SOURCE:
                            switch (context.source) {
                                case CLASS:
                                    next = State.CLASS_INPUT;
                                    break;
                                case DIR:
                                    next = State.DIR_INPUT;
                                    break;
                                case PROPERTIES:
                                    next = State.PROPERTIES_INPUT;
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case CLASS_INPUT:
                        case DIR_INPUT:
                        case PROPERTIES_INPUT:
                            next = State.ORDINAL;
                            break;
                        case ORDINAL:
                            break;
                        default:
                            break;
                    }
                    return next;
                })
                .onFinish((w, context) -> {
                    ResourceAddress address = CONFIG_SOURCE_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, ADD).payload(context.modelNode).build();
                    dispatcher.execute(operation,
                            modelNode -> success(context.name),
                            (op, failure) -> w.showError(resources.constants().operationFailed(),
                                    resources.messages().addResourceError(context.name, failure)),
                            (op, exception) -> w.showError(resources.constants().operationFailed(),
                                    resources.messages().addResourceError(context.name, exception.getMessage())));
                })
                .stayOpenAfterFinish()
                .build();
    }

    void show() {
        wizard.show();
    }

    private void success(String name) {
        wizard.showSuccess(resources.constants().operationSuccessful(),
                resources.messages().addResourceSuccess(Names.CONFIG_SOURCE, name),
                ctx -> presenter.reload());
    }


    // ------------------------------------------------------ context and state


    static class Context {

        Source source;
        String name;
        ModelNode modelNode;
    }


    enum State {
        SOURCE, CLASS_INPUT, DIR_INPUT, PROPERTIES_INPUT, ORDINAL
    }


    enum Source {
        CLASS, DIR, PROPERTIES
    }


    // ------------------------------------------------------ steps


    static class SourceStep extends WizardStep<Context, State> {

        private final HTMLElement root;

        SourceStep(MetadataRegistry metadataRegistry) {
            super(Names.CONFIG_SOURCE);

            Metadata metadata = metadataRegistry.lookup(CONFIG_SOURCE_TEMPLATE);
            Property operation = metadata.getDescription().findOperation(ADD);
            String description = "";
            if (operation != null) {
                description = operation.getValue().get(DESCRIPTION).asString();
            }

            root = div()
                    .add(p().textContent(description))
                    .add(div().css(CSS.radio)
                            .add(label()
                                    .add(input(radio)
                                            .attr(NAME, "source") //NON-NLS
                                            .attr(VALUE, Source.CLASS.name().toLowerCase())
                                            .on(click, event -> wizard().getContext().source = Source.CLASS))
                                    .add(span().textContent("Class")))
                            .add(label()
                                    .add(input(radio)
                                            .attr(NAME, "source") //NON-NLS
                                            .attr(VALUE, Source.DIR.name().toLowerCase())
                                            .on(click, event -> wizard().getContext().source = Source.DIR))
                                    .add(span().textContent("Directory")))
                            .add(label()
                                    .add(input(radio)
                                            .attr(NAME, "source") //NON-NLS
                                            .attr(VALUE, Source.PROPERTIES.name().toLowerCase())
                                            .on(click, event -> wizard().getContext().source = Source.PROPERTIES))
                                    .add(span().textContent("Directory"))))
                    .asElement();
        }

        @Override
        public HTMLElement asElement() {
            return root;
        }
    }


    static class ClassInputStep extends WizardStep<Context, State> {

        ClassInputStep(Resources resources) {
            super(resources.constants().attributes());
        }

        @Override
        public HTMLElement asElement() {
            return null;
        }
    }


    static class DirInputStep extends WizardStep<Context, State> {

        DirInputStep(Resources resources) {
            super(resources.constants().attributes());
        }

        @Override
        public HTMLElement asElement() {
            return null;
        }
    }


    static class PropertiesInputStep extends WizardStep<Context, State> {

        PropertiesInputStep(Resources resources) {
            super(resources.constants().attributes());
        }

        @Override
        public HTMLElement asElement() {
            return null;
        }
    }


    static class OrdinalStep extends WizardStep<Context, State> {

        OrdinalStep() {
            super(new LabelBuilder().label(ModelDescriptionConstants.ORDINAL));
        }

        @Override
        public HTMLElement asElement() {
            return null;
        }
    }
}