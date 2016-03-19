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
package org.jboss.hal.core.finder;

import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;

import javax.inject.Inject;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTIION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Provides methods to create common column actions.
 *
 * @author Harald Pehl
 */
public class ColumnActionFactory {

    private class ColumnAddResourceCallback<T> implements AddResourceDialog.Callback<ModelNode> {

        private final FinderColumn<T> column;
        private final String type;
        private final AddressTemplate template;

        ColumnAddResourceCallback(final FinderColumn<T> column, final String type,
                final AddressTemplate template) {
            this.column = column;
            this.type = type;
            this.template = template;
        }

        @Override
        public void onAdd(final String name, final ModelNode model) {
            ResourceAddress address = template.resolve(statementContext, name);
            Operation operation = new Operation.Builder(ADD, address)
                    .payload(model)
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().addResourceSuccess(type, name)));
                column.refresh(name);
            });
        }
    }


    private final MetadataRegistry metadataRegistry;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public ColumnActionFactory(final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    /**
     * Returns a column action which opens an add-resource-dialog for the given resource type. The dialog contains
     * fields for all required request properties. When clicking "Add", a new resource is added using the specified
     * address template.
     */
    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template) {
        return add(id, type, template, null);
    }

    public <T> ColumnAction<T> add(String id, String type, AddressTemplate template,
            @NonNls final String firstAttribute, @NonNls final String... otherAttributes) {

        return add(id, column -> {
            Metadata metadata = metadataRegistry.lookup(template);
            ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(
                    IdBuilder.build(id, "add", "form"), metadata)
                    .createResource()
                    .unboundFormItem(new NameItem(), 0);
            if (firstAttribute != null) {
                builder.include(firstAttribute, otherAttributes);
            }
            AddResourceDialog<ModelNode> dialog = new AddResourceDialog<>(
                    resources.messages().addResourceTitle(type), builder.build(),
                    new ColumnAddResourceCallback<>(column, type, template));
            dialog.show();
        });
    }

    public <T> ColumnAction<T> add(String id, ColumnActionHandler<T> handler) {
        Element element = new Elements.Builder().span().css(pfIcon("add-circle-o")).end().build();
        return new ColumnAction<>(id, element, handler);
    }

    public <T> ColumnAction<T> refresh(String id) {
        Element element = new Elements.Builder().span().css(fontAwesome(CSS.refresh)).end().build();
        return new ColumnAction<>(id, element, column -> column.refresh(RESTORE_SELECTIION));
    }
}
