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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import java.util.Collections;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class ModelBrowser implements HasElements, Attachable, SecurityContextAware {

    private static final int MARGIN_BIG = 20; // keep this in sync with the
    private static final int MARGIN_SMALL = 10; // margins in modelbrowser.less
    private static final String BREADCRUMB_ELEMENT = "breadcrumbElement";

    private final Iterable<Element> rows;
    private final Element header;
    private final Element breadcrumb;
    private final Element buttonGroup;
    private final Element treeHolder;
    private final Tree<Context> tree;
    private final Element content;

    public ModelBrowser(final Dispatcher dispatcher, final SecurityContext securityContext,
            final ResourceAddress root) {
        String resource = root == ResourceAddress.ROOT ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser must be created with a concrete address.");
        }

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .header().css(modelBrowserHeader)
                .ul().css(CSS.breadcrumb).rememberAs(BREADCRUMB_ELEMENT)
                    .li().span().css(key).innerText("profiles").end().span().css(value).innerText("full").end().end()
                    .li().span().css(key).innerText("subsystem").end().span().css(value).innerText("datasources").end().end()
                    .li().span().css(key).innerText("data-source").end().span().css(value).innerText("some-data-source").end().end()
                    .li().span().css(key).innerText("fqn").end().span().css(value).innerText("org.jboss.hal.core.modelbrowser").end().end()
                    .li().span().css(key).innerText("another-key").end().span().css(value).innerText("another-value").end().end()
                    .li().span().css(key).innerText("lore-ipsum").end().span().css(value).innerText("dolor-sit-amet").end().end()
                .end()
                .p().css(lead).innerText("Ein benannter Dateisystempfad, ohne dass ein Spezifizieren des eigentlichen Pfads nötig ist. Falls kein eigentlicher Pfad spezifiziert ist, fungiert dies als Platzhalter im Modell (z.B. auf Domain-Ebene), bis eine voll spezifizierte Pfaddefinition auf niedrigerer Ebene (z.B. auf Host-Ebene, wo verfügbare Adressen bekannt sind) angewendet wird.").end()
            .end();
        header = builder.build();
        breadcrumb = builder.referenceFor(BREADCRUMB_ELEMENT);
        // @formatter:on

        buttonGroup = new Elements.Builder()
                .div().css(btnGroup, modelBrowserButtons)
                .button().css(btn, btnDefault).add("i").css(fontAwesome("filter")).end()
                .button().css(btn, btnDefault).add("i").css(fontAwesome("refresh")).end()
                .end()
                .build();

        Context context = new Context(root, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(IdBuilder.uniqueId(), resource, context)
                .folder()
                .build();
        tree = new Tree<>(Ids.MODEL_BROWSER, securityContext, rootNode, new ReadChildren(dispatcher));
        treeHolder = new Elements.Builder().div().css(modelBrowserTree).add(tree.asElement()).end().build();

        content = new Elements.Builder().div().css(modelBrowserContent).end().build();

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
                    .add(treeHolder)
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
            treeHolder.getStyle().setHeight(height - 2 * MARGIN_BIG - header - buttonGroup - 2 * MARGIN_SMALL, PX);
            content.getStyle().setHeight(height - 2 * MARGIN_BIG - header - 2 * MARGIN_SMALL, PX);
        }
    }

    @Override
    public Iterable<Element> asElements() {
        return rows;
    }

    @Override
    public void attach() {
        tree.attach();
        adjustHeight();
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {}
}
