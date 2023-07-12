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
package org.jboss.hal.core.modelbrowser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.core.modelbrowser.SingletonState.CHOOSE;
import static org.jboss.hal.core.modelbrowser.SingletonState.CREATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OBJECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STRING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_GROUP;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_PROFILE;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.modelBrowserButtons;
import static org.jboss.hal.resources.CSS.modelBrowserContent;
import static org.jboss.hal.resources.CSS.tagManagerContainer;
import static org.jboss.hal.resources.CSS.tagManagerTag;
import static org.jboss.hal.resources.CSS.tmTag;
import static org.jboss.hal.resources.CSS.tmTagRemove;
import static org.jboss.hal.resources.CSS.vh;
import static org.jboss.hal.resources.Ids.MODEL_BROWSER_ROOT;

/** Model browser element which can be embedded in other elements. */
public class ModelBrowser implements IsElement<HTMLElement> {

    private static final Logger logger = LoggerFactory.getLogger(ModelBrowser.class);

    static final HTMLElement PLACE_HOLDER_ELEMENT = div().element();

    private final CrudOperations crud;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;
    private final Stack<FilterInfo> filterStack;

    private final HTMLElement root;
    private final HTMLElement buttonGroup;
    private final HTMLButtonElement filter;
    private final HTMLButtonElement refresh;
    private final HTMLButtonElement collapse;
    private final HTMLElement treeContainer;
    private final HTMLElement content;
    private final ResourcePanel resourcePanel;
    private final ChildrenPanel childrenPanel;
    Tree<Context> tree;

    private boolean updateBreadcrumb;
    private int surroundingHeight;

    // ------------------------------------------------------ ui setup

    @Inject
    public ModelBrowser(CrudOperations crud,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            Dispatcher dispatcher,
            Environment environment,
            EventBus eventBus,
            Resources resources) {
        this.crud = crud;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.filterStack = new Stack<>();
        this.updateBreadcrumb = false;
        this.surroundingHeight = 0;

        buttonGroup = div().css(btnGroup, modelBrowserButtons)
                .add(filter = button().css(btn, btnDefault)
                        .on(click, event -> filter(tree.getSelected()))
                        .title(resources.constants().filter())
                        .add(Elements.i().css(fontAwesome(CSS.filter))).element())
                .add(refresh = button().css(btn, btnDefault)
                        .on(click, event -> refresh(tree.getSelected()))
                        .title(resources.constants().refresh())
                        .add(Elements.i().css(fontAwesome(CSS.refresh))).element())
                .add(collapse = button().css(btn, btnDefault)
                        .on(click, event -> collapse(tree.getSelected()))
                        .title(resources.constants().collapse())
                        .add(Elements.i().css(fontAwesome("minus"))).element())
                .element();

        treeContainer = div().css(CSS.treeContainer).element();
        content = div().css(modelBrowserContent).element();

        resourcePanel = new ResourcePanel(this, dispatcher, resources);
        for (HTMLElement element : resourcePanel) {
            content.appendChild(element);
        }
        resourcePanel.hide();

        childrenPanel = new ChildrenPanel(this, environment, dispatcher, metadataProcessor, resources);
        for (HTMLElement element : childrenPanel) {
            content.appendChild(element);
        }
        childrenPanel.hide();

        root = row()
                .add(column(4)
                        .addAll(buttonGroup, treeContainer))
                .add(column(8)
                        .add(content))
                .element();
    }

    private void adjustHeight() {
        int buttonGroup = (int) this.buttonGroup.offsetHeight;
        int treeContainerOffset = applicationOffset() + 2 * MARGIN_BIG + buttonGroup + MARGIN_SMALL + surroundingHeight;
        int contentOffset = applicationOffset() + 2 * MARGIN_BIG + surroundingHeight;

        treeContainer.style.height = vh(treeContainerOffset);
        content.style.height = vh(contentOffset);
    }

    private void initTree(ResourceAddress address, String text) {
        Context context = new Context(address, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(MODEL_BROWSER_ROOT, text, context)
                .asyncFolder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, new ReadChildren(dispatcher));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.element());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> onTreeSelection(selectionContext));
        childrenPanel.attach();
    }

    @SuppressWarnings("unchecked")
    private void emptyTree() {
        Context context = new Context(ResourceAddress.root(), Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(MODEL_BROWSER_ROOT, Names.NOT_AVAILABLE, context)
                .asyncFolder()
                .build();

        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, (node, callback) -> callback.result(new Node[0]));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.element());
        tree.attach();
        childrenPanel.hide();
        resourcePanel.hide();
    }

    // ------------------------------------------------------ event handler & co

    private void filter(Node<Context> node) {
        if (node != null && node.parent != null) {
            Node<Context> parent = tree.getNode(node.parent);
            FilterInfo filterInfo = new FilterInfo(parent, node);
            filterStack.add(filterInfo);
            filter(filterInfo);
            tree.openNode(MODEL_BROWSER_ROOT, () -> tree.selectNode(MODEL_BROWSER_ROOT));
        }
    }

    private void filter(FilterInfo filter) {
        elemental2.dom.Element oldFilterElement = buttonGroup.querySelector("." + tagManagerContainer);
        if (filter.filterText != null) {
            HTMLElement filterElement = div().css(tagManagerContainer)
                    .add(span().css(tmTag, tagManagerTag)
                            .add(span().textContent(filter.filterText))
                            .add(a().css(clickable, tmTagRemove)
                                    .on(click, event -> clearFilter()).textContent("x")))
                    .element();

            if (oldFilterElement != null) {
                buttonGroup.replaceChild(filterElement, oldFilterElement);
            } else {
                buttonGroup.appendChild(filterElement);
            }
        } else if (oldFilterElement != null) {
            buttonGroup.removeChild(oldFilterElement);
        }

        // reset tree
        tree.destroy();
        initTree(filter.address, filter.text);
    }

    private void clearFilter() {
        elemental2.dom.Element filterElement = buttonGroup.querySelector("." + tagManagerContainer);
        if (filterElement != null) {
            buttonGroup.removeChild(filterElement);
        }
        if (!filterStack.isEmpty()) {
            FilterInfo previousFilter = filterStack.pop();
            filter(filterStack.isEmpty() ? FilterInfo.ROOT : filterStack.peek());

            List<Task<FlowContext>> tasks = previousFilter.parents.stream()
                    .map(OpenNodeTask::new)
                    .collect(toList());
            sequential(new FlowContext(progress.get()), tasks)
                    .then(__ -> {
                        tree.selectNode(previousFilter.node.id);
                        return null;
                    })
                    .catch_(error -> {
                        logger.debug("Failed to restore selection {}", previousFilter.parents);
                        return null;
                    });
        }
    }

    private void refresh(Node<Context> node) {
        if (node != null) {
            updateNode(node);
            tree.refreshNode(node.id);
        }
    }

    private void collapse(Node<Context> node) {
        if (node != null) {
            tree.selectNode(node.id, true);
        }
    }

    private void onTreeSelection(SelectionContext<Context> context) {
        if ("ready".equals(context.action)) { // NON-NLS
            // only (de)selection events please
            return;
        }

        filter.disabled = context.selected.length == 0 ||
                !context.node.data.isFullyQualified() ||
                context.node.id.equals(MODEL_BROWSER_ROOT);
        refresh.disabled = context.selected.length == 0;
        collapse.disabled = context.selected.length == 0;

        resourcePanel.hide();
        childrenPanel.hide();
        if (context.selected.length == 0) {
            updateBreadcrumb(null);
        } else {
            updateNode(context.node);
        }
    }

    private void updateNode(Node<Context> node) {
        updateBreadcrumb(node);

        ResourceAddress address = node.data.getAddress();
        if (node.data.isFullyQualified()) {
            showResourceView(node, address);

        } else {
            childrenPanel.update(node, address);
            childrenPanel.show();
        }
    }

    private void updateBreadcrumb(Node<Context> node) {
        if (updateBreadcrumb) {
            ModelBrowserPath path = new ModelBrowserPath(this, node);
            eventBus.fireEvent(new ModelBrowserPathEvent(path));
        }
    }

    private void showResourceView(Node<Context> node, ResourceAddress address) {
        Node<Context> parent = tree.getNode(node.parent);
        AddressTemplate template = asGenericTemplate(parent, address);
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(Metadata metadata) {
                resourcePanel.update(node, node.data.getAddress(), metadata);
                resourcePanel.show();
            }
        });
    }

    void add(Node<Context> parent, List<String> children) {
        if (parent.data.hasSingletons()) {
            if (parent.data.getSingletons().size() == children.size()) {
                MessageEvent.fire(eventBus, Message.warning(resources.messages().allSingletonsExist()));

            } else if (parent.data.getSingletons().size() - children.size() == 1) {
                // no need to show a wizard - find the missing singleton
                HashSet<String> singletons = Sets.newHashSet(parent.data.getSingletons());
                singletons.removeAll(children);
                String singleton = singletons.iterator().next();

                ResourceAddress singletonAddress = parent.data.getAddress().getParent().add(parent.text, singleton);
                AddressTemplate template = asGenericTemplate(parent, singletonAddress);
                String id = Ids.build(parent.id, "singleton", Ids.ADD);
                crud.addSingleton(id, singleton, template, address -> refresh(parent));

            } else {
                // open wizard to choose the singleton
                Wizard<SingletonContext, SingletonState> wizard = new Wizard.Builder<SingletonContext, SingletonState>(
                        resources.messages().addResourceTitle(parent.text),
                        new SingletonContext(parent, children))

                        .addStep(CHOOSE, new ChooseSingletonStep(parent, children, resources))
                        .addStep(CREATE, new CreateSingletonStep(parent, metadataProcessor, progress,
                                eventBus, resources))

                        .onBack((context, currentState) -> currentState == CREATE ? CHOOSE : null)
                        .onNext((context, currentState) -> currentState == CHOOSE ? CREATE : null)

                        .onFinish((wzrd, context) -> {
                            Operation.Builder builder = new Operation.Builder(fqAddress(parent, context.singleton),
                                    ADD);
                            if (context.modelNode != null) {
                                builder.payload(context.modelNode);
                            }
                            dispatcher.execute(builder.build(),
                                    result -> {
                                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                .addResourceSuccess(parent.text, context.singleton)));
                                        refresh(parent);
                                    });
                        })
                        .build();
                wizard.show();
            }

        } else {
            AddressTemplate template = asGenericTemplate(parent, parent.data.getAddress());
            metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
                @Override
                public void onMetadata(Metadata metadata) {
                    flattenDescription(metadata.getDescription().get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES));
                    String title = new LabelBuilder().label(parent.text);
                    ResourceNameItem resourceNameItem = new ResourceNameItem();
                    String id = Ids.build(parent.id, "add");
                    ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                            .unboundFormItem(resourceNameItem, 0)
                            .fromRequestProperties()
                            .panelForOptionalAttributes()
                            .build();

                    AddResourceDialog dialog = new AddResourceDialog(
                            resources.messages().addResourceTitle(title),
                            form, (name1, model) -> {
                                unflattenModel(model);
                                crud.add(title, resourceNameItem.getValue(), fqAddress(parent, resourceNameItem.getValue()),
                                        model, (n, a) -> refresh(parent));
                            });
                    dialog.show();
                }
            });
        }
    }

    private void flattenDescription(ModelNode model) {
        for (Property p : model.asPropertyList()) {
            if (p.getValue().get(TYPE).asString().equalsIgnoreCase(OBJECT)
                    && !p.getValue().get(VALUE_TYPE).asString().equalsIgnoreCase(STRING)) {

                model.remove(p.getName());

                for (Property nested : p.getValue().get(VALUE_TYPE).asPropertyList()) {
                    model.get(p.getName() + "." + nested.getName()).set(nested.getValue());
                }
            }
        }
    }

    private void unflattenModel(ModelNode model) {
        if (!model.isDefined()) {
            return;
        }
        for (Property p : model.asPropertyList()) {
            if (p.getName().indexOf('.') < 0) {
                continue;
            }

            String[] split = p.getName().split("\\.");

            model.remove(p.getName());
            model.get(split[0]).get(split[1]).set(p.getValue());
        }
    }

    static AddressTemplate asGenericTemplate(Node<Context> node, ResourceAddress address) {
        return AddressTemplate.of(address, (name, value, first, last, index, size) -> {
            String segment;
            if (PROFILE.equals(name)) {
                segment = SELECTED_PROFILE.expression();
            } else if (SERVER_GROUP.equals(name)) {
                segment = SELECTED_GROUP.expression();
            } else {
                if (last && node != null && node.data != null && !node.data.hasSingletons()) {
                    segment = name + "=*";
                } else {
                    segment = name + "=" + ModelNodeHelper.encodeValue(value);
                }
            }
            return segment;
        });
    }

    private ResourceAddress fqAddress(Node<Context> parent, String child) {
        return parent.data.getAddress().getParent().add(parent.text, child);
    }

    void remove(ResourceAddress address) {
        crud.remove(address.lastName(), address.lastValue(), address, () -> refresh(tree.getSelected()));
    }

    void save(ResourceAddress address, Map<String, Object> changedValues, Metadata metadata) {
        crud.save(address.lastName(), address.lastValue(), address, changedValues, metadata,
                () -> refresh(tree.getSelected()));
    }

    void reset(ResourceAddress address, Form<ModelNode> form, Metadata metadata) {
        crud.reset(address.lastName(), address.lastValue(), address, form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        refresh(tree.getSelected());
                    }
                });
    }

    // ------------------------------------------------------ public API

    /**
     * Use this method if you embed the model browser into an application view and if you have additional elements before or
     * after the model browser. This method should be called when the application view is attached or before
     * {@link #setRoot(ResourceAddress, boolean)} is called.
     *
     * @param surroundingHeight the sum of the height of all surrounding elements
     */
    public void setSurroundingHeight(int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
    }

    /**
     * Entry point to show the specified address.
     *
     * @param root the root address for this model browser
     * @param updateBreadcrumb {@code true} if this model browser should fire {@link ModelBrowserPathEvent}s
     */
    public void setRoot(ResourceAddress root, boolean updateBreadcrumb) {
        this.updateBreadcrumb = updateBreadcrumb;

        String resource = root.equals(ResourceAddress.root()) ? Names.MANAGEMENT_MODEL
                : SafeHtmlUtils.htmlEscapeAllowEntities(root.lastValue());
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser.setRoot() must be called with a concrete address.");
        }
        // TODO Removing a filter in a scoped model browser does not work
        Elements.setVisible(filter, root.equals(ResourceAddress.root()));

        Operation ping = new Operation.Builder(root, READ_RESOURCE_OPERATION).build();
        dispatcher.execute(ping,
                result -> {
                    initTree(root, resource);
                    tree.openNode(MODEL_BROWSER_ROOT, () -> resourcePanel.tabs.showTab(0));
                    tree.selectNode(MODEL_BROWSER_ROOT);

                    adjustHeight();
                },

                (operation, failure) -> {
                    emptyTree();
                    MessageEvent.fire(eventBus, Message.error(resources.messages().unknownResource(),
                            resources.messages().unknownResourceDetails(root.toString(), failure)));

                    adjustHeight();
                });
    }

    public void select(String id, boolean closeSelected) {
        tree.selectNode(id, closeSelected);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private static class FilterInfo {

        static final FilterInfo ROOT = new FilterInfo(null, null);

        final ResourceAddress address;
        final Node<Context> node;
        final String text;
        final String filterText;
        final List<String> parents;

        private FilterInfo(Node<Context> parent, Node<Context> child) {
            this.address = child == null ? ResourceAddress.root() : child.data.getAddress();
            this.node = child;
            this.text = child == null ? Names.MANAGEMENT_MODEL : child.text;
            this.filterText = parent == null || child == null ? null : parent.text + "=" + child.text;
            this.parents = child == null ? emptyList() : asList(child.parents);
            if (!parents.isEmpty()) {
                Collections.reverse(parents);
                parents.remove(0); // get rif of the artificial root
            }
        }
    }

    private final class OpenNodeTask implements Task<FlowContext> {

        private final String id;

        private OpenNodeTask(String id) {
            this.id = id;
        }

        @Override
        public Promise<FlowContext> apply(final FlowContext context) {
            return new Promise<>((resolve, reject) -> {
                if (tree.getNode(id) != null) {
                    tree.openNode(id, () -> resolve.onInvoke(context));
                } else {
                    resolve.onInvoke(context);
                }
            });
        }
    }

    /**
     * Modified name item that uses the "_name" string as the field name (to avoid clashing with resource specific "name"
     * attributes, which are rare but do exist).
     */
    private static final class ResourceNameItem extends TextBoxItem {

        private static final Constants CONSTANTS = GWT.create(Constants.class);

        public ResourceNameItem() {
            super("_" + ModelDescriptionConstants.NAME, CONSTANTS.resourceName());
            setRequired(true);
            setExpressionAllowed(false);
        }
    }
}
