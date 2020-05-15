/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.microprofile;

import java.util.LinkedHashMap;
import java.util.Map;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.RadioItem;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.CONFIG_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.UIConstants.NAME;

class AddConfigSourceWizard {

    private final MicroProfileConfigPresenter presenter;
    private final Resources resources;
    private final Wizard<Context, State> wizard;

    AddConfigSourceWizard(MicroProfileConfigPresenter presenter,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        String title = resources.messages().addResourceTitle(Names.CONFIG_SOURCE);
        Metadata metadata = metadataRegistry.lookup(CONFIG_SOURCE_TEMPLATE);

        this.presenter = presenter;
        this.resources = resources;
        this.wizard = new Wizard.Builder<Context, State>(title, new Context())

                .addStep(State.SOURCE, new SourceStep(metadata, resources))
                .addStep(State.INPUT, new InputStep(metadata, resources))
                .addStep(State.ORDINAL, new OrdinalStep(metadata))

                .onBack((context, state) -> {
                    State previous = null;
                    switch (state) {
                        case SOURCE:
                            break;
                        case INPUT:
                            previous = State.SOURCE;
                            break;
                        case ORDINAL:
                            previous = State.INPUT;
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
                            next = State.INPUT;
                            break;
                        case INPUT:
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
                    // In the context node everything is flat. We need to copy it into the right structure
                    ModelNode payload = new ModelNode();
                    if (CLASS.equals(context.source)) {
                        payload.get(CLASS).get(NAME).set(context.modelNode.get(NAME));
                        payload.get(CLASS).get(MODULE).set(context.modelNode.get(MODULE));
                    } else if (DIR.equals(context.source)) {
                        payload.get(DIR).get(PATH).set(context.modelNode.get(PATH));
                        if (context.modelNode.hasDefined(RELATIVE_TO)) {
                            payload.get(DIR).get(RELATIVE_TO).set(context.modelNode.get(RELATIVE_TO));
                        }
                    } else if (PROPERTIES.equals(context.source)) {
                        payload.get(PROPERTIES).set(context.modelNode.get(PROPERTIES));
                    }
                    if (context.modelNode.hasDefined(ORDINAL)) {
                        payload.get(ORDINAL).set(context.modelNode.get(ORDINAL));
                    }

                    ResourceAddress address = CONFIG_SOURCE_TEMPLATE.resolve(statementContext, context.name);
                    Operation operation = new Operation.Builder(address, ADD).payload(payload).build();
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

        String name = "";
        String source = CLASS;
        ModelNode modelNode = new ModelNode();
    }


    enum State {
        SOURCE, INPUT, ORDINAL
    }


    // ------------------------------------------------------ steps


    class SourceStep extends WizardStep<Context, State> {

        private final HTMLElement root;
        private final NameItem nameItem;
        private final RadioItem sourceItem;
        private final ModelNodeForm<ModelNode> form;

        SourceStep(Metadata metadata, Resources resources) {
            super(Names.CONFIG_SOURCE);

            String id = Ids.build(Ids.MICRO_PROFILE_CONFIG_SOURCE, SOURCE, Ids.FORM);
            Metadata emptyMetadata = Metadata.empty();
            nameItem = new NameItem();
            sourceItem = new RadioItem(SOURCE, resources.constants().source(),
                    asList(new String[]{CLASS, DIR, PROPERTIES}), false);
            sourceItem.setName(SOURCE);
            form = new ModelNodeForm.Builder<>(id, emptyMetadata)
                    .unboundFormItem(nameItem, 0)
                    .unboundFormItem(sourceItem, 1)
                    .build();
            registerAttachable(form);

            Property operation = metadata.getDescription().findOperation(ADD);
            String description = "";
            if (operation != null) {
                description = operation.getValue().get(DESCRIPTION).asString();
            }

            root = div()
                    .add(p().textContent(description))
                    .add(form).element();
        }

        @Override
        public HTMLElement element() {
            return root;
        }

        @Override
        protected void onShow(Context context) {
            nameItem.setValue(context.name);
            sourceItem.setValue(context.source);
            form.edit(context.modelNode);
        }

        @Override
        protected boolean onNext(Context context) {
            context.name = nameItem.getValue();
            context.source = sourceItem.getValue();
            return form.save();
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


    class InputStep extends WizardStep<Context, State> {

        private final Map<String, HTMLElement> descriptions;
        private final Map<String, Form<ModelNode>> forms;
        private final HTMLElement root;

        @SuppressWarnings("unchecked")
        InputStep(Metadata configSourceMeta, Resources resources) {
            super(resources.constants().attributes());
            descriptions = new LinkedHashMap<>();
            forms = new LinkedHashMap<>();

            String id, attribute;
            Metadata metadata;
            HTMLElement description;
            Form<ModelNode> form;

            // class
            attribute = CLASS;
            id = Ids.build(Ids.MICRO_PROFILE_CONFIG_SOURCE, attribute, Ids.FORM);
            metadata = configSourceMeta.forComplexAttribute(attribute);
            description = p().textContent(metadata.getDescription().getDescription()).element();
            form = new ModelNodeForm.Builder<>(id, metadata)
                    .unsorted()
                    .include(NAME, MODULE)
                    .onSave((f, changedValues) -> changedValues.forEach((name, value) -> {
                        if (value != null) {
                            wizard.getContext().modelNode.get(name).set((String) value);
                        }
                    }))
                    .build();
            descriptions.put(attribute, description);
            forms.put(attribute, form);

            // dir
            attribute = DIR;
            id = Ids.build(Ids.MICRO_PROFILE_CONFIG_SOURCE, attribute, Ids.FORM);
            metadata = configSourceMeta.forComplexAttribute(attribute);
            description = p().textContent(metadata.getDescription().getDescription()).element();
            form = new ModelNodeForm.Builder<>(id, metadata)
                    .unsorted()
                    .include(PATH, RELATIVE_TO)
                    .onSave((f, changedValues) -> changedValues.forEach((name, value) -> {
                        if (value != null) {
                            wizard.getContext().modelNode.get(name).set((String) value);
                        }
                    }))
                    .build();
            form.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
            descriptions.put(attribute, description);
            forms.put(attribute, form);

            // properties
            attribute = PROPERTIES;
            id = Ids.build(Ids.MICRO_PROFILE_CONFIG_SOURCE, attribute, Ids.FORM);
            metadata = configSourceMeta;
            description = p().textContent(metadata.getDescription()
                    .findAttribute(ATTRIBUTES, PROPERTIES)
                    .getValue()
                    .get(DESCRIPTION)
                    .asString()).element();
            form = new ModelNodeForm.Builder<>(id, configSourceMeta)
                    .include(attribute)
                    .onSave((f, changedValues) -> {
                        Map<String, String> properties = (Map<String, String>) changedValues.get(PROPERTIES);
                        if (properties != null) {
                            properties.forEach((key, value) ->
                                    wizard.getContext().modelNode.get(PROPERTIES).get(key).set(value));
                        }
                    })
                    .build();
            form.getFormItem(PROPERTIES).setRequired(true);
            descriptions.put(attribute, description);
            forms.put(attribute, form);

            HtmlContentBuilder<HTMLDivElement> builder = div();
            for (HTMLElement d : descriptions.values()) {
                builder.add(d);
                setVisible(d, false);
            }
            for (Form<ModelNode> f : forms.values()) {
                builder.add(f);
                registerAttachable(f);
                setVisible(f.element(), false);
            }
            root = builder.element();
        }

        @Override
        public HTMLElement element() {
            return root;
        }

        @Override
        protected void onShow(Context context) {
            descriptions.forEach((attribute, description) -> setVisible(description, context.source.equals(attribute)));
            forms.forEach((attribute, form) -> setVisible(form.element(), context.source.equals(attribute)));
            forms.get(context.source).edit(context.modelNode);
        }

        @Override
        protected boolean onNext(Context context) {
            return forms.get(context.source).save();
        }

        @Override
        protected boolean onBack(Context context) {
            forms.get(context.source).cancel();
            return true;
        }

        @Override
        protected boolean onCancel(Context context) {
            forms.get(context.source).cancel();
            return true;
        }
    }


    class OrdinalStep extends WizardStep<Context, State> {

        private final ModelNodeForm<ModelNode> form;
        private final HTMLElement root;

        OrdinalStep(Metadata metadata) {
            super(new LabelBuilder().label(ORDINAL));

            String id = Ids.build(Ids.MICRO_PROFILE_CONFIG_SOURCE, ORDINAL, Ids.FORM);
            form = new ModelNodeForm.Builder<>(id, metadata)
                    .addOnly()
                    .include(ORDINAL)
                    .onSave((f, changedValues) -> {
                        if (changedValues.containsKey(ORDINAL) && changedValues.get(ORDINAL) != null) {
                            wizard.getContext().modelNode.get(ORDINAL).set((long) changedValues.get(ORDINAL));
                        }
                    })
                    .build();
            registerAttachable(form);

            String attributeDescription = metadata.getDescription()
                    .findAttribute(ATTRIBUTES, ORDINAL)
                    .getValue()
                    .get(DESCRIPTION)
                    .asString();
            root = div()
                    .add(p().textContent(attributeDescription))
                    .add(form).element();
        }

        @Override
        public HTMLElement element() {
            return root;
        }

        @Override
        protected void onShow(Context context) {
            form.edit(context.modelNode);
        }

        @Override
        protected boolean onNext(Context context) {
            return form.save();
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
}