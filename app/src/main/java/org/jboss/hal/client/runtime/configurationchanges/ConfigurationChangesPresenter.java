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
package org.jboss.hal.client.runtime.configurationchanges;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLPreElement;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.pre;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.applicationHeight;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.LARGE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.token.NameTokens.CONFIGURATION_CHANGES;
import static org.jboss.hal.resources.CSS.formControlStatic;
import static org.jboss.hal.resources.CSS.px;
import static org.jboss.hal.resources.CSS.wrap;
import static org.jboss.hal.resources.Ids.ADD_SUFFIX;

public class ConfigurationChangesPresenter extends
        ApplicationFinderPresenter<ConfigurationChangesPresenter.MyView, ConfigurationChangesPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(CONFIGURATION_CHANGES)
    @Requires(CONFIGURATION_CHANGES_ADDRESS)
    public interface MyProxy extends ProxyPlace<ConfigurationChangesPresenter> {}
    public interface MyView extends HalView, HasPresenter<ConfigurationChangesPresenter> {
        void update(ModelNode model);
    }
    // @formatter:on

    public static final String CONFIGURATION_CHANGES_ADDRESS = "{selected.host}/subsystem=core-management/service=configuration-changes";
    private static final AddressTemplate CORE_MANAGEMENT_TEMPLATE = AddressTemplate.of(
            "{selected.host}/subsystem=core-management");
    public static final AddressTemplate CONFIGURATION_CHANGES_TEMPLATE = AddressTemplate.of(
            CONFIGURATION_CHANGES_ADDRESS);

    private Environment environment;
    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private CrudOperations crud;
    private MetadataRegistry metadataRegistry;

    @Inject
    public ConfigurationChangesPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Environment environment,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final CrudOperations crud,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.crud = crud;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public FinderPath finderPath() {
        return environment.isStandalone() ? finderPathFactory.runtimeServerPath() : finderPathFactory.runtimeHostPath();
    }

    @Override
    protected void reload() {
        ResourceAddress coreAddress = CORE_MANAGEMENT_TEMPLATE.resolve(statementContext);
        Operation coreOperation = new Operation.Builder(coreAddress, READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SERVICE)
                .build();
        dispatcher.execute(coreOperation, coreResult -> {
            boolean configurationChangesEnabled = coreResult.asList().size() > 0;

            if (configurationChangesEnabled) {
                ResourceAddress ccAddress = CONFIGURATION_CHANGES_TEMPLATE.resolve(statementContext);
                Operation ccOperation = new Operation.Builder(ccAddress, LIST_CHANGES_OPERATION)
                        .build();
                dispatcher.execute(ccOperation, ccResult -> getView().update(ccResult));
            } else {
                getView().update(new ModelNode());
            }
        });
    }

    void launchAdd() {
        Metadata metadata = metadataRegistry.lookup(CONFIGURATION_CHANGES_TEMPLATE);
        String id = Ids.build(Ids.CONFIGURATION_CHANGES, ADD_SUFFIX);
        Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, ADD)
                .build();
        ModelNode changeModel = new ModelNode();
        Dialog dialog = new Dialog.Builder(resources.constants().configurationChanges())
                .add(form.asElement())
                .primary(resources.constants().yes(), () -> {
                    boolean valid = form.save();
                    // if the form contains validation error, don't close the dialog
                    if (valid) {
                        crud.addSingleton(Names.CONFIGURATION_CHANGES, CONFIGURATION_CHANGES_TEMPLATE, form.getModel(),
                                address -> reload());
                    }
                    return valid;
                })
                .secondary(resources.constants().cancel(), () -> true)
                .closeIcon(true)
                .closeOnEsc(true)
                .build();

        dialog.show();
        form.edit(changeModel);
    }

    void disable() {
        String type = HOST;
        String name = statementContext.selectedHost();
        if (environment.isStandalone()) {
            type = Names.STANDALONE_SERVER;
            name = Server.STANDALONE.getName();
        }
        DialogFactory.showConfirmation(resources.constants().configurationChanges(),
                resources.messages().removeConfigurationChangesQuestion(type, name),
                () -> {
                    ResourceAddress address = CONFIGURATION_CHANGES_TEMPLATE.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, REMOVE)
                            .build();
                    dispatcher.execute(operation, result -> getView().update(result));
                });
    }

    void viewRawChange(final ConfigurationChange change) {
        HTMLPreElement elem = pre().css(formControlStatic, wrap).asElement();
        elem.textContent = change.asModelNode().toString();

        HTMLElement content = div()
                .add(elem)
                .style("overflow: scroll") //NON-NLS
                .asElement();

        int maxheight = applicationHeight() - 6 * MARGIN_BIG;
        content.style.maxHeight = CSSProperties.MaxHeightUnionType.of(px(maxheight));

        Dialog dialog = new Dialog.Builder(resources.constants().configurationChanges())
                .primary(resources.constants().close(), () -> true)
                .closeOnEsc(true)
                .closeIcon(true)
                .size(LARGE)
                .add(content)
                .build();
        dialog.show();
    }

    public StatementContext getStatementContext() {
        return statementContext;
    }
}
