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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.AsyncColumn;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.LOGGING_PROFILE_COLUMN)
public class LoggingProfileColumn extends FinderColumn<NamedNode> {

    public LoggingProfileColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.LOGGING_PROFILE_COLUMN, Names.LOGGING_PROFILES)

                .itemsProvider(new ItemsProvider<NamedNode>() {
                    @Override
                    public void get(final FinderContext context, final AsyncCallback<List<NamedNode>> callback) {

                    }
                })
        );
    }
}
