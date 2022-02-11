/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.client.runtime.BrowseByColumn;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOGGING_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@Column(Ids.LOG_FILE)
@Requires(value = { LOGGING_SUBSYSTEM_ADDRESS, LOG_FILE_ADDRESS }, recursive = false)
public class LogFileColumn extends FinderColumn<LogFile> {

    @Inject
    public LogFileColumn(Finder finder,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            LogFiles logFiles,
            Resources resources) {
        super(new Builder<LogFile>(finder, Ids.LOG_FILE, resources.constants().logFile())

                .columnAction(columnActionFactory.refresh(Ids.LOG_FILE_REFRESH))
                .itemsProvider((context, callback) -> {
                    Composite composite = new Composite();
                    ResourceAddress address = AddressTemplates.LOGGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
                    Operation readLogsOp = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, LOG_FILE)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    composite.add(readLogsOp);

                    Operation readLogProfilesOp = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                            .param(CHILD_TYPE, LOGGING_PROFILE)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE_DEPTH, 1)
                            .build();
                    composite.add(readLogProfilesOp);

                    dispatcher.execute(composite, (CompositeResult result) -> {
                        List<LogFile> logs = result.step(0).get(RESULT).asList().stream()
                                .map(node -> {
                                    Property prop = node.asProperty();
                                    return new LogFile(prop.getName(), prop.getValue());
                                })
                                .collect(toList());

                        for (ModelNode logNode : result.step(1).get(RESULT).asList()) {
                            Property prop = logNode.asProperty();
                            String logProfile = prop.getName();
                            if (prop.getValue().hasDefined(LOG_FILE)) {
                                List<LogFile> logsProfs = prop.getValue().get(LOG_FILE).asList().stream()
                                        .map(node -> {
                                            Property prop2 = node.asProperty();
                                            return new LogFile(prop2.getName(), logProfile, prop2.getValue());
                                        })
                                        .collect(toList());
                                logs.addAll(logsProfs);
                            }
                        }

                        logs.sort(Comparator.comparing(LogFile::getFilename));
                        callback.onSuccess(logs);
                    });
                })
                .itemRenderer(item -> new ItemDisplay<LogFile>() {
                    @Override
                    public String getTitle() {
                        return item.getFilename();
                    }

                    @Override
                    public HTMLElement element() {
                        if (item.getLoggingProfile() != null) {
                            return ItemDisplay
                                    .withSubtitle(item.getFilename(), item.getLoggingProfile());
                        }
                        return null;
                    }

                    @Override
                    public String getFilterData() {
                        return String.join(" ", item.getFilename(), item.getFormattedLastModifiedDate());
                    }

                    @Override
                    public String getTooltip() {
                        return item.getFormattedSize();
                    }

                    @Override
                    public List<ItemAction<LogFile>> actions() {
                        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(NameTokens.LOG_FILE);
                        if (BrowseByColumn.browseByServerGroups(finder.getContext())) {
                            builder.with(SERVER_GROUP, statementContext.selectedServerGroup());
                        } else {
                            builder.with(HOST, statementContext.selectedHost());
                        }
                        builder.with(SERVER, statementContext.selectedServer())
                                .with(NAME, item.getFilename());
                        if (item.getLoggingProfile() != null) {
                            builder.with(LOGGING_PROFILE, item.getLoggingProfile());
                        }

                        ItemAction<LogFile> download = new ItemAction.Builder<LogFile>()
                                .title(resources.constants().download())
                                .href(logFiles.downloadUrl(item.getFilename(), item.getLoggingProfile()),
                                        UIConstants.DOWNLOAD, item.getFilename())
                                .build();
                        ItemAction<LogFile> external = new ItemAction.Builder<LogFile>()
                                .title(resources.constants().openInExternalWindow())
                                .href(logFiles.externalUrl(item.getFilename(), item.getLoggingProfile()),
                                        UIConstants.TARGET, logFiles.target(item.getFilename()))
                                .build();

                        return asList(itemActionFactory.view(builder.build()), download, external);
                    }
                })
                .onPreview(item -> new LogFilePreview(logFiles, item, resources))
                .pinnable()
                .showCount()
                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
                .filterDescription(resources.messages().logfileColumnFilterDescription()));
    }

}
