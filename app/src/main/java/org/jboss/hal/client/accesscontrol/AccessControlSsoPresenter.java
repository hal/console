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
package org.jboss.hal.client.accesscontrol;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.TopLevelPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTH_SERVER_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEYCLOAK_SERVER_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REALM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REALM_PUBLIC_KEY;
import static org.jboss.hal.flow.Flow.series;

public class AccessControlSsoPresenter
        extends TopLevelPresenter<AccessControlSsoPresenter.MyView, AccessControlSsoPresenter.MyProxy> {

    // keycloak subsystem for http-management works only in standalone mode
    private static final String KEYCLOAK_REALM_ADDRESS = "/subsystem=keycloak/realm=*";
    // the wildfly-console is the default name for the http management interface
    private static final String KEYCLOAK_SECURE_SERVER_ADDRESS = "/subsystem=keycloak/secure-server=wildfly-console";
    private static final AddressTemplate KEYCLOAK_REALM_TEMPLATE = AddressTemplate.of(KEYCLOAK_REALM_ADDRESS);
    private static final AddressTemplate KEYCLOAK_SECURE_SERVER_TEMPLATE = AddressTemplate.of(KEYCLOAK_SECURE_SERVER_ADDRESS);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Provider<Progress> progress;
    private final Environment environment;

    @Inject
    public AccessControlSsoPresenter(EventBus eventBus, MyView view, MyProxy myProxy, Dispatcher dispatcher,
            StatementContext statementContext, Resources resources, @Footer Provider<Progress> progress,
            Environment environment) {
        super(eventBus, view, myProxy);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.progress = progress;
        this.environment = environment;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        List<Task<FlowContext>> tasks = new ArrayList<>();

        tasks.add(flowContext -> {
            ResourceAddress address = KEYCLOAK_SECURE_SERVER_TEMPLATE.resolve(statementContext);
            Operation op = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .build();
            flowContext.set(ADDRESS, address.toString());

            return dispatcher.execute(op)
                    .then(response -> Promise.resolve(flowContext.set(REALM, response.get(REALM).asString())))
                    .catch_(error -> Promise.resolve(flowContext.failure(
                            resources.messages().failedReadKeycloak(address.toString(), String.valueOf(error)))));
        });

        tasks.add(flowContext -> {
            ResourceAddress address = KEYCLOAK_REALM_TEMPLATE.resolve(statementContext, flowContext.<String> get(REALM));
            Operation op = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .build();
            flowContext.set(ADDRESS, address.toString());

            return dispatcher.execute(op)
                    .then(response -> {
                        flowContext.set(KEYCLOAK_SERVER_URL, response.get(AUTH_SERVER_URL).asString());
                        flowContext.set(REALM_PUBLIC_KEY, response.get(REALM_PUBLIC_KEY).asString());
                        return Promise.resolve(flowContext);
                    })
                    .catch_(error -> Promise.resolve(flowContext.failure(
                            resources.messages().failedReadKeycloak(address.toString(), String.valueOf(error)))));
        });

        series(new FlowContext(progress.get()), tasks).then(flowContext -> {
            if (flowContext.hasFailure()) {
                flowContext.showFailure(getEventBus());
            } else {
                ModelNode payload = new ModelNode();
                payload.get(REALM).set(flowContext.<String> get(REALM));
                payload.get(REALM_PUBLIC_KEY).set(flowContext.<String> get(REALM_PUBLIC_KEY));
                payload.get(KEYCLOAK_SERVER_URL).set(flowContext.<String> get(KEYCLOAK_SERVER_URL));
                getView().update(payload);
            }
            return null;
        });
    }

    public Environment getEnvironment() {
        return environment;
    }

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.ACCESS_CONTROL_SSO)
    public interface MyProxy extends ProxyPlace<AccessControlSsoPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<AccessControlSsoPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
