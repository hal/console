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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.List;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class SSLPresenter extends ApplicationFinderPresenter<SSLPresenter.MyView, SSLPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String SPACE = " ";

    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private Resources resources;
    private Dispatcher dispatcher;

    @Inject
    public SSLPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Resources resources,
            Finder finder,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.resources = resources;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.ELYTRON_RUNTIME, Ids.ELYTRON_SECURITY_REALMS,
                        Names.ELYTRON, Names.STORES);
    }

    @Override
    protected void reload() {
        Composite composite = new Composite();
        composite.add(operation(KEY_MANAGER_TEMPLATE));
        composite.add(operation(SECURITY_DOMAIN_TEMPLATE));
        composite.add(operation(TRUST_MANAGER_TEMPLATE));
        dispatcher.execute(composite, (CompositeResult result) -> {
            int i = 0;
            getView().updateKeyManager(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateSecurityDomain(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
            getView().updateTrustManager(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
        });
    }

    private Operation operation(AddressTemplate template) {
        return new Operation.Builder(template.getParent().resolve(statementContext), READ_CHILDREN_RESOURCES_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(CHILD_TYPE, template.lastName())
                .build();
    }

    void initKeyManager(String name) {
        Operation operation = new Operation.Builder(KEY_MANAGER_TEMPLATE.resolve(statementContext, name), INIT)
                .build();
        dispatcher.execute(operation, result -> {
                    MessageEvent.fire(getEventBus(), Message.success(resources.messages().initSuccess(name)));
                    reload();
                },
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().initError(name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().initError(name, exception.getMessage()))));
    }

    void reloadCRL(String name) {
        Operation operation = new Operation.Builder(TRUST_MANAGER_TEMPLATE.resolve(statementContext, name),
                RELOAD_CERTIFICATE_REVOCATION_LIST)
                .build();
        dispatcher.execute(operation, result -> MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().reloadCRLSuccess(name))),
                (operation1, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadCRLError(name, failure))),
                (operation1, exception) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().reloadCRLError(name, exception.getMessage()))));
    }

    void readIdentity(Metadata metadata, String name) {
        /*
        AddressTemplate template = metadata.getTemplate();
        LabelBuilder labelBuilder = new LabelBuilder();
        String resource = labelBuilder.label(template.lastName()) + SPACE + name;
        ResourceAddress address = template.resolve(statementContext, name);
        Operation addOp = new Operation.Builder(address, READ_IDENTITY)
                .param(IDENTITY, name)
                .build();
        dispatcher.execute(addOp, result -> {},
                (operation, failure) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasError(name, resource, failure))),
                (operation, ex) -> MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().readAliasError(name, resource, ex.getMessage()))));

*/
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.ELYTRON_RUNTIME_SSL)
    @Requires({KEY_MANAGER_ADDRESS, SECURITY_DOMAIN_ADDRESS, TRUST_MANAGER_ADDRESS})
    public interface MyProxy extends ProxyPlace<SSLPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<SSLPresenter> {
        void updateKeyManager(List<NamedNode> items);
        void updateSecurityDomain(List<NamedNode> items);
        void updateTrustManager(List<NamedNode> items);

    }
    // @formatter:on
}
