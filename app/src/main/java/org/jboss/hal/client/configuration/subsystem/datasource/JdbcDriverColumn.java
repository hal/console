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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.TopologyFunctions;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.DEPLOYMENT;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.MODULE;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.UNKNOWN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.JDBC_DRIVER)
@Requires(JDBC_DRIVER_ADDRESS)
public class JdbcDriverColumn extends FinderColumn<JdbcDriver> {

    @Inject
    public JdbcDriverColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Environment environment,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final @Footer Provider<Progress> progress,
            final Resources resources) {

        super(new FinderColumn.Builder<JdbcDriver>(finder, Ids.JDBC_DRIVER, Names.JDBC_DRIVER)

                .itemsProvider((context, callback) -> {
                    Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getException());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            callback.onSuccess(context.get(JdbcDriverFunctions.DRIVERS));
                        }
                    };
                    new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                            new JdbcDriverFunctions.ReadConfiguration(crud),
                            new TopologyFunctions.RunningServersQuery(environment, dispatcher,
                                    environment.isStandalone()
                                            ? null
                                            : new ModelNode().set(PROFILE_NAME, statementContext.selectedProfile())),
                            new JdbcDriverFunctions.ReadRuntime(environment, dispatcher),
                            new JdbcDriverFunctions.CombineDriverResults());
                })
                .withFilter()
        );

        addColumnAction(columnActionFactory.add(Ids.JDBC_DRIVER_ADD, Names.JDBC_DRIVER,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);
                    Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.JDBC_DRIVER_ADD_FORM, metadata)
                            .fromRequestProperties()
                            .include(DRIVER_NAME, DRIVER_MODULE_NAME, DRIVER_CLASS_NAME, DRIVER_MAJOR_VERSION,
                                    DRIVER_MINOR_VERSION)
                            .unsorted()
                            .build();
                    AddResourceDialog dialog = new AddResourceDialog(
                            resources.messages().addResourceTitle(Names.JDBC_DRIVER), form,
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    // name is null - the form does not have a name field!
                                    String driverName = modelNode.get(DRIVER_NAME).asString();
                                    ResourceAddress address = JDBC_DRIVER_TEMPLATE
                                            .resolve(statementContext, driverName);
                                    Operation operation = new Operation.Builder(ADD, address)
                                            .payload(modelNode)
                                            .build();
                                    dispatcher.execute(operation, result -> {
                                        MessageEvent.fire(eventBus,
                                                Message.success(resources.messages()
                                                        .addResourceSuccess(Names.JDBC_DRIVER,
                                                                modelNode.get(DRIVER_NAME).asString())));
                                        column.refresh(driverName);
                                    });
                                }
                            });
                    dialog.show();
                }));
        addColumnAction(columnActionFactory.refresh(Ids.JDBC_DRIVER_REFRESH));

        setItemRenderer(driver -> new ItemDisplay<JdbcDriver>() {
            @Override
            public Element getIcon() {
                SpanElement icon = null;
                JdbcDriver.Provider provider = driver.getProvider();
                if (provider != UNKNOWN) {
                    icon = Browser.getDocument().createSpanElement();
                    if (provider == MODULE) {
                        icon.setClassName(fontAwesome("cubes"));
                    } else if (provider == DEPLOYMENT) {
                        icon.setClassName(fontAwesome("archive"));
                    }
                }
                return icon;
            }

            @Override
            public String getTitle() {
                return driver.getName();
            }

            @Override
            public String getFilterData() {
                if ((driver.getProvider() == DEPLOYMENT || driver.getProvider() == MODULE)) {
                    return getTitle() + " " + driver.getProvider().text();
                }
                return getTitle();
            }

            @Override
            public List<ItemAction<JdbcDriver>> actions() {
                List<ItemAction<JdbcDriver>> actions = new ArrayList<>();
                if (driver.getProvider() == MODULE) {
                    actions.add(itemActionFactory.remove(Names.JDBC_DRIVER, driver.getName(), JDBC_DRIVER_TEMPLATE,
                            JdbcDriverColumn.this));
                }
                return actions;
            }
        });

        setPreviewCallback(driver -> new JdbcDriverPreview(driver, resources));
    }
}
