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
package org.jboss.hal.core.modelbrowser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionChangeHandler.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.JsHelper.asList;
import static org.jboss.hal.core.modelbrowser.SingletonState.CHOOSE;
import static org.jboss.hal.core.modelbrowser.SingletonState.CREATE;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.core.ui.Skeleton.applicationOffset;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_GROUP;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.MODEL_BROWSER_ROOT;

/**
 * Model browser element which can be embedded in other elements.
 *
 * @author Harald Pehl
 */
public class ModelBrowser implements HasElements {

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
            this.parents = child == null ? Collections.emptyList() : asList(child.parents);
            if (!parents.isEmpty()) {
                Collections.reverse(parents);
                parents.remove(0); // get rif of the artificial root
            }
        }
    }


    private class OpenNodeFunction implements Function<FunctionContext> {

        private final String id;

        private OpenNodeFunction(final String id) {this.id = id;}

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (tree.api().getNode(id) != null) {
                tree.api().openNode(id, control::proceed);
            } else {
                control.proceed();
            }
        }
    }


    private static final String FILTER_ELEMENT = "filterElement";
    private static final String REFRESH_ELEMENT = "refreshElement";
    private static final String COLLAPSE_ELEMENT = "collapseElement";

    static final Element PLACE_HOLDER_ELEMENT = Browser.getDocument().createDivElement();

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ModelBrowser.class);

    private final CrudOperations crud;
    private MetadataProcessor metadataProcessor;
    private Provider<Progress> progress;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;
    private final Stack<FilterInfo> filterStack;

    private final Iterable<Element> rows;
    private final Element buttonGroup;
    private final ButtonElement filter;
    private final ButtonElement refresh;
    private final ButtonElement collapse;
    private final Element treeContainer;
    private final Element content;
    private final ResourcePanel resourcePanel;
    private final ChildrenPanel childrenPanel;
    Tree<Context> tree;

    private boolean updateBreadcrumb;
    private int surroundingHeight;


    // ------------------------------------------------------ ui setup

    @Inject
    public ModelBrowser(final CrudOperations crud,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Resources resources) {
        this.crud = crud;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.filterStack = new Stack<>();
        this.updateBreadcrumb = false;
        this.surroundingHeight = 0;

        // @formatter:off
        Elements.Builder buttonsBuilder = new Elements.Builder()
            .div().css(btnGroup, modelBrowserButtons)
                .button().rememberAs(FILTER_ELEMENT).on(click, event -> filter(tree.api().getSelected())).css(btn, btnDefault)
                    .add("i").css(fontAwesome(CSS.filter))
                .end()
                .button().rememberAs(REFRESH_ELEMENT).on(click, event -> refresh(tree.api().getSelected())).css(btn, btnDefault)
                    .add("i").css(fontAwesome(CSS.refresh))
                .end()
                .button().rememberAs(COLLAPSE_ELEMENT).on(click, event -> collapse(tree.api().getSelected())).css(btn, btnDefault)
                    .add("i").css(fontAwesome("minus"))
                .end()
            .end();
        // @formatter:on
        filter = buttonsBuilder.referenceFor(FILTER_ELEMENT);
        refresh = buttonsBuilder.referenceFor(REFRESH_ELEMENT);
        collapse = buttonsBuilder.referenceFor(COLLAPSE_ELEMENT);
        buttonGroup = buttonsBuilder.build();
        treeContainer = new Elements.Builder().div().css(CSS.treeContainer).end().build();
        content = new Elements.Builder().div().css(modelBrowserContent).end().build();

        resourcePanel = new ResourcePanel(this, dispatcher, resources);
        for (Element element : resourcePanel.asElements()) {
            content.appendChild(element);
        }
        resourcePanel.hide();

        childrenPanel = new ChildrenPanel(this, dispatcher, resources);
        for (Element element : childrenPanel.asElements()) {
            content.appendChild(element);
        }
        childrenPanel.hide();

        // @formatter:off
        rows =  new LayoutBuilder()
            .row()
                .column(4)
                    .add(buttonGroup)
                    .add(treeContainer)
                .end()
                .column(8).add(content).end()
            .end()
        .elements();
        // @formatter:on
    }

    private void adjustHeight() {
        int buttonGroup = this.buttonGroup.getOffsetHeight();
        int treeContainerOffset = applicationOffset() + 2 * MARGIN_BIG + buttonGroup + MARGIN_SMALL + surroundingHeight;
        int contentOffset = applicationOffset() + 2 * MARGIN_BIG + surroundingHeight;

        treeContainer.getStyle().setHeight(vh(treeContainerOffset));
        content.getStyle().setHeight(vh((contentOffset)));
    }

    private void initTree(ResourceAddress address, String text) {
        Context context = new Context(address, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(MODEL_BROWSER_ROOT, text, context)
                .asyncFolder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, new ReadChildren(dispatcher));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> onTreeSelection(selectionContext));
        childrenPanel.attach();
    }

    private void emptyTree() {
        Context context = new Context(ResourceAddress.root(), Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(MODEL_BROWSER_ROOT, Names.NOT_AVAILABLE, context)
                .asyncFolder()
                .build();

        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, (node, callback) -> callback.result(JsArrayOf.create()));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());
        tree.attach();
        childrenPanel.hide();
        resourcePanel.hide();
    }


    // ------------------------------------------------------ event handler & co

    private void filter(Node<Context> node) {
        if (node != null && node.parent != null) {
            Node<Context> parent = tree.api().getNode(node.parent);
            FilterInfo filterInfo = new FilterInfo(parent, node);
            filterStack.add(filterInfo);
            filter(filterInfo);
            tree.api().openNode(MODEL_BROWSER_ROOT, () -> tree.select(MODEL_BROWSER_ROOT, false));
        }
    }

    private void filter(FilterInfo filter) {
        Element oldFilterElement = buttonGroup.querySelector("." + tagManagerContainer);
        if (filter.filterText != null) {
            // @formatter:off
            Element filterElement = new Elements.Builder()
                .div().css(tagManagerContainer)
                    .span().css(tmTag, tagManagerTag)
                        .span().textContent(filter.filterText).end()
                        .a().css(clickable, tmTagRemove).on(click, event -> clearFilter()).textContent("x").end() //NON-NLS
                    .end()
                .end().build();
            // @formatter:on

            if (oldFilterElement != null) {
                buttonGroup.replaceChild(filterElement, oldFilterElement);
            } else {
                buttonGroup.appendChild(filterElement);
            }
        } else if (oldFilterElement != null) {
            buttonGroup.removeChild(oldFilterElement);
        }

        // reset tree
        tree.api().destroy(false);
        initTree(filter.address, filter.text);
    }

    private void clearFilter() {
        Element filterElement = buttonGroup.querySelector("." + tagManagerContainer);
        if (filterElement != null) {
            buttonGroup.removeChild(filterElement);
        }
        if (!filterStack.isEmpty()) {
            FilterInfo previousFilter = filterStack.pop();
            filter(filterStack.isEmpty() ? FilterInfo.ROOT : filterStack.peek());

            List<OpenNodeFunction> functions = previousFilter.parents.stream()
                    .map(OpenNodeFunction::new)
                    .collect(toList());
            Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    logger.debug("Failed to restore selection {}", previousFilter.parents);
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    tree.select(previousFilter.node.id, false);
                }
            };
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                    functions.toArray(new Function[functions.size()]));
        }
    }

    private void refresh(final Node<Context> node) {
        if (node != null) {
            updateNode(node);
            tree.api().refreshNode(node.id);
        }
    }

    private void collapse(Node<Context> node) {
        if (node != null) {
            tree.select(node.id, true);
        }
    }

    private void onTreeSelection(SelectionContext<Context> context) {
        if ("ready".equals(context.action)) { //NON-NLS
            // only (de)selection events please
            return;
        }

        filter.setDisabled(context.selected.isEmpty() ||
                !context.node.data.isFullyQualified() ||
                context.node.id.equals(MODEL_BROWSER_ROOT));
        refresh.setDisabled(context.selected.isEmpty());
        collapse.setDisabled(context.selected.isEmpty());

        resourcePanel.hide();
        childrenPanel.hide();
        if (context.selected.isEmpty()) {
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
        Node<Context> parent = tree.api().getNode(node.parent);
        AddressTemplate template = asGenericTemplate(parent, address);
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                resourcePanel.update(node, node.data.getAddress(), metadata);
                resourcePanel.show();
            }
        });
    }

    void add(final Node<Context> parent, final List<String> children) {
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
                String id = Ids.build(parent.id, "singleton", Ids.ADD_SUFFIX);
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
                            Operation.Builder builder = new Operation.Builder(ADD,
                                    fqAddress(parent, context.singleton));
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
                    AddResourceDialog dialog = new AddResourceDialog(
                            Ids.build(parent.id, "add"),
                            resources.messages().addResourceTitle(parent.text),
                            metadata,
                            (name, modelNode) ->
                                    crud.add(parent.text, name, fqAddress(parent, name), modelNode,
                                            (n, a) -> refresh(parent)));
                    dialog.show();
                }
            });
        }
    }

    static AddressTemplate asGenericTemplate(Node<Context> node, ResourceAddress address) {
        return AddressTemplate.of(address, (name, value, first, last, index) -> {
            String segment;
            if (PROFILE.equals(name)) {
                segment = SELECTED_PROFILE.variable();
            } else if (SERVER_GROUP.equals(name)) {
                segment = SELECTED_GROUP.variable();
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
        crud.remove(address.lastName(), address.lastValue(), address, () -> refresh(tree.api().getSelected()));
    }

    void save(ResourceAddress address, Map<String, Object> changedValues, Metadata metadata) {
        crud.save(address.lastName(), address.lastValue(), address, changedValues, metadata,
                () -> refresh(tree.api().getSelected()));
    }

    void reset(ResourceAddress address, Form<ModelNode> form, Metadata metadata) {
        crud.reset(address.lastName(), address.lastValue(), address, form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        refresh(tree.api().getSelected());
                    }
                });
    }


    // ------------------------------------------------------ public API

    /**
     * Use this method if you embed the model browser into an application view and if you have additional elements
     * before or after the model browser. This method should be called when the application view is attached or before
     * {@link #setRoot(ResourceAddress, boolean)} is called.
     *
     * @param surroundingHeight the sum of the height of all surrounding elements
     */
    public void setSurroundingHeight(final int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
    }

    /**
     * Entry point to show the specified address.
     *
     * @param root             the root address for this model browser
     * @param updateBreadcrumb {@code true} if this model browser should fire {@link ModelBrowserPathEvent}s
     */
    public void setRoot(ResourceAddress root, boolean updateBreadcrumb) {
        this.updateBreadcrumb = updateBreadcrumb;

        String resource = root.equals(ResourceAddress.root()) ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser.setRoot() must be called with a concrete address.");
        }
        // TODO Removing a filter in a scoped model browser does not work
        Elements.setVisible(filter, root.equals(ResourceAddress.root()));

        Operation ping = new Operation.Builder(READ_RESOURCE_OPERATION, root).build();
        dispatcher.execute(ping,
                result -> {
                    initTree(root, resource);
                    tree.api().openNode(MODEL_BROWSER_ROOT, () -> resourcePanel.tabs.showTab(0));
                    tree.select(MODEL_BROWSER_ROOT, false);

                    adjustHeight();
                },

                (operation, failure) -> {
                    emptyTree();
                    MessageEvent.fire(eventBus, Message.error(resources.messages().unknownResource(),
                            resources.messages().unknownResourceDetails(root.toString(), failure)));

                    adjustHeight();
                },

                (operation, exception) -> {
                    emptyTree();
                    MessageEvent.fire(eventBus, Message.error(resources.messages().unknownResource(),
                            resources.messages().unknownResourceDetails(root.toString(), exception.getMessage())));

                    adjustHeight();
                });
    }

    public void select(final String id, final boolean closeSelected) {
        tree.select(id, closeSelected);
    }

    @Override
    public Iterable<Element> asElements() {
        return rows;
    }
}
