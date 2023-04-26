/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
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
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.runtime.TopologyTasks;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
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

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.DEPLOYMENT;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.MODULE;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.UNKNOWN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_MODULE_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE_SLOT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.properties;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.fontAwesome;

@AsyncColumn(Ids.JDBC_DRIVER)
@Requires(JDBC_DRIVER_ADDRESS)
public class JdbcDriverColumn extends FinderColumn<JdbcDriver> {

    @Inject
    public JdbcDriverColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Environment environment,
            EventBus eventBus,
            Dispatcher dispatcher,
            CrudOperations crud,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            @Footer Provider<Progress> progress,
            Resources resources) {

        super(new FinderColumn.Builder<JdbcDriver>(finder, Ids.JDBC_DRIVER, Names.JDBC_DRIVER)

                .itemsProvider(finderContext -> {
                    List<Task<FlowContext>> tasks = new ArrayList<>();
                    tasks.add(new JdbcDriverTasks.ReadConfiguration(crud));
                    tasks.addAll(TopologyTasks.runningServers(environment, dispatcher,
                            properties(PROFILE_NAME, statementContext.selectedProfile())));
                    tasks.add(new JdbcDriverTasks.ReadRuntime(environment, dispatcher));
                    tasks.add(new JdbcDriverTasks.CombineDriverResults());
                    return sequential(new FlowContext(progress.get()), tasks)
                            .then(flowContext -> Promise.resolve(flowContext.<List<JdbcDriver>> get(JdbcDriverTasks.DRIVERS)));
                })
                .withFilter()
                .filterDescription(resources.messages().jdbcDriverColumnFilterDescription()));

        addColumnAction(columnActionFactory.add(Ids.JDBC_DRIVER_ADD, Names.JDBC_DRIVER, JDBC_DRIVER_TEMPLATE,
                column -> {
                    Metadata metadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);
                    Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.JDBC_DRIVER_ADD_FORM, metadata)
                            .fromRequestProperties()
                            .unboundFormItem(new NameItem(), 0)
                            .include(DRIVER_MODULE_NAME, MODULE_SLOT, DRIVER_CLASS_NAME,
                                    DRIVER_DATASOURCE_CLASS_NAME,
                                    DRIVER_XA_DATASOURCE_CLASS_NAME)
                            .unsorted()
                            .build();

                    FormItem<String> driverNameItem = form.getFormItem(NAME);
                    driverNameItem.addValidationHandler(createUniqueValidation());

                    AddResourceDialog dialog = new AddResourceDialog(
                            resources.messages().addResourceTitle(Names.JDBC_DRIVER), form,
                            (name, modelNode) -> {
                                if (modelNode != null) {
                                    ResourceAddress address = JDBC_DRIVER_TEMPLATE
                                            .resolve(statementContext, name);
                                    Operation operation = new Operation.Builder(address, ADD)
                                            .payload(modelNode)
                                            .build();
                                    dispatcher.execute(operation, result -> {
                                        MessageEvent.fire(eventBus,
                                                Message.success(resources.messages()
                                                        .addResourceSuccess(Names.JDBC_DRIVER, name)));
                                        column.refresh(name);
                                    }, (__, failure) -> MessageEvent.fire(eventBus,
                                            Message.error(resources.messages()
                                                    .addResourceError(name, failure))));
                                }
                            });
                    dialog.show();
                }));
        addColumnAction(columnActionFactory.refresh(Ids.JDBC_DRIVER_REFRESH));

        setItemRenderer(driver -> new ItemDisplay<JdbcDriver>() {
            @Override
            public HTMLElement getIcon() {
                HTMLElement icon = null;
                JdbcDriver.Provider provider = driver.getProvider();
                if (provider != UNKNOWN) {
                    icon = span().element();
                    if (provider == MODULE) {
                        icon.className = fontAwesome("cubes");
                    } else if (provider == DEPLOYMENT) {
                        icon.className = fontAwesome("archive");
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
