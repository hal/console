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
package org.jboss.hal.client.configuration.subsystem.messaging;

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "unused", "WeakerAccess"})
public abstract class ServerView extends MbuiViewImpl<ServerPresenter> implements ServerPresenter.MyView {

    public static ServerView create(final MbuiContext mbuiContext) {
        return new Mbui_ServerView(mbuiContext);
    }

    @MbuiElement("messaging-server-vertical-navigation") VerticalNavigation verticalNavigation;
    @MbuiElement("messaging-server-form") Form<NamedNode> form;
    @MbuiElement("messaging-server-paging-directory-form") FailSafeForm<ModelNode> pagingDirectoryForm;
    @MbuiElement("messaging-server-bindings-directory-form") FailSafeForm<ModelNode> bindingsDirectoryForm;
    @MbuiElement("messaging-server-large-messages-directory-form") FailSafeForm<ModelNode> largeMessagesDirectoryForm;
    @MbuiElement("messaging-server-journal-directory-form") FailSafeForm<ModelNode> journalDirectoryForm;

    ServerView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        form.getFormItem("journal-datasource")
                .registerSuggestHandler(
                        new ReadChildrenAutoComplete(mbuiContext.dispatcher(), mbuiContext.statementContext(),
                                AddressTemplate.of("/{selected.profile}/subsystem=datasources/data-source=*")));
        pagingDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        bindingsDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        largeMessagesDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        journalDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
    }

    @Override
    public void update(final NamedNode server) {
        form.view(server);
        pagingDirectoryForm.view(failSafeGet(server, "path/paging-directory"));
        bindingsDirectoryForm.view(failSafeGet(server, "path/bindings-directory"));
        largeMessagesDirectoryForm.view(failSafeGet(server, "path/large-messages-directory"));
        journalDirectoryForm.view(failSafeGet(server, "path/journal-directory"));
    }
}
