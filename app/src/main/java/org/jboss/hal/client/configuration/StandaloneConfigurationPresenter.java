/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.core.TopLevelCategory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.HasFinder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.PropertyColumn;
import org.jboss.hal.core.mbui.LabelBuilder;
import org.jboss.hal.core.mvp.PatternFlyPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.Slots;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromSafeConstant;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.*;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
public class StandaloneConfigurationPresenter
        extends PatternFlyPresenter<StandaloneConfigurationPresenter.MyView, StandaloneConfigurationPresenter.MyProxy>
        implements TopLevelCategory {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.CONFIGURATION)
    public interface MyProxy extends ProxyPlace<StandaloneConfigurationPresenter> {}

    public interface MyView extends PatternFlyView, HasFinder {}
    // @formatter:on


    private final Finder finder;
    private final SubsystemColumn subsystemColumn;
    private final PropertyColumn interfaceColumn;
    private final PropertyColumn socketBindingColumn;
    private final Dispatcher dispatcher;

    @Inject
    public StandaloneConfigurationPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final Resources resources) {
        super(eventBus, view, proxy, Slots.MAIN);

        StandaloneRootColumn initialColumn = new StandaloneRootColumn(this, resources);
        PreviewContent initialPreview = new PreviewContent(CONFIGURATION,
                fromSafeConstant(resources.previews().standaloneConfiguration().getText()));

        this.dispatcher = dispatcher;
        this.finder = new Finder(CONFIGURATION_FINDER, eventBus, initialColumn, initialPreview);
        this.subsystemColumn = new SubsystemColumn();
        this.interfaceColumn = new PropertyColumn(INTERFACE_COLUMN, INTERFACE,
                property -> new PreviewContent(new LabelBuilder().label(property), SafeHtmlUtils.fromString(NYI)));
        this.socketBindingColumn = new PropertyColumn(SOCKET_BINDING_COLUMN, SOCKET_BINDING,
                property -> new PreviewContent(new LabelBuilder().label(property), SafeHtmlUtils.fromString(NYI)));
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setFinder(finder);
    }

    public void loadSubsystems() {
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, ModelDescriptionConstants.SUBSYSTEM).build();
        dispatcher.execute(operation, result -> {
            List<String> names = Lists.transform(result.asList(), ModelNode::asString);
            subsystemColumn.setItems(names);
            finder.appendColumn(subsystemColumn);
        });
    }

    public void loadInterfaces() {
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "interface").build();
        dispatcher.execute(operation, result -> {
            interfaceColumn.setItems(result.asPropertyList());
            finder.appendColumn(interfaceColumn);
        });
    }

    public void loadSocketBindings() {
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "socket-binding-group").build();
        dispatcher.execute(operation, result -> {
            socketBindingColumn.setItems(result.asPropertyList());
            finder.appendColumn(socketBindingColumn);
        });
    }
}
