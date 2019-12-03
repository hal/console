/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;
import rx.Completable;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBDEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.undertow.AddressTemplates.WEB_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.flow.Flow.series;

public class DeploymentPresenter
        extends ApplicationFinderPresenter<DeploymentPresenter.MyView, DeploymentPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SERVLETS = "servlets";
    private static final String WEBSOCKETS = "websockets";
    private static final String SESSION_IDS = "session-ids";
    private static final String SESSIONS = "sessions";

    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Provider<Progress> progress;
    private String deploymentName;
    private String subdeploymentName;

    @Inject
    public DeploymentPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources,
            @Footer Provider<Progress> progress) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.resources = resources;
        this.progress = progress;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        deploymentName = request.getParameter(DEPLOYMENT, null);
        subdeploymentName = request.getParameter(SUBDEPLOYMENT, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        String itemPath = subdeploymentName == null ? deploymentName : deploymentName + "/" + subdeploymentName;
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, UNDERTOW, resources.constants().monitor(), Names.WEB)
                .append(Ids.UNDERTOW_RUNTIME, Ids.asId(Names.DEPLOYMENT), Names.WEB, Names.DEPLOYMENT)
                .append(Ids.UNDERTOW_RUNTIME_DEPLOYMENT, Ids.asId(itemPath), Names.DEPLOYMENT, itemPath);
    }

    @Override
    protected void reload() {
        ResourceAddress address = deploymentAddress();

        // task 1: read sessions ids, servlets and websockets
        Operation readResourceOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();
        Operation listSessionsOp = new Operation.Builder(address, LIST_SESSIONS).build();
        Task<FlowContext> task1 = context -> dispatcher.execute(new Composite(readResourceOp, listSessionsOp))
                .doOnSuccess((CompositeResult result) -> {
                    ModelNode readResourceResult = result.step(0).get(RESULT);
                    List<NamedNode> servlets = asNamedNodes(failSafePropertyList(readResourceResult, SERVLET));
                    List<NamedNode> websockets = asNamedNodes(failSafePropertyList(readResourceResult, WEBSOCKET));

                    // sorted session ids (important for step 2!)
                    ModelNode listSessionsResult = result.step(1).get(RESULT);
                    List<String> sessionIds = listSessionsResult.isDefined()
                            ? listSessionsResult.asList().stream().map(ModelNode::asString).sorted().collect(toList())
                            : Collections.emptyList();

                    context.set(SERVLETS, servlets);
                    context.set(WEBSOCKETS, websockets);
                    context.set(SESSION_IDS, sessionIds);
                })
                .toCompletable();

        // task 2: read session creation and last access times
        Task<FlowContext> task2 = context -> {
            List<String> sessionIds = context.get(SESSION_IDS);
            if (sessionIds.isEmpty()) {
                context.set(SESSIONS, Collections.emptyList());
                return Completable.complete();
            } else {
                List<Operation> operations = new ArrayList<>();
                for (String id : sessionIds) {
                    operations.add(new Operation.Builder(address, GET_SESSION_CREATION_TIME)
                            .param(SESSION_ID, id)
                            .build());
                    operations.add(new Operation.Builder(address, GET_SESSION_LAST_ACCESSED_TIME)
                            .param(SESSION_ID, id)
                            .build());
                }
                return dispatcher.execute(new Composite(operations))
                        .doOnSuccess((CompositeResult result) -> {
                            int i = 0;
                            List<Session> sessions = new ArrayList<>();
                            for (String sessionId : sessionIds) {
                                ModelNode modelNode = new ModelNode();
                                if (result.step(i).isDefined() && result.step(i).get(RESULT).isDefined()) {
                                    modelNode.get(CREATION_TIME).set(result.step(i).get(RESULT));
                                }
                                if (result.step(i + 1).isDefined() && result.step(i + 1).get(RESULT).isDefined()) {
                                    modelNode.get(LAST_ACCESSED_TIME).set(result.step(i + 1).get(RESULT));
                                }
                                sessions.add(new Session(sessionId, modelNode));
                                i++;
                            }
                            context.set(SESSIONS, sessions);
                        })
                        .toCompletable();
            }
        };

        series(new FlowContext(progress.get()), task1, task2)
                .subscribe(new SuccessfulOutcome<FlowContext>(getEventBus(), resources) {
                    @Override
                    public void onSuccess(FlowContext context) {
                        List<Session> sessions = context.get(SESSIONS);
                        List<NamedNode> servlets = context.get(SERVLETS);
                        List<NamedNode> websockets = context.get(WEBSOCKETS);

                        getView().updateSessions(sessions);
                        getView().updateServlets(servlets);
                        getView().updateWebsockets(websockets);
                    }
                });
    }

    StatementContext getStatementContext() {
        return statementContext;
    }

    void invalidateSession(Session session) {
        DialogFactory.showConfirmation(resources.constants().invalidateSession(),
                resources.messages().invalidateSessionQuestion(), () -> {
                    Operation operation = new Operation.Builder(deploymentAddress(), INVALIDATE_SESSION)
                            .param(SESSION_ID, session.getSessionId())
                            .build();
                    dispatcher.execute(operation,
                            result -> {
                                MessageEvent.fire(getEventBus(), Message.success(
                                        resources.messages().invalidateSessionSuccess()));
                                reload();
                            },
                            (op, failure) -> MessageEvent.fire(getEventBus(),
                                    Message.error(resources.messages().invalidateSessionError(failure))));
                });
    }

    void listSessionAttributes(Session session) {
        Operation operation = new Operation.Builder(deploymentAddress(), LIST_SESSION_ATTRIBUTES)
                .param(SESSION_ID, session.getSessionId())
                .build();
        dispatcher.execute(operation, result -> getView().updateSessionAttributes(result.asPropertyList()));
    }

    private ResourceAddress deploymentAddress() {
        ResourceAddress address;
        if (subdeploymentName == null) {
            address = WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext, deploymentName);
        } else {
            address = WEB_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext, deploymentName, subdeploymentName);
        }
        return address;
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires({WEB_DEPLOYMENT_ADDRESS, WEB_SUBDEPLOYMENT_ADDRESS})
    @NameToken(NameTokens.UNDERTOW_RUNTIME_DEPLOYMENT_VIEW)
    public interface MyProxy extends ProxyPlace<DeploymentPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<DeploymentPresenter> {
        void updateSessions(List<Session> sessions);
        void updateSessionAttributes(List<Property> attributes);
        void updateServlets(List<NamedNode> model);
        void updateWebsockets(List<NamedNode> model);
    }
    // @formatter:on
}
