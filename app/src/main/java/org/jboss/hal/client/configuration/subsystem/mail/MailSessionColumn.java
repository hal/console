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
package org.jboss.hal.client.configuration.subsystem.mail;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter.MAIL_SESSION_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter.MAIL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda
 */
@AsyncColumn(MAIL_SESSION)
@Requires(MAIL_SESSION_ADDRESS)
public class MailSessionColumn extends FinderColumn<MailSession> {

    @Inject
    protected MailSessionColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final @Footer Provider<Progress> progress,
            final Resources resources) {

        super(new Builder<MailSession>(finder, MAIL_SESSION, Names.MAIL_SESSION)
                .withFilter()
                .useFirstActionAsBreadcrumbHandler());

        setItemsProvider((context, callback) -> {
            ResourceAddress mailAddress = MAIL_TEMPLATE.resolve(statementContext);
            Operation op = new Operation.Builder(READ_RESOURCE_OPERATION, mailAddress)
                    .param(RECURSIVE, true).build();
            
            dispatcher.execute(op, result -> {
                
                List<MailSession> mailSessions = Lists.transform(result.get(MAIL_SESSION).asPropertyList(),
                        property -> new MailSession(property));
                
                callback.onSuccess(mailSessions);
            });
        });


        addColumnAction(columnActionFactory.add(IdBuilder.build(Ids.MAIL_SESSION, ADD), Names.MAIL_SESSION,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(MailSessionPresenter.MAIL_SESSION_TEMPLATE);
                    Form<ModelNode> form = new ModelNodeForm.Builder<>(
                            IdBuilder.build(Ids.MAIL_SESSION, ADD, "form"), metadata)
                            .addFromRequestProperties()
                            .unboundFormItem(new NameItem(), 0)
                            .include("jndi-name", "from", "debug")
                            .unsorted()
                            .build();
                    AddResourceDialog dialog = new AddResourceDialog(
                            resources.messages().addResourceTitle(Names.MAIL_SESSION), form,
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    ResourceAddress address = MailSessionPresenter.MAIL_SESSION_TEMPLATE.resolve(statementContext, name);
                                    Operation operation = new Operation.Builder(ADD, address)
                                            .param(MAIL_SESSION, name)
                                            .payload(modelNode)
                                            .build();
                                    dispatcher.execute(operation, result -> {
                                        MessageEvent.fire(eventBus,
                                                Message.success(resources.messages()
                                                        .addResourceSuccess(Names.MAIL_SESSION, name)));
                                        column.refresh(name);
                                    });
                                }
                            });
                    dialog.show();
                }));

        addColumnAction(columnActionFactory.refresh(IdBuilder.build(MAIL_SESSION, "refresh")));

        setItemRenderer(mailSession -> new ItemDisplay<MailSession>() {

            @Override
            public String getTitle() {
                return mailSession.getName();
            }

            @Override
            public List<ItemAction<MailSession>> actions() {
                String profile = environment.isStandalone() ? STANDALONE : statementContext.selectedProfile();
                PlaceRequest.Builder builder = new PlaceRequest.Builder()
                        .nameToken(NameTokens.MAIL_SESSION)
                        .with(PROFILE, profile)
                        .with(NAME, mailSession.getName());
                
                List<ItemAction<MailSession>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(builder.build()));
                actions.add(itemActionFactory.remove(Names.MAIL_SESSION, mailSession.getName(), MailSessionPresenter.MAIL_SESSION_TEMPLATE,
                        MailSessionColumn.this));
                return actions;
            }
        });

        setPreviewCallback(mailSession -> new MailSessionPreview(mailSession, resources));
        
    }

    private void launchNewMailSessionWizard() {
        
    }
}
