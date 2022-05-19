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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.EXPRESSION_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENCRYPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVERS;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Ids.EMPTY;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.TABLE;

class ExpressionEncryptionElement implements IsElement<HTMLElement>, Attachable, HasPresenter<OtherSettingsPresenter> {

    private final HTMLElement header;
    private final HTMLElement description;
    private final EmptyState emptyState;
    private final Form<ModelNode> attributes;
    private final Table<NamedNode> resolversTable;
    private final HTMLElement subHeader;
    private final HTMLElement subDescription;
    private final Form<NamedNode> resolversAttributes;
    private final HTMLElement root;
    private OtherSettingsPresenter presenter;

    private final Metadata metadata;
    private final Resources resources;

    private final StaticAutoComplete defaultResolverAutoComplete;

    private LabelBuilder labelBuilder = new LabelBuilder();
    private final String RESOLVER_TYPE = labelBuilder.label("resolver");

    ExpressionEncryptionElement(Metadata metadata, Resources resources) {
        this.metadata = metadata;
        this.resources = resources;

        EmptyState.Builder emptyStateBuilder = new EmptyState.Builder(
                id(FORM, EMPTY),
                resources.constants().noResource());

        if (metadata.getSecurityContext().isWritable()) {
            emptyStateBuilder.primaryAction(resources.constants().add(), this::launchAddExpression,
                    Constraint.executable(metadata.getTemplate(), ADD))
                    .description(resources.messages().noResource());
        } else {
            emptyStateBuilder.description(resources.constants().restricted());
        }
        emptyState = emptyStateBuilder.build();

        attributes = new ModelNodeForm.Builder<>(id(FORM), metadata)
                .exclude(RESOLVERS)
                .dontVerifyExcludes()
                .onSave(((__, changedValues) -> presenter.saveExpressionEncryption(changedValues)))
                .build();

        defaultResolverAutoComplete = new StaticAutoComplete(Collections.emptyList());
        attributes.getFormItem("default-resolver").registerSuggestHandler(defaultResolverAutoComplete);

        Metadata resolversMetadata = metadata.forComplexAttribute(RESOLVERS);

        Constraint constraint = Constraint.writable(EXPRESSION_TEMPLATE, CONSTANT_HEADERS);
        resolversTable = new ModelNodeTable.Builder<NamedNode>(id(RESOLVERS, TABLE), resolversMetadata)
                .button(resources.constants().add(), t -> addResolver(resolversMetadata), constraint)
                .button(resources.constants().remove(), t -> removeResolver(t.selectedRow()), Scope.SELECTED, constraint)
                .columns(NAME, CREDENTIAL_STORE, "secret-key")
                .build();

        resolversAttributes = new ModelNodeForm.Builder<NamedNode>(id(RESOLVERS, FORM), resolversMetadata)
                .onSave(((form, changedValues) -> saveResolver(form)))
                .build();

        root = section()
                .add(header = h(1).textContent(Names.EXPRESSION).element())
                .add(description = p().textContent(metadata.getDescription().getDescription()).element())
                .addAll(emptyState, attributes)
                .add(subHeader = h(2).textContent(Names.RESOLVERS).element())
                .add(subDescription = p().textContent(resolversMetadata.getDescription().getDescription()).element())
                .addAll(resolversTable, resolversAttributes)
                .element();
    }

    private String id(String... ids) {
        return Ids.build(Ids.ELYTRON_EXPRESSION, ids);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        resolversTable.attach();
        attributes.attach();
        resolversAttributes.attach();
        resolversTable.bindForm(resolversAttributes);
    }

    @Override
    public void setPresenter(OtherSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(ModelNode model) {
        attributes.clear();
        resolversAttributes.clear();
        boolean isDefined = model.has(ENCRYPTION);

        setVisible(emptyState.element(), !isDefined);
        setVisible(header, isDefined);
        setVisible(description, isDefined);
        setVisible(attributes.element(), isDefined);
        setVisible(subHeader, isDefined);
        setVisible(subDescription, isDefined);
        setVisible(resolversTable.element(), isDefined);
        setVisible(resolversAttributes.element(), isDefined);

        if (isDefined) {
            ModelNode encryption = model.get(ENCRYPTION);
            attributes.view(encryption);
            List<NamedNode> resolvers = encryption.get(RESOLVERS)
                    .asList().stream().map(NamedNode::new)
                    .collect(Collectors.toList());
            storeIndex(resolvers);
            resolversTable.update(resolvers);
            List<String> resolverNames = encryption.get(RESOLVERS)
                    .asList().stream().map(r -> r.get(NAME).asString())
                    .collect(Collectors.toList());
            defaultResolverAutoComplete.update(resolverNames);
        }
    }

    private void launchAddExpression() {
        AddExpressionWizard wizard = new AddExpressionWizard(presenter, metadata, resources);
        wizard.show();
    }

    private void addResolver(Metadata metadata) {
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(Ids.RESOLVERS, Ids.ADD), metadata)
                .addOnly()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(RESOLVER_TYPE), form,
                (name, model) -> {
                    if (model != null) {
                        SafeHtml message = resources.messages().addSuccess(RESOLVER_TYPE, name, labelBuilder.label(RESOLVERS));
                        presenter.addResolver(model, message);
                    }
                });
        dialog.show();
    }

    private void saveResolver(Form<NamedNode> form) {
        int index = form.getModel().get(HAL_INDEX).asInt();
        String name = form.getModel().getName();
        SafeHtml successMessage = resources.messages().modifyResourceSuccess(RESOLVER_TYPE, name);
        presenter.saveResolver(index, form.getModel(), successMessage);
    }

    private void removeResolver(ModelNode row) {
        int index = row.get(HAL_INDEX).asInt();
        String name = row.get(NAME).asString();
        SafeHtml question = resources.messages().removeConfirmationQuestion(name);
        SafeHtml success = resources.messages().removeResourceSuccess(RESOLVER_TYPE, name);

        DialogFactory.showConfirmation(resources.messages().removeConfirmationTitle(RESOLVER_TYPE), question,
                () -> presenter.removeResolver(index, success));
    }
}
