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

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class BrowseContentView extends PatternFlyViewImpl implements BrowseContentPresenter.MyView {

    private static final String TREE_CONTAINER = "treeContainer";
    private static final String CONTENT_CONTAINER = "contentContainer";

    private Search search;
    private Element treeContainer;
    private Element contentContainer;
    private BrowseContentPresenter presenter;
    private Tree<ContentEntry> tree;
    private AceEditor editor;

    public BrowseContentView() {
        search = new Search.Builder(Ids.CONTENT_SEARCH,
                query -> {
                    if (tree.api() != null) {
                        tree.api().search(query);
                    }
                })
                .onClear(() -> {
                    if (tree.api() != null) {
                        tree.api().clearSearch();
                    }
                })
                .build();

        Options editorOptions = new Options();
        editorOptions.readOnly = true;
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.CONTENT_EDITOR, editorOptions);
        registerAttachable(editor);

        // @formatter:off
        LayoutBuilder builder = new LayoutBuilder()
            .row()
                .column(4)
                    .div().css(flexRow, marginTopLarge)
                        .div().css(btnGroup, marginRightSmall)
                            .button().css(btn, btnDefault).on(click, event -> presenter.loadContent())
                                .add("i").css(fontAwesome(CSS.refresh))
                            .end()
                            .button().css(btn, btnDefault).on(click, event -> collapse(tree.api().getSelected()))
                                .add("i").css(fontAwesome("minus"))
                            .end()
                        .end()
                        .add(search)
                    .end()
                    .div().rememberAs(TREE_CONTAINER).css(CSS.treeContainer).end()
                .end()
                .column(8)
                    .div().css(marginTopLarge).rememberAs(CONTENT_CONTAINER)
                        .add(editor.asElement())
                    .end()
                .end()
            .end();
        // @formatter:on

        this.treeContainer = builder.referenceFor(TREE_CONTAINER);
        this.contentContainer = builder.referenceFor(CONTENT_CONTAINER);
        Element root = builder.build();
        initElement(root);
    }

    @Override
    public void setPresenter(final BrowseContentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
        Browser.getWindow().setOnresize(event -> adjustHeight());
        adjustHeight();
    }

    private void adjustHeight() {
        int height = Skeleton.applicationHeight();
        int searchHeight = search.asElement().getOffsetHeight();
        treeContainer.getStyle().setHeight(height - 2 * MARGIN_BIG - searchHeight - 2 * MARGIN_SMALL, PX);
        editor.asElement().getStyle().setHeight(200, PX);
    }

    private void collapse(final Node<ContentEntry> node) {
        if (node != null) {
            tree.select(node.id, true);
        }
    }
    @Override
    public void setContent(final JsArrayOf<Node<ContentEntry>> nodes) {
        tree = new Tree<>(Ids.CONTENT_TREE, nodes);
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());
        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> {
            if (!"ready".equals(selectionContext.action)) { //NON-NLS
                boolean hasSelection = !selectionContext.selected.isEmpty();
                if (hasSelection) {
                    ContentEntry contentEntry = selectionContext.node.data;
                    presenter.loadContent(contentEntry, result -> {
                        editor.setModeFromPath(contentEntry.name);
                        editor.getEditor().getSession().setValue(result);
                    });
                }
            }
        });
    }
}
