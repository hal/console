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

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
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
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class ModelBrowser implements HasElements {

    private static class FilterInfo {

        final String title;
        final ResourceAddress address;

        private FilterInfo(final String title, final ResourceAddress address) {
            this.title = title;
            this.address = address;
        }
    }


    private abstract class DefaultMetadataCallback implements MetadataProvider.MetadataCallback {

        private final ResourceAddress address;

        DefaultMetadataCallback(final ResourceAddress address) {this.address = address;}

        @Override
        public void onError(final Throwable error) {
            //noinspection HardCodedStringLiteral
            logger.error("Error while processing metadata for {}: {}", address, error.getMessage());
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

    private final MetadataProvider metadataProvider;
    private final Capabilities capabilities;
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
            final SecurityFramework securityFramework,
            final Capabilities capabilities,
            final ResourceDescriptions resourceDescriptions,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        this.capabilities = capabilities;

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.metadataProvider = new MetadataProvider(metadataProcessor, securityFramework, resourceDescriptions,
                capabilities, progress);
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

        resourcePanel = new ResourcePanel(this, dispatcher, capabilities, resources);
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


    // ------------------------------------------------------ event handler & co

    private void filter(Node<Context> node) {
        if (node != null && node.parent != null) {
            Node<Context> parent = tree.api().getNode(node.parent);
            FilterInfo filterInfo = new FilterInfo(parent.text + "=" + node.text, node.data.getAddress());
            filterStack.add(filterInfo);
            filter(filterInfo);
        }
    }

    private void filter(FilterInfo filterInfo) {
        // @formatter:off
        Element filter = new Elements.Builder()
            .div().css(tagManagerContainer)
                .span().css(tmTag, tagManagerTag)
                    .span().textContent(filterInfo.title).end()
                    .a().css(clickable, tmTagRemove).on(click, event -> clearFilter()).textContent("x").end() //NON-NLS
                .end()
            .end().build();
        // @formatter:on

        Element oldFilter = buttonGroup.querySelector("." + tagManagerContainer);
        if (oldFilter != null) {
            buttonGroup.replaceChild(filter, oldFilter);
        } else {
            buttonGroup.appendChild(filter);
        }
        tree.api().destroy(false);
        setRoot(filterInfo.address, true, false);
    }

    private void clearFilter() {
        Element filterElement = buttonGroup.querySelector("." + tagManagerContainer);
        if (filterElement != null) {
            buttonGroup.removeChild(filterElement);
            if (!filterStack.isEmpty()) {
                filter(filterStack.pop());
            } else {
                setRoot(ResourceAddress.ROOT, true, true);
            }
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
        metadataProvider.getMetadata(parent, address, new DefaultMetadataCallback(address) {
            @Override
            public void onMetadata(final SecurityContext securityContext, final ResourceDescription description) {
                resourcePanel.update(node, node.data.getAddress(), securityContext, description);
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
                metadataProvider.getMetadata(parent, singletonAddress, new DefaultMetadataCallback(singletonAddress) {
                    @Override
                    public void onMetadata(SecurityContext securityContext, ResourceDescription description) {
                        String id = IdBuilder.build(parent.id, "singleton", "add");
                        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, securityContext, description,
                                capabilities).createResource().build();
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
                NewSingletonWizard wizard = new NewSingletonWizard(eventBus, metadataProvider, resources,
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
            metadataProvider.getMetadata(parent, parent.data.getAddress(),
                    new DefaultMetadataCallback(parent.data.getAddress()) {
                        @Override
                        public void onMetadata(SecurityContext securityContext, ResourceDescription description) {
                            AddResourceDialog<ModelNode> dialog = new AddResourceDialog<>(
                                    IdBuilder.build(parent.id, "add"),
                                    resources.messages().addResourceTitle(parent.text),
                                    securityContext, description, capabilities,
                                    (name, modelNode) -> {
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

    public void setRoot(ResourceAddress root, boolean updateBreadcrumb, boolean clearFilter) {
        this.updateBreadcrumb = updateBreadcrumb;
        if (clearFilter) {
            filterStack.clear();
            NodeList nodes = buttonGroup.querySelectorAll("." + tagManagerContainer);
            for (int i = 0; i < nodes.getLength(); i++) {
                buttonGroup.removeChild(nodes.item(i));
            }
        }

        String resource = root == ResourceAddress.ROOT ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser.setRoot() must be called with a concrete address.");
        }
        Context context = new Context(root, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(ROOT_ID, resource, context)
                .folder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, new ReadChildren(dispatcher));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> onTreeSelection(selectionContext));
        tree.api().openNode(ROOT_ID, () -> resourcePanel.tabs.showTab(0));
        childrenPanel.attach();

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
    }

    @Override
    public Iterable<Element> asElements() {
        return rows;
    }
}
