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
package org.jboss.hal.client.configuration.subsystem.jca;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.jca.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Names.THREAD_POOL;

/**
 * @author Harald Pehl
 */
public class JcaPresenter
        extends ApplicationFinderPresenter<JcaPresenter.MyView, JcaPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(JCA_ADDRESS)
    @NameToken(NameTokens.JCA)
    public interface MyProxy extends ProxyPlace<JcaPresenter> {}

    public interface MyView extends HalView, HasPresenter<JcaPresenter> {
        void update(ModelNode payload);
        void updateThreadPools(AddressTemplate workmanagerTemplate, String workmanager,
                List<Property> lrt, List<Property> srt);
    }
    // @formatter:on

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public JcaPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final CrudOperations crud,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return JCA_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(JCA_TEMPLATE.lastValue());
    }

    @Override
    protected void reload() {
        crud.read(JCA_TEMPLATE, 1, result -> getView().update(result));
    }


    // ------------------------------------------------------ generic crud

    void add(final String type, final String name, final AddressTemplate template, final ModelNode payload) {
        crud.add(type, name, template, payload, (n, a) -> reload());
    }

    void saveResource(final AddressTemplate template, final String name, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        crud.save(name, template, changedValues, successMessage, this::reload);
    }

    void saveSingleton(final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        crud.saveSingleton(template, changedValues, successMessage, this::reload);
    }

    void resetResource(final AddressTemplate template, final String type, final String name,
            final Form<NamedNode> form, final Metadata metadata) {
        crud.reset(type, name, template, form, metadata, this::reload);
    }

    void resetSingleton(final String type, final AddressTemplate template, final Form<ModelNode> form,
            final Metadata metadata) {
        crud.resetSingleton(type, template, form, metadata, this::reload);
    }


    // ------------------------------------------------------ tracer

    void addTracer() {
        crud.addSingleton(new LabelBuilder().label(TRACER_TEMPLATE.lastKey()), TRACER_TEMPLATE, (n, a) -> reload());
    }


    // ------------------------------------------------------ thread pools (for normal and distributed work managers)

    /**
     * Used to bring up the dialog to add thread pools for the normal and the distributed work managers.
     * <p>
     * Only one long and one short running thread pool is allowed per (distributed) work manager. This method takes
     * care of showing the right attributes in the dialog. If there are already long and short running thread pools
     * attached to the work manager an error message is shown.
     */
    void launchAddThreadPool(AddressTemplate workmanagerTemplate, String workmanager) {
        dispatcher.execute(threadPoolsOperation(workmanagerTemplate, workmanager), (CompositeResult cr) -> {
            boolean lrtPresent = !cr.step(0).get(RESULT).asPropertyList().isEmpty();
            boolean srtPresent = !cr.step(1).get(RESULT).asPropertyList().isEmpty();

            if (lrtPresent && srtPresent) {
                MessageEvent.fire(getEventBus(), Message.error(resources.messages().allThreadPoolsExist()));

            } else {
                FormItem<String> typeItem;
                if (!lrtPresent && !srtPresent) {
                    typeItem = new SingleSelectBoxItem(TYPE, resources.constants().type(), asList(
                            Names.LONG_RUNNING, Names.SHORT_RUNNING), false);
                    typeItem.setRequired(true);

                } else {
                    typeItem = new TextBoxItem(TYPE, resources.constants().type());
                    typeItem.setValue(lrtPresent ? Names.SHORT_RUNNING : Names.LONG_RUNNING);
                    typeItem.setEnabled(false);
                }

                // for the metadata it doesn't matter whether we use the LRT or SRT template nor
                // whether we use the normal or distributed workmanager version
                Metadata metadata = metadataRegistry.lookup(WORKMANAGER_LRT_TEMPLATE);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.JCA_THREAD_POOL_ADD, metadata)
                        .addFromRequestProperties()
                        .unboundFormItem(typeItem, 0)
                        .unboundFormItem(new NameItem(), 1)
                        .include(MAX_THREADS, QUEUE_LENGTH, THREAD_FACTORY)
                        .unsorted()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(
                        resources.messages().addResourceTitle(THREAD_POOL), form,
                        (name, modelNode) -> {
                            String type = typeItem.getValue();
                            AddressTemplate tpTemplate = Names.LONG_RUNNING.equals(type)
                                    ? workmanagerTemplate.append(WORKMANAGER_LRT_TEMPLATE.lastKey() + "=" + name)
                                    : workmanagerTemplate.append(WORKMANAGER_SRT_TEMPLATE.lastKey() + "=" + name);
                            ResourceAddress address = tpTemplate.resolve(statementContext, workmanager);
                            Operation operation = new Operation.Builder(ADD, address)
                                    .param(NAME, name)
                                    .payload(modelNode)
                                    .build();
                            dispatcher.execute(operation, result -> {
                                MessageEvent.fire(getEventBus(),
                                        Message.success(resources.messages()
                                                .addResourceSuccess(THREAD_POOL, name)));
                                loadThreadPools(workmanagerTemplate, workmanager);
                            });
                        });
                dialog.show();
            }
        });
    }

    void loadThreadPools(AddressTemplate workmanagerTemplate, String workmanager) {
        dispatcher.execute(threadPoolsOperation(workmanagerTemplate, workmanager), (CompositeResult result) -> {
            List<Property> lrt = result.step(0).get(RESULT).asPropertyList();
            List<Property> srt = result.step(1).get(RESULT).asPropertyList();
            getView().updateThreadPools(workmanagerTemplate, workmanager, lrt, srt);
        });
    }

    void removeThreadPool(AddressTemplate workmanagerTemplate, String workmanager, String threadPoolName,
            boolean longRunning) {
        AddressTemplate template = longRunning
                ? workmanagerTemplate.append(WORKMANAGER_LRT_TEMPLATE.lastKey() + "=" + threadPoolName)
                : workmanagerTemplate.append(WORKMANAGER_SRT_TEMPLATE.lastKey() + "=" + threadPoolName);
        ResourceAddress address = template.resolve(statementContext, workmanager);
        crud.remove(THREAD_POOL, threadPoolName, address, () -> loadThreadPools(workmanagerTemplate, workmanager));
    }

    private Composite threadPoolsOperation(final AddressTemplate template, final String name) {
        Operation lrtOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                template.resolve(statementContext, name))
                .param(CHILD_TYPE, WORKMANAGER_LRT_TEMPLATE.lastKey())
                .build();
        Operation srtOp = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                template.resolve(statementContext, name))
                .param(CHILD_TYPE, WORKMANAGER_SRT_TEMPLATE.lastKey())
                .build();
        return new Composite(lrtOp, srtOp);
    }
}
