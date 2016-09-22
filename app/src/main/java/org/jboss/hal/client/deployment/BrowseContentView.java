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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.resources.CSS.modelBrowserContent;

/**
 * @author Harald Pehl
 */
public class BrowseContentView extends PatternFlyViewImpl implements BrowseContentPresenter.MyView {

    private static final String TREE_CONTAINER = "treeContainer";
    private static final String CONTENT_CONTAINER = "contentContainer";

    private Element treeContainer;
    private Element contentContainer;

    @Inject
    public BrowseContentView(Resources resources) {
        // @formatter:off
        LayoutBuilder builder = new LayoutBuilder()
            .row()
                .column(4)
                    .div().rememberAs(TREE_CONTAINER).css(CSS.treeContainer).end()
                .end()
                .column(8)
                    .h(1).textContent(resources.constants().details()).end()
                    .div().css(modelBrowserContent).rememberAs(CONTENT_CONTAINER).end()
                .end()
            .end();
        // @formatter:on

        this.treeContainer = builder.referenceFor(TREE_CONTAINER);
        this.contentContainer = builder.referenceFor(CONTENT_CONTAINER);
        Element root = builder.build();
        initElement(root);
    }

    @Override
    public void attach() {
        super.attach();
        Browser.getWindow().setOnresize(event -> adjustHeight());
        adjustHeight();
    }

    private void adjustHeight() {
        int height = Skeleton.applicationHeight();
        treeContainer.getStyle().setHeight(height - 2 * MARGIN_BIG - 2 * MARGIN_SMALL, PX);
    }

    @Override
    public void setContent(final JsArrayOf<Node<ContentEntry>> nodes) {
        Tree<ContentEntry> tree = new Tree<>(Ids.CONTENT_TREE, nodes);
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());
        tree.attach();
    }
}
