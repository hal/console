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

import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Iterator;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.util.Collections.singleton;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class ModelBrowser implements HasElements, Attachable, SecurityContextAware {

    private static final int MARGIN_BIG = 20; // keep this in sync with the
    private static final int MARGIN_SMALL = 10; // margins in modelbrowser.less
    private static final String BREADCRUMB_ELEMENT = "breadcrumbElement";

    private static final Logger logger = LoggerFactory.getLogger(ModelBrowser.class);

    private final MetadataProcessor metadataProcessor;
    private final ResourceDescriptions resourceDescriptions;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private final Resources resources;

    private final Iterable<Element> rows;
    private final Element header;
    private final Element breadcrumb;
    private final Element buttonGroup;
    private final Element treeContainer;
    private final Element content;
    private final ResourceView resourceView;
    private Tree<Context> tree;
    private ResourceAddress root;


    @Inject
    public ModelBrowser(final MetadataProcessor metadataProcessor,
            final ResourceDescriptions resourceDescriptions,
            final Dispatcher dispatcher,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        this.metadataProcessor = metadataProcessor;
        this.resourceDescriptions = resourceDescriptions;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.resources = resources;

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .header().css(modelBrowserHeader)
                .ul().css(CSS.breadcrumb).rememberAs(BREADCRUMB_ELEMENT).end()
            .end();
        // @formatter:on

        header = builder.build();
        breadcrumb = builder.referenceFor(BREADCRUMB_ELEMENT);

        buttonGroup = new Elements.Builder()
                .div().css(btnGroup, modelBrowserButtons)
                .button().css(btn, btnDefault).add("i").css(fontAwesome("filter")).end()
                .button().css(btn, btnDefault).add("i").css(fontAwesome("refresh")).end()
                .end()
                .build();
        treeContainer = new Elements.Builder().div().css(modelBrowserTree).end().build();
        content = new Elements.Builder().div().css(modelBrowserContent).end().build();
        resourceView = new ResourceView(dispatcher, resources);

        // @formatter:off
        rows =  new LayoutBuilder()
            .row()
                .column()
                    .add(header)
                .end()
            .end()
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
        int header = this.header.getOffsetHeight();
        int buttonGroup = this.buttonGroup.getOffsetHeight();
        if (navigation > 0 && footer > 0) {
            int height = window - navigation - footer;
            // keep this in sync with the margins in modelbrowser.less
            treeContainer.getStyle().setHeight(height - 2 * MARGIN_BIG - header - buttonGroup - 2 * MARGIN_SMALL, PX);
            content.getStyle().setHeight(height - 2 * MARGIN_BIG - header - 2 * MARGIN_SMALL, PX);
        }
    }

    @Override
    public Iterable<Element> asElements() {
        return rows;
    }

    @Override
    public void attach() {
        if (tree != null) {
            tree.attach();
            tree.onSelectionChange((event, context) -> update(context.node));
        }
        updateBreadcrumb(root);
        adjustHeight();
    }

    private void update(Node<Context> node) {
        Elements.removeChildrenFrom(content);

        if (node == null) {
            updateBreadcrumb(null);
        } else {
            ResourceAddress address = node.data.getAddress();
            updateBreadcrumb(address);

            if (node.data.isFullyQualified()) {
                updateDescription(node, asGenericTemplate(address));
            } else {
                // TODO Show children
            }
        }
    }

    private void updateBreadcrumb(ResourceAddress address) {
        Elements.removeChildrenFrom(breadcrumb);
        if (address == null) {
            // deselection
            breadcrumb.appendChild(
                    new Elements.Builder().li().innerText(resources.constants().nothingSelected()).build());

        } else {
            if (address == ResourceAddress.ROOT) {
                Element li = new Elements.Builder().li().innerText(Names.MANAGEMENT_MODEL).build();
                breadcrumb.appendChild(li);

            } else {
                for (Property property : address.asPropertyList()) {
                    Element li = new Elements.Builder().li()
                            .span().css(key).innerText(property.getName()).end()
                            .span().css(value).innerText(property.getValue().asString()).end()
                            .end().build();
                    breadcrumb.appendChild(li);
                }
            }
        }
    }

    private void updateDescription(Node<Context> node, AddressTemplate template) {
        metadataProcessor.process(Ids.MODEL_BROWSER, singleton(template), progress,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        logger.error("Unable to process metadata for {} on node {}({}): {}", //NON-NLS
                                template, node.id, node.text, throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(final Void aVoid) {
                        ResourceDescription description = resourceDescriptions.lookup(template);
                        if (description != null) {
                            resourceView.update(node.data.getAddress(), description);
                            for (Element element : resourceView.asElements()) {
                                content.appendChild(element);
                            }
                        }
                    }
                });
    }

    private AddressTemplate asGenericTemplate(ResourceAddress address) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Property> iterator = address.asPropertyList().iterator(); iterator.hasNext(); ) {
            Property property = iterator.next();
            String name = property.getName();

            if (PROFILE.equals(name)) {
                builder.append("{any.profile}"); //NON-NLS

            } else {
                builder.append(name).append("=");
                builder.append(property.getValue().asString());
            }
            if (iterator.hasNext()) {
                builder.append("/");
            }
        }
        return AddressTemplate.of(builder.toString());
    }


    // ------------------------------------------------------ public API

    public void setRoot(ResourceAddress root) {
        this.root = root;
        String resource = root == ResourceAddress.ROOT ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser must be created with a concrete address.");
        }
        Context context = new Context(root, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(IdBuilder.uniqueId(), resource, context)
                .folder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, rootNode, new ReadChildren(dispatcher));
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());
        attach();
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {}
}
