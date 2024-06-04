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
package org.jboss.hal.client.configuration.subsystem.ejb;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.EJB_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.EJB_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.REMOTING_EJB_RECEIVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.REMOTING_PROFILE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.ejb.AddressTemplates.RER_CHANNEL_CREATION_OPTIONS_TEMPLATE;

public class EjbPresenter
        extends MbuiPresenter<EjbPresenter.MyView, EjbPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;

    @Inject
    public EjbPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
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
        return EJB_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(ModelDescriptionConstants.EJB3);
    }

    @Override
    protected void reload() {
        crud.read(EJB_SUBSYSTEM_TEMPLATE, 1, result -> getView().update(result));
    }

    public void loadRemotingProfileChild(String remotingProfileName, String childType) {
        crud.readRecursive(REMOTING_PROFILE_TEMPLATE.resolve(statementContext, remotingProfileName),
                result -> getView().updateRemotingProfileChild(remotingProfileName, childType, result));
    }

    public void addRemotingProfileChild(String id, String label, String remotingProfileName, String childType,
            AddressTemplate template) {
        crud.add(id, label, template.replaceWildcards(remotingProfileName),
                (name, address) -> loadRemotingProfileChild(remotingProfileName, childType));
    }

    public void removeRemotingProfileChild(String label, String name, String remotingProfileName, String childType,
            AddressTemplate template) {
        crud.remove(label, name, template.replaceWildcards(remotingProfileName),
                () -> loadRemotingProfileChild(remotingProfileName, childType));
    }

    public void loadRerChannelCreationOptions(String remotingProfileName, String ejbReceiverName) {
        crud.readRecursive(
                REMOTING_EJB_RECEIVER_TEMPLATE.resolve(statementContext, remotingProfileName, ejbReceiverName),
                result -> getView().updateRerChannelCreationOptions(ejbReceiverName, result));
    }

    public void addRerChannelCreationOptions(String id, String label, String remotingProfileName, String ejbReceiverName) {
        crud.add(id, label, RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.replaceWildcards(remotingProfileName, ejbReceiverName),
                List.of(ModelDescriptionConstants.VALUE),
                (name, address) -> loadRerChannelCreationOptions(remotingProfileName, ejbReceiverName));
    }

    public void removeRerChannelCreationOptions(String label, String name, String remotingProfileName, String ejbReceiverName) {
        crud.remove(label, name, RER_CHANNEL_CREATION_OPTIONS_TEMPLATE.replaceWildcards(remotingProfileName, ejbReceiverName),
                () -> loadRerChannelCreationOptions(remotingProfileName, ejbReceiverName));
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.EJB3_CONFIGURATION)
    @Requires({ EJB_SUBSYSTEM_ADDRESS })
    public interface MyProxy extends ProxyPlace<EjbPresenter> {
    }

    public interface MyView extends MbuiView<EjbPresenter> {
        void update(ModelNode payload);
        void updateRemotingProfileChild(String remotingProfileName, String childType, ModelNode payload);
        void updateRerChannelCreationOptions(String ejbReceiverName, ModelNode payload);
    }
    // @formatter:on
}
