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
package org.jboss.hal.client.runtime.configurationchanges;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
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
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
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
import static org.jboss.hal.resources.Ids.ADD;

public class ConfigurationChangesPresenter extends
        ApplicationFinderPresenter<ConfigurationChangesPresenter.MyView, ConfigurationChangesPresenter.MyProxy> {

    public static final String CONFIGURATION_CHANGES_ADDRESS = "/subsystem=core-management/service=configuration-changes";
    public static final String HOST_CONFIGURATION_CHANGES_ADDRESS = "{selected.host}" + CONFIGURATION_CHANGES_ADDRESS;
    public static final AddressTemplate HOST_CONFIGURATION_CHANGES_TEMPLATE = AddressTemplate.of(
            HOST_CONFIGURATION_CHANGES_ADDRESS);
    private static final String SERVER_CONFIGURATION_CHANGES_ADDRESS = "/{selected.host}/{selected.server}" + CONFIGURATION_CHANGES_ADDRESS;
    public static final AddressTemplate SERVER_CONFIGURATION_CHANGES_TEMPLATE = AddressTemplate.of(
            SERVER_CONFIGURATION_CHANGES_ADDRESS);
    private static final String PROFILE_CONFIGURATION_CHANGES_ADDRESS = "/profile=*/subsystem=core-management/service=configuration-changes";
    private static final AddressTemplate PROFILE_CONFIGURATION_CHANGES_TEMPLATE = AddressTemplate.of(PROFILE_CONFIGURATION_CHANGES_ADDRESS);
    private static final AddressTemplate CORE_MANAGEMENT_TEMPLATE = AddressTemplate.of(
            "{selected.host}/subsystem=core-management");

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private Provider<Progress> progress;
    private MetadataProcessor metadataProcessor;
    private final StatementContext statementContext;
    private final Resources resources;
    private Environment environment;
    private CrudOperations crud;
    private boolean hostOnly;
    private String profile;

    @Inject
    public ConfigurationChangesPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            Environment environment,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            @Footer Provider<Progress> progress,
            MetadataProcessor metadataProcessor,
            StatementContext statementContext,
            CrudOperations crud,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.environment = environment;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.metadataProcessor = metadataProcessor;
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
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        hostOnly = !environment.isStandalone() && request.getParameter(SERVER, null) == null;
        profile = request.getParameter(PROFILE, null);
    }

    @Override
    public FinderPath finderPath() {
        return hostOnly ? finderPathFactory.runtimeHostPath() : finderPathFactory.runtimeServerPath();
    }

    @Override
    protected void reload() {
        AddressTemplate template;
        if (environment.isStandalone()) {
            template = CORE_MANAGEMENT_TEMPLATE;
        } else {
            if (hostOnly) {
                template = CORE_MANAGEMENT_TEMPLATE;
            } else {
                template = AddressTemplate.of("/{selected.host}/{selected.server}/subsystem=core-management");
            }
        }
        ResourceAddress coreAddress = template.resolve(statementContext);
        Operation operation = new Operation.Builder(coreAddress, READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SERVICE)
                .build();
        dispatcher.execute(operation, coreResult -> {
            if (coreResult.asList().size() > 0) {
                Optional<ModelNode> configurationChangesResult = coreResult.asList().stream()
                        .filter(service -> service.asString().equals(CONFIGURATION_CHANGES)).findFirst();
                if (configurationChangesResult.isPresent()) {
                    ResourceAddress ccAddress = template.append("service=configuration-changes").resolve(statementContext);
                    Operation ccOperation = new Operation.Builder(ccAddress, LIST_CHANGES_OPERATION)
                            .build();
                    dispatcher.execute(ccOperation, ccResult -> getView().update(ccResult));
                } else {
                    getView().update(new ModelNode());
                }
            } else {
                getView().update(new ModelNode());
            }
        });
    }

    void launchAdd() {
        AddressTemplate template;
        if (environment.isStandalone()) {
            template = HOST_CONFIGURATION_CHANGES_TEMPLATE;
        } else {
            if (hostOnly) {
                template = HOST_CONFIGURATION_CHANGES_TEMPLATE;
            } else {
                template = PROFILE_CONFIGURATION_CHANGES_TEMPLATE.replaceWildcards(profile);
            }
        }
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(getEventBus(), resources) {
                    @Override
                    public void onMetadata(Metadata metadata) {
                        String id = Ids.build(Ids.CONFIGURATION_CHANGES, Ids.ADD);
                        Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, ADD)
                                .build();
                        ModelNode changeModel = new ModelNode();
                        Dialog dialog = new Dialog.Builder(resources.constants().configurationChanges())
                                .add(form.element())
                                .primary(resources.constants().yes(), () -> {
                                    boolean valid = form.save();
                                    // if the form contains validation error, don't close the dialog
                                    if (valid) {
                                        crud.addSingleton(Names.CONFIGURATION_CHANGES, template, form.getModel(),
                                                address -> reload());
                                    }
                                    return valid;
                                })
                                .secondary(resources.constants().cancel(), () -> true)
                                .closeIcon(true)
                                .closeOnEsc(true)
                                .build();

                        dialog.registerAttachable(form);
                        dialog.show();
                        form.edit(changeModel);
                    }
                }
        );
    }

    void disable() {
        AddressTemplate template;
        String type;
        String name;
        if (environment.isStandalone()) {
            template = HOST_CONFIGURATION_CHANGES_TEMPLATE;
            type = Names.STANDALONE_SERVER;
            name = Server.STANDALONE.getName();
        } else {
            if (hostOnly) {
                template = HOST_CONFIGURATION_CHANGES_TEMPLATE;
                type = HOST;
                name = statementContext.selectedHost();
            } else {
                template = PROFILE_CONFIGURATION_CHANGES_TEMPLATE.replaceWildcards(profile);
                type = SERVER;
                name = statementContext.selectedServer();
            }
        }

        DialogFactory.showConfirmation(resources.constants().configurationChanges(),
                resources.messages().removeConfigurationChangesQuestion(type, name),
                () -> {
                    ResourceAddress address = template.resolve(statementContext);
                    Operation operation = new Operation.Builder(address, REMOVE)
                            .build();
                    dispatcher.execute(operation, result -> getView().update(result));
                });
    }

    void viewRawChange(ConfigurationChange change) {
        showInDialog(change.asModelNode().toString());
    }

    void viewCli(ConfigurationChange change) {
        showInDialog(change.asCli());
    }

    void showInDialog(String textContent) {
        HTMLPreElement elem = pre().css(formControlStatic, wrap).get();
        elem.textContent = textContent;

        HTMLElement content = div()
                .add(elem)
                .style("overflow: scroll") //NON-NLS
                .get();

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


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(CONFIGURATION_CHANGES)
    @Requires({HOST_CONFIGURATION_CHANGES_ADDRESS, SERVER_CONFIGURATION_CHANGES_ADDRESS})
    public interface MyProxy extends ProxyPlace<ConfigurationChangesPresenter> {

    }

    public interface MyView extends HalView, HasPresenter<ConfigurationChangesPresenter> {
        void update(ModelNode model);
    }
    // @formatter:on
}
