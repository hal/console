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
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.List;
import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.client.runtime.BrowseByColumn;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOGGING_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

@Column(Ids.LOG_FILE)
@Requires(value = {LOGGING_SUBSYSTEM_ADDRESS, LOG_FILE_ADDRESS}, recursive = false)
public class LogFileColumn extends FinderColumn<LogFile> {

    @Inject
    public LogFileColumn(final Finder finder,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final LogFiles logFiles,
            final Resources resources) {
        super(new Builder<LogFile>(finder, Ids.LOG_FILE, resources.constants().logFile())

                .columnAction(columnActionFactory.refresh(Ids.LOG_FILE_REFRESH))
                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(
                            AddressTemplates.LOGGING_SUBSYSTEM_TEMPLATE.resolve(statementContext),
                            "list-log-files").build(); //NON-NLS
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asList().stream()
                            .map(LogFile::new)
                            .sorted(comparing(LogFile::getFilename))
                            .collect(toList())));
                })
                .itemRenderer(item -> new ItemDisplay<LogFile>() {
                    @Override
                    public String getTitle() {
                        return item.getFilename();
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

                        ItemAction<LogFile> download = new ItemAction.Builder<LogFile>()
                                .title(resources.constants().download())
                                .href(logFiles.downloadUrl(item.getFilename()),
                                        UIConstants.DOWNLOAD, item.getFilename())
                                .build();
                        ItemAction<LogFile> external = new ItemAction.Builder<LogFile>()
                                .title(resources.constants().openInExternalWindow())
                                .href(logFiles.externalUrl(item.getFilename()),
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
        );
    }


}
