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
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
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

public class JcaPresenter
        extends ApplicationFinderPresenter<JcaPresenter.MyView, JcaPresenter.MyProxy>
        implements SupportsExpertMode {

    private static final String EQUALS = "=";

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public JcaPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
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
        return finderPathFactory.configurationSubsystemPath(JCA_TEMPLATE.lastValue());
    }

    @Override
    protected void reload() {
        crud.read(JCA_TEMPLATE, 1, result -> getView().update(result));
    }


    // ------------------------------------------------------ generic crud

    void add(String type, String name, AddressTemplate template, ModelNode payload) {
        crud.add(type, name, template, payload, (n, a) -> reload());
    }

    void saveResource(AddressTemplate template, String name, Map<String, Object> changedValues,
            SafeHtml successMessage) {
        crud.save(name, template, changedValues, successMessage, this::reload);
    }

    void saveSingleton(AddressTemplate template, Map<String, Object> changedValues, SafeHtml successMessage) {
        crud.saveSingleton(template, changedValues, successMessage, this::reload);
    }

    void resetResource(AddressTemplate template, String type, String name, Form<NamedNode> form, Metadata metadata) {
        crud.reset(type, name, template, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reload();
            }
        });
    }

    void resetSingleton(String type, AddressTemplate template, Form<ModelNode> form, Metadata metadata) {
        crud.resetSingleton(type, template, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeSingleton(String type, AddressTemplate template, Form<ModelNode> form) {
        crud.removeSingleton(type, template, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }


    // ------------------------------------------------------ distributed work manager

    void addDistributedWorkManager(String type, String name, ModelNode payload) {
        ModelNode maxThreads = payload.remove(MAX_THREADS);
        ModelNode queueLength = payload.remove(QUEUE_LENGTH);
        ModelNode srtModel = new ModelNode();
        srtModel.get(MAX_THREADS).set(maxThreads);
        srtModel.get(QUEUE_LENGTH).set(queueLength);
        Composite composite = new Composite();
        ResourceAddress dwmAddress = DISTRIBUTED_WORKMANAGER_TEMPLATE.resolve(statementContext, name);
        Operation addDwm = new Operation.Builder(dwmAddress, ADD)
                .payload(payload)
                .build();
        composite.add(addDwm);

        ResourceAddress srtAddress = DISTRIBUTED_WORKMANAGER_SRT_TEMPLATE.resolve(statementContext, name, name);
        Operation addSrt = new Operation.Builder(srtAddress, ADD)
                .payload(srtModel)
                .build();
        composite.add(addSrt);
        dispatcher.execute(composite, (CompositeResult compositeResult) -> {
                    MessageEvent.fire(getEventBus(), Message.success(
                            resources.messages().addResourceSuccess(type, name)));
                    reload();
                },
                (operation, failure) -> MessageEvent.fire(getEventBus(), Message.error(
                        resources.messages().addResourceError(type, failure))),
                (operation, e) -> MessageEvent.fire(getEventBus(), Message.error(
                        resources.messages().addResourceError(type, e.getMessage()))));
    }


    // ------------------------------------------------------ tracer

    void addTracer() {
        crud.addSingleton(new LabelBuilder().label(TRACER_TEMPLATE.lastName()), TRACER_TEMPLATE, address -> reload());
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
                        .fromRequestProperties()
                        .unboundFormItem(typeItem, 0)
                        .include(MAX_THREADS, QUEUE_LENGTH, THREAD_FACTORY)
                        .unsorted()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(
                        resources.messages().addResourceTitle(THREAD_POOL), form,
                        (name, modelNode) -> {
                            String type = typeItem.getValue();
                            AddressTemplate tpTemplate = Names.LONG_RUNNING.equals(type)
                                    ? workmanagerTemplate.append(WORKMANAGER_LRT_TEMPLATE.lastName() + EQUALS + workmanager)
                                    : workmanagerTemplate.append(WORKMANAGER_SRT_TEMPLATE.lastName() + EQUALS + workmanager);
                            ResourceAddress address = tpTemplate.resolve(statementContext, workmanager);
                            Operation operation = new Operation.Builder(address, ADD)
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

    void saveThreadPool(AddressTemplate workmanagerTemplate, String workmanager, ThreadPool threadPool,
            Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(
                threadPool.isLongRunning() ? WORKMANAGER_LRT_TEMPLATE : WORKMANAGER_SRT_TEMPLATE);
        ResourceAddress address = threadPoolAddress(workmanagerTemplate, workmanager, threadPool);
        crud.save(THREAD_POOL, threadPool.getName(), address, changedValues, metadata,
                () -> loadThreadPools(workmanagerTemplate, workmanager));
    }

    void resetThreadPool(AddressTemplate workmanagerTemplate, String workmanager, ThreadPool threadPool,
            Form<ThreadPool> form) {
        Metadata metadata = metadataRegistry.lookup(
                threadPool.isLongRunning() ? WORKMANAGER_LRT_TEMPLATE : WORKMANAGER_SRT_TEMPLATE);
        ResourceAddress address = threadPoolAddress(workmanagerTemplate, workmanager, threadPool);
        crud.reset(THREAD_POOL, threadPool.getName(), address, form, metadata, new FinishReset<ThreadPool>(form) {
            @Override
            public void afterReset(Form<ThreadPool> form) {
                loadThreadPools(workmanagerTemplate, workmanager);
            }
        });
    }

    void removeThreadPool(AddressTemplate workmanagerTemplate, String workmanager, ThreadPool threadPool) {
        ResourceAddress address = threadPoolAddress(workmanagerTemplate, workmanager, threadPool);
        crud.remove(THREAD_POOL, threadPool.getName(), address,
                () -> loadThreadPools(workmanagerTemplate, workmanager));
    }

    private Composite threadPoolsOperation(AddressTemplate template, String name) {
        Operation lrtOp = new Operation.Builder(template.resolve(statementContext, name),
                READ_CHILDREN_RESOURCES_OPERATION
        )
                .param(CHILD_TYPE, WORKMANAGER_LRT_TEMPLATE.lastName())
                .build();
        Operation srtOp = new Operation.Builder(template.resolve(statementContext, name),
                READ_CHILDREN_RESOURCES_OPERATION
        )
                .param(CHILD_TYPE, WORKMANAGER_SRT_TEMPLATE.lastName())
                .build();
        return new Composite(lrtOp, srtOp);
    }

    private ResourceAddress threadPoolAddress(AddressTemplate workmanagerTemplate, String workmanager,
            ThreadPool threadPool) {
        AddressTemplate template = threadPool.isLongRunning()
                ? workmanagerTemplate.append(WORKMANAGER_LRT_TEMPLATE.lastName() + EQUALS + threadPool.getName())
                : workmanagerTemplate.append(WORKMANAGER_SRT_TEMPLATE.lastName() + EQUALS + threadPool.getName());
        return template.resolve(statementContext, workmanager);
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(JCA_ADDRESS)
    @NameToken(NameTokens.JCA)
    public interface MyProxy extends ProxyPlace<JcaPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<JcaPresenter> {
        void update(ModelNode payload);
        void updateThreadPools(AddressTemplate workmanagerTemplate, String workmanager,
                List<Property> lrt, List<Property> srt);
    }
    // @formatter:on
}
