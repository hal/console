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
package org.jboss.hal.client.configuration.subsystem.datasource;

import javax.inject.Inject;
import javax.inject.Provider;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.runtime.domain.TopologyFunctions;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JDBC_DRIVER;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@AsyncColumn(JDBC_DRIVER)
@Requires(JDBC_DRIVER_ADDRESS)
public class JdbcDriverColumn extends FinderColumn<JdbcDriver> {


    @Inject
    protected JdbcDriverColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final @Footer Provider<Progress> progress,
            final Resources resources) {

        super(new FinderColumn.Builder<JdbcDriver>(finder, JDBC_DRIVER, Names.JDBC_DRIVER)

                .columnAction(columnActionFactory.add(IdBuilder.build(JDBC_DRIVER, "add"), Names.JDBC_DRIVER,
                        JDBC_DRIVER_TEMPLATE))
                .columnAction(columnActionFactory.refresh(IdBuilder.build(JDBC_DRIVER, "refresh")))

                .itemsProvider((context, callback) -> {
                    Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getError());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            callback.onSuccess(context.get(JdbcDriverFunctions.DRIVERS));
                        }
                    };
                    new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                            new JdbcDriverFunctions.Read(statementContext, dispatcher),
                            new TopologyFunctions.ServerGroupsOfProfile(dispatcher, statementContext.selectedProfile()),
                            new TopologyFunctions.RunningServersOfGroupsInContext(dispatcher),
                            new JdbcDriverFunctions.AddRuntimeInfo(dispatcher));
                })

                .itemRenderer(driver -> new ItemDisplay<JdbcDriver>() {
                    @Override
                    public Element asElement() {
                        if (driver.getProvider() != JdbcDriver.Provider.UNKNOWN) {
                            String providedBy = resources.constants().providedBy() + " " + driver.getProvider()
                                    .label();
                            return new Elements.Builder()
                                    .span().css(itemText)
                                    .span().textContent(driver.getName()).end()
                                    .start("small").css(subtitle).textContent(providedBy).end()
                                    .end().build();
                        }
                        return null;
                    }

                    @Override
                    public String getTitle() {
                        return driver.getName();
                    }
                })

                .useFirstActionAsBreadcrumbHandler());

        setPreviewCallback(driver -> new JdbcDriverPreview(driver, resources));
    }
}
