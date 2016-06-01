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
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelDescriptionConstants;
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
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter.MAIL_SESSION_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.mail.MailSessionPresenter.MAIL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

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
            final Places places,
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
                        MailSession::new);
                callback.onSuccess(mailSessions);
            });
        });

        addColumnAction(columnActionFactory.add(IdBuilder.build(Ids.MAIL_SESSION, ADD), Names.MAIL_SESSION,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(MailSessionPresenter.MAIL_SESSION_TEMPLATE);
                    AddResourceDialog dialog = new AddResourceDialog(IdBuilder.build(Ids.MAIL_SESSION, ADD, "form"),
                            resources.messages().addResourceTitle(Names.MAIL_SESSION), metadata,
                            Arrays.asList(ModelDescriptionConstants.JNDI_NAME, "from", "debug"), //NON-NLS
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    ResourceAddress address = MailSessionPresenter.MAIL_SESSION_TEMPLATE
                                            .resolve(statementContext, name);
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
            public Element asElement() {
                if (!mailSession.getServers().isEmpty()) {
                    return new Elements.Builder()
                            .span().css(itemText)
                            .span().textContent(mailSession.getName()).end()
                            .start("small").css(subtitle).textContent(Joiner.on(", ").join(mailSession.getServers()))
                            .end()
                            .end().build();
                }
                return null;
            }

            @Override
            public String getFilterData() {
                List<String> data = new ArrayList<>();
                data.add(mailSession.getName());
                data.addAll(mailSession.getServers());
                return Joiner.on(' ').join(data);
            }

            @Override
            public List<ItemAction<MailSession>> actions() {
                List<ItemAction<MailSession>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.MAIL_SESSION)
                        .with(NAME, mailSession.getName()).build()));
                actions.add(itemActionFactory
                        .remove(Names.MAIL_SESSION, mailSession.getName(), MailSessionPresenter.MAIL_SESSION_TEMPLATE,
                                MailSessionColumn.this));
                return actions;
            }
        });

        setPreviewCallback(mailSession -> new MailSessionPreview(mailSession, resources));
    }
}
