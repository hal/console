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
package org.jboss.hal.core.modelbrowser;

import com.google.common.collect.FluentIterable;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionChangeHandler.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.js.JsHelper.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Key.ANY_GROUP;
import static org.jboss.hal.meta.StatementContext.Key.ANY_PROFILE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NYI;

/**
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
            this.address = child == null ? ResourceAddress.ROOT : child.data.getAddress();
            this.node = child == null ? null : child;
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


    private abstract class DefaultMetadataCallback implements MetadataProcessor.MetadataCallback {

        private final ResourceAddress address;

        DefaultMetadataCallback(final ResourceAddress address) {this.address = address;}

        @Override
        public void onError(final Throwable error) {
                MessageEvent.fire(eventBus, Message.error(resources.constants().metadataError(), error.getMessage()));
        }
    }


    private static final int MARGIN_BIG = 20; // keep this in sync with the
    private static final int MARGIN_SMALL = 10; // margins in modelbrowser.less
    private static final String FILTER_ELEMENT = "filterElement";
    private static final String REFRESH_ELEMENT = "refreshElement";
    private static final String COLLAPSE_ELEMENT = "collapseElement";
    static final String ROOT_ID = IdBuilder.build(Ids.MODEL_BROWSER, "root");
    static final Element PLACE_HOLDER_ELEMENT = Browser.getDocument().createDivElement();

    private static final Logger logger = LoggerFactory.getLogger(ModelBrowser.class);

    private MetadataProcessor metadataProcessor;
    private Provider<Progress> progress;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;
    private final OperationFactory operationFactory;
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


    // ------------------------------------------------------ ui setup

    @Inject
    public ModelBrowser(final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Resources resources) {
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.operationFactory = new OperationFactory();
        this.filterStack = new Stack<>();

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
        treeContainer = new Elements.Builder().div().css(modelBrowserTree).end().build();
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
                .column(0, 4)
                    .add(buttonGroup)
                    .add(treeContainer)
                .end()
                .column(0, 8)
                    .add(content)
                .end()
            .end()
        .elements();
        // @formatter:on

        Browser.getWindow().setOnresize(event -> adjustHeight());
    }

    private void adjustHeight() {
        int window = Browser.getWindow().getInnerHeight();
        int navigation = Skeleton.navigationHeight();
        int footer = Skeleton.footerHeight();
        int buttonGroup = this.buttonGroup.getOffsetHeight();
        if (navigation > 0 && footer > 0) {
            int height = window - navigation - footer;
            // keep this in sync with the margins in modelbrowser.less
            treeContainer.getStyle().setHeight(height - 2 * MARGIN_BIG - buttonGroup - 2 * MARGIN_SMALL, PX);
            content.getStyle().setHeight(height - 2 * MARGIN_BIG - 2 * MARGIN_SMALL, PX);
        }
    }

    private void initTree(ResourceAddress address, String text) {
        Context context = new Context(address, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(ROOT_ID, text, context)
                .folder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, new ReadChildren(dispatcher));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> onTreeSelection(selectionContext));
        childrenPanel.attach();
    }


    // ------------------------------------------------------ event handler & co

    private void filter(Node<Context> node) {
        if (node != null && node.parent != null) {
            Node<Context> parent = tree.api().getNode(node.parent);
            FilterInfo filterInfo = new FilterInfo(parent, node);
            filterStack.add(filterInfo);
            filter(filterInfo);
            tree.api().openNode(ROOT_ID, () -> select(ROOT_ID, false));
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

            List<OpenNodeFunction> functions = FluentIterable
                    .from(previousFilter.parents)
                    .transform(OpenNodeFunction::new).toList();
            Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    logger.debug("Failed to restore selection {}", previousFilter.parents); //NON-NLS
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    select(previousFilter.node.id, false);
                }
            };
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                    functions.toArray(new OpenNodeFunction[functions.size()]));
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
            select(node.id, true);
        }
    }

    private void onTreeSelection(SelectionContext<Context> context) {
        if ("ready".equals(context.action)) { //NON-NLS
            // only (de)selection events please
            return;
        }

        filter.setDisabled(context.selected.isEmpty() ||
                !context.node.data.isFullyQualified() ||
                context.node.id.equals(ROOT_ID));
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
        metadataProcessor.lookup(template, progress.get(), new DefaultMetadataCallback(address) {
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
                MessageEvent.fire(eventBus, Message.warning(resources.constants().allSingletonsExist()));

            } else if (parent.data.getSingletons().size() == 1) {
                // no need to show a wizard
                String singleton = parent.data.getSingletons().iterator().next();
                ResourceAddress singletonAddress = parent.data.getAddress().getParent().add(parent.text, singleton);
                AddressTemplate template = asGenericTemplate(parent, singletonAddress);
                metadataProcessor.lookup(template, progress.get(), new DefaultMetadataCallback(singletonAddress) {
                    @Override
                    public void onMetadata(Metadata metadata) {
                        String id = IdBuilder.build(parent.id, "singleton", "add");
                        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata).createResource().build();
                        AddResourceDialog<ModelNode> dialog = new AddResourceDialog<>(
                                resources.messages().addResourceTitle(singleton), form,
                                (n, modelNode) -> {
                                    Operation.Builder builder = new Operation.Builder(ADD,
                                            fqAddress(parent, singleton));
                                    if (modelNode != null) {
                                        builder.payload(modelNode);
                                    }
                                    dispatcher.execute(builder.build(),
                                            result -> {
                                                MessageEvent.fire(eventBus,
                                                        Message.success(resources.messages()
                                                                .addResourceSuccess(parent.text, singleton)));
                                                refresh(parent);
                                            });
                                });
                        dialog.show();
                    }
                });

            } else {
                // open wizard to choose the singleton
                NewSingletonWizard wizard = new NewSingletonWizard(metadataProcessor, progress, eventBus, resources,
                        parent, children, context -> {
                    Operation.Builder builder = new Operation.Builder(ADD,
                            fqAddress(parent, context.singleton));
                    if (context.modelNode != null) {
                        builder.payload(context.modelNode);
                    }
                    dispatcher.execute(builder.build(),
                            result -> {
                                MessageEvent.fire(eventBus, Message.success(
                                        resources.messages()
                                                .addResourceSuccess(parent.text, context.singleton)));
                                refresh(parent);
                            });
                });
                wizard.show();
            }

        } else {
            AddressTemplate template = asGenericTemplate(parent, parent.data.getAddress());
            metadataProcessor.lookup(template, progress.get(), new DefaultMetadataCallback(parent.data.getAddress()) {
                @Override
                public void onMetadata(Metadata metadata) {
                    AddResourceDialog<ModelNode> dialog = new AddResourceDialog<>(
                            IdBuilder.build(parent.id, "add"),
                            resources.messages().addResourceTitle(parent.text),
                            metadata, (name, modelNode) -> {
                        Operation operation = new Operation.Builder(ADD, fqAddress(parent, name))
                                .payload(modelNode)
                                .build();
                        dispatcher.execute(operation,
                                result -> {
                                    MessageEvent.fire(eventBus,
                                            Message.success(resources.messages()
                                                    .addResourceSuccess(parent.text, name)));
                                    refresh(parent);
                                });
                    });
                    dialog.show();
                }
            });
        }
    }

    static AddressTemplate asGenericTemplate(Node<Context> node, ResourceAddress address) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Property> iterator = address.asPropertyList().iterator(); iterator.hasNext(); ) {
            Property property = iterator.next();
            String name = property.getName();

            if (PROFILE.equals(name)) {
                builder.append(ANY_PROFILE.variable());
            } else if (SERVER_GROUP.equals(name)) {
                builder.append(ANY_GROUP.variable());
            } else {
                builder.append(name).append("=");
                if (!iterator.hasNext() && node != null && node.data != null && !node.data.hasSingletons()) {
                    builder.append("*");
                } else {
                    builder.append(property.getValue().asString());
                }
            }
            if (iterator.hasNext()) {
                builder.append("/");
            }
        }
        return AddressTemplate.of(builder.toString());
    }

    private ResourceAddress fqAddress(Node<Context> parent, String child) {
        return parent.data.getAddress().getParent().add(parent.text, child);
    }

    void remove(ResourceAddress address) {
        Operation operation = new Operation.Builder(REMOVE, address).build();
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus,
                    Message.success(
                            resources.messages().removeResourceSuccess(address.lastName(), address.lastValue())));
            refresh(tree.api().getSelected());
        });
    }

    void reset(Form<ModelNode> form) {
        Browser.getWindow().alert(NYI);
    }

    void save(ResourceAddress address, Map<String, Object> changedValues) {
        Composite composite = operationFactory.fromChangeSet(address, changedValues);
        dispatcher.execute(composite, (CompositeResult result) -> {
            MessageEvent.fire(eventBus,
                    Message.success(
                            resources.messages().modifyResourceSuccess(address.lastName(), address.lastValue())));
            refresh(tree.api().getSelected());
        });
    }


    // ------------------------------------------------------ public API

    public void setRoot(ResourceAddress root, boolean updateBreadcrumb) {
        this.updateBreadcrumb = updateBreadcrumb;

        String resource = root == ResourceAddress.ROOT ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser.setRoot() must be called with a concrete address.");
        }
        initTree(root, resource);
        tree.api().openNode(ROOT_ID, () -> resourcePanel.tabs.showTab(0));
        select(ROOT_ID, false);
        adjustHeight();
    }

    public void select(final String id, final boolean closeSelected) {
        tree.api().deselectAll(true);
        tree.api().selectNode(id, false, false);
        if (closeSelected) {
            tree.api().closeNode(id);
        }
        tree.asElement().focus();
        Browser.getDocument().getElementById(id).scrollIntoView(false);
    }

    @Override
    public Iterable<Element> asElements() {
        return rows;
    }
}
