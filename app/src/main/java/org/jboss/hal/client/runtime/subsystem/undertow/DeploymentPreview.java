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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerUrl;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class DeploymentPreview extends PreviewContent<DeploymentResource> {

    private static final String LINK = "link";

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ServerActions serverActions;
    private PreviewAttributes<DeploymentResource> previewAttributes;

    DeploymentPreview(DeploymentResource deploymentResource,
            FinderPathFactory finderPathFactory,
            Places places,
            Environment environment,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ServerActions serverActions) {
        super(deploymentResource.getPath());
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.serverActions = serverActions;

        previewAttributes = new PreviewAttributes<>(deploymentResource)
                .append(ACTIVE_SESSIONS)
                .append(model -> new PreviewAttributes.PreviewAttribute(new LabelBuilder().label(CONTEXT_ROOT),
                        span().textContent(model.get(CONTEXT_ROOT).asString())
                                .data(LINK, "")
                                .asElement()))
                .append(model -> {
                    FinderPath path = finderPathFactory.deployment(deploymentResource.getDeployment());
                    PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
                    return new PreviewAttributes.PreviewAttribute(Names.DEPLOYMENT, deploymentResource.getPath(),
                            places.historyToken(placeRequest));
                })
                .append(EXPIRE_MESSAGE)
                .append(EXPIRED_SESSIONS)
                .append(MAX_ACTIVE_SESSIONS)
                .append(REJECTED_SESSIONS)
                .append(SERVER)
                .append(SESSION_AVG_ALIVE_TIME)
                .append(SESSION_MAX_ALIVE_TIME)
                .append(SESSIONS_CREATED)
                .append(VIRTUAL_HOST);
        getHeaderContainer().appendChild(refreshLink(() -> update(deploymentResource)));
        previewBuilder().addAll(previewAttributes);
    }

    @Override
    public void attach() {
        super.attach();
        injectUrls();
    }

    @Override
    public void update(final DeploymentResource item) {
        Operation operation = new Operation.Builder(item.getAddress(), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation, result -> {
            DeploymentResource n = new DeploymentResource(item.getAddress(), result);
            previewAttributes.refresh(n);
            injectUrls();
        });
    }

    private void injectUrls() {
        List<HTMLElement> linkContainers = new ArrayList<>();
        asElements().forEach(e -> {
            List<HTMLElement> elements = stream(e.querySelectorAll("[data-" + LINK + "]")) //NON-NLS
                    .filter(htmlElements())
                    .map(asHtmlElement())
                    .collect(toList());
            linkContainers.addAll(elements);
        });
        if (!linkContainers.isEmpty()) {
            String host = statementContext.selectedHost();
            String serverGroup = statementContext.selectedServerGroup();
            String server = statementContext.selectedServer();
            serverActions.readUrl(environment.isStandalone(), host, serverGroup, server,
                    new AsyncCallback<ServerUrl>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            // noop
                        }

                        @Override
                        public void onSuccess(ServerUrl url) {
                            for (HTMLElement linkContainer : linkContainers) {
                                String link = linkContainer.textContent;
                                Elements.removeChildrenFrom(linkContainer);
                                linkContainer.appendChild(a(url.getUrl() + link)
                                        .apply(a -> a.target = serverId())
                                        .textContent(link)
                                        .asElement());
                            }
                        }
                    });
        }
    }

    private String serverId() {
        return Ids.hostServer(statementContext.selectedHost(), statementContext.selectedServer());
    }
}
