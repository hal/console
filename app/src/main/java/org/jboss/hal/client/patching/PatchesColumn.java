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
package org.jboss.hal.client.patching;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda
 */
@Column(Ids.PATCHES_HOST)
//@Requires(value = "/host=*/core-service=patching")
public class PatchesColumn extends FinderColumn<ModelNode> {

    static final String SELECTED_PATCHING_ADDRESS = "/{selected.host}/core-service=patching";
    static final AddressTemplate SELECTED_PATCHING_TEMPLATE = AddressTemplate.of(SELECTED_PATCHING_ADDRESS);

    @Inject
    public PatchesColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final Resources resources) {

        super(new Builder<ModelNode>(finder, Ids.PATCHES_HOST, Names.PATCHES)

                .columnAction(columnActionFactory.refresh(Ids.PATCHES_REFRESH))

                .itemsProvider((context, callback) -> {
                    ResourceAddress address = SELECTED_PATCHING_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, SHOW_HISTORY_OPERATION).build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList()));
                })
                .onPreview(PatchesPreview::new)
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<ModelNode>() {
            @Override
            public String getId() {
                return Ids.build(HOST, PATCHING, item.get(PATCH_ID).asString());
            }

            @Override
            public String getTitle() {
                return item.get(PATCH_ID).asString();
            }

            @Override
            public List<ItemAction<ModelNode>> actions() {

                List<ItemAction<ModelNode>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<ModelNode>()
                        .title(resources.constants().rollback())
                        .handler(item1 -> rollback(item1.get(PATCH_ID).asString(), metadataRegistry, resources,
                                statementContext, dispatcher,
                                eventBus))
                        .constraint(Constraint.executable(SELECTED_PATCHING_TEMPLATE, ROLLBACK_OPERATION))
                        .build());

                return actions;
            }

        });

        ColumnAction<ModelNode> applyPatchAction = new ColumnAction.Builder<ModelNode>(Ids.PATCH_ADD)
                .element(columnActionFactory.addButton(Names.PATCH))
                .handler(column -> applyPatch(metadataRegistry, resources, statementContext, dispatcher, eventBus))
                .build();
        ColumnAction<ModelNode> ageoutAction = new ColumnAction.Builder<ModelNode>(Ids.build("patch", "ageout-history"))
                .element(columnActionFactory.addButton("Clean patch history", "fa fa-eraser"))
                .handler(column -> ageoutHistory(resources, statementContext, dispatcher, eventBus))
                .build();
        addColumnAction(applyPatchAction);
        addColumnAction(ageoutAction);

    }

    private void rollback(final String patchId, final MetadataRegistry metadataRegistry, final Resources resources,
            final StatementContext statementContext, final Dispatcher dispatcher, final EventBus eventBus) {

        ModelNode model = new ModelNode();
        model.get(PATCH_ID).set(patchId);
        ResourceAddress address = SELECTED_PATCHING_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SELECTED_PATCHING_TEMPLATE);
        Metadata operationMetadata = metadata.forOperation(ROLLBACK_OPERATION);
        String id = Ids.build(Ids.HOST, CORE_SERVICE, PATCHING, patchId, ROLLBACK_OPERATION);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, operationMetadata)
                .unsorted()
                .build();
        form.getFormItem(PATCH_ID).setEnabled(false);
        Dialog dialog = new Dialog.Builder("Rollback")
                .add(form.asElement())
                .closeIcon(true)
                .closeOnEsc(true)
                .primary(resources.constants().rollback(), () -> {
                    if (form.save()) {
                        ModelNode payload = form.getModel();
                        // reset-configuration is a required attribute, if the user doesn't set it, meaning it should be false
                        // it will not be added into the payload, but we must forcibly set as false to satisfy the required=true metadata
                        if (!payload.hasDefined(RESET_CONFIGURATION)) {
                            payload.get(RESET_CONFIGURATION).set(false);
                        }
                        Operation operation = new Operation.Builder(address, ROLLBACK_OPERATION)
                                .payload(payload)
                                .build();
                        dispatcher.execute(operation, result -> {
                            MessageEvent.fire(eventBus, Message.success(SafeHtmlUtils.fromString("rollback success.")));
                            refresh(RESTORE_SELECTION);
                        });
                        return true;
                    }
                    return false;
                })
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(model);
    }

    private void ageoutHistory(final Resources resources,
            final StatementContext statementContext, final Dispatcher dispatcher,
            final EventBus eventBus) {

        ResourceAddress address = SELECTED_PATCHING_TEMPLATE.resolve(statementContext);

        DialogFactory.showConfirmation("Clean patch history",
                SafeHtmlUtils.fromString("Do you want to clean the patch history ?"), () -> {
                    Operation operation = new Operation.Builder(address, AGEOUT_HISTORY_OPERATION)
                            .build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus,
                                Message.success(SafeHtmlUtils.fromString("clean patch history success.")));
                        refresh(RESTORE_SELECTION);
                    });

                });
    }

    private void applyPatch(final MetadataRegistry metadataRegistry, final Resources resources,
            final StatementContext statementContext, final Dispatcher dispatcher,
            final EventBus eventBus) {

        // TODO
    }
}
