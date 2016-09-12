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
package org.jboss.hal.client.deployment;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.client.deployment.DeploymentFunctions.AssignmentsOfServerGroup;
import org.jboss.hal.client.deployment.DeploymentFunctions.LoadDeploymentsFromRunningServer;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.TopologyFunctions.RunningServersQuery;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * The assigned deployments of a server group.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ASSIGNED_DEPLOYMENT)
@Requires("/server-group=*/deployment=*")
public class AssignmentColumn extends FinderColumn<Assignment> {

    @Inject
    public AssignmentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Environment environment,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final Places places,
            final StatementContext statementContext,
            final @Footer Provider<Progress> progress,
            final Resources resources) {

        super(new FinderColumn.Builder<Assignment>(finder, Ids.ASSIGNED_DEPLOYMENT, Names.DEPLOYMENT)

                .itemRenderer(item -> new ItemDisplay<Assignment>() {
                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public String getTooltip() {
                        if (item.getDeployment() != null && item.getDeployment()
                                .getStatus() == Deployment.Status.FAILED) {
                            return resources.constants().failed();
                        } else {
                            return item.isEnabled() ? resources.constants().enabled() : resources.constants()
                                    .disabled();
                        }
                    }

                    @Override
                    public Element getIcon() {
                        if (item.getDeployment() != null && item.getDeployment()
                                .getStatus() == Deployment.Status.FAILED) {
                            return Icons.unknown();
                        } else {
                            return item.isEnabled() ? Icons.ok() : Icons.disabled();
                        }
                    }

                    @Override
                    public String getFilterData() {
                        return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
                    }
                })

                .pinnable()
                .showCount()
                .withFilter());

        addColumnAction(columnActionFactory.add(Ids.ASSIGNMENT_ADD, Names.DEPLOYMENT, column -> add()));
        addColumnAction(columnActionFactory.refresh(Ids.ASSIGNMENT_REFRESH));
        setPreviewCallback(item -> new AssignmentPreview(this, item, places, resources));

        setItemsProvider((context, callback) -> {

            List<Function<FunctionContext>> functions = Lists.newArrayList(
                    new AssignmentsOfServerGroup(environment, dispatcher, statementContext.selectedServerGroup()),
                    new RunningServersQuery(environment, dispatcher,
                            new ModelNode().set(SERVER_GROUP, statementContext.selectedServerGroup())),
                    new LoadDeploymentsFromRunningServer(environment, dispatcher));

            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getError());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Assignment> assignments = context.get(DeploymentFunctions.ASSIGNMENTS);
                            callback.onSuccess(assignments);
                        }
                    }, functions.toArray(new Function[functions.size()]));

        });

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.assign(this, dispatcher, eventBus, progress, resources,
                    statementContext.selectedServerGroup(), event.dataTransfer.files));
        }
    }

    private void add() {
        Browser.getWindow().alert(Names.NYI);
    }

    void diable() {
        Browser.getWindow().alert(Names.NYI);
    }

    void enable() {
        Browser.getWindow().alert(Names.NYI);
    }
}
