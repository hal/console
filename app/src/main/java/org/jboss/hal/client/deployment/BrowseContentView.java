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

import com.google.common.base.Strings;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Integer.max;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.deployment.BrowseContentPresenter.SUPPORTED_FILE_TYPES;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CONTENT;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class BrowseContentView extends PatternFlyViewImpl implements BrowseContentPresenter.MyView {

    private static final String COPY_TO_CLIPBOARD = "copyToClipboard";
    private static final String DOWNLOAD = "download";
    private static final String EDITOR_CONTROLS = "contentHeader";
    private static final String EDITOR_STATUS = "editorStatus";
    private static final String UNSUPPORTED = "unsupported";
    private static final String TREE_CONTAINER = "treeContainer";
    private static final int MIN_HEIGHT = 70;

    private final Dispatcher dispatcher;
    private final Resources resources;

    private Search treeSearch;
    private Tree<ContentEntry> tree;
    private AceEditor editor;

    private Element treeContainer;
    private Element editorControls;
    private Element editorStatus;
    private Element unsupported;
    private Element copyToClipboardLink;
    private Element downloadLink;

    private BrowseContentPresenter presenter;

    @Inject
    public BrowseContentView(final Dispatcher dispatcher, final Resources resources) {
        this.dispatcher = dispatcher;
        this.resources = resources;

        treeSearch = new Search.Builder(Ids.CONTENT_TREE_SEARCH,
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

        Search contentSearch = new Search.Builder(Ids.CONTENT_SEARCH,
                query -> editor.getEditor().find(query))
                .onPrevious(query -> editor.getEditor().findPrevious())
                .onNext(query -> editor.getEditor().findNext())
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
                        .add(treeSearch)
                    .end()
                    .div().rememberAs(TREE_CONTAINER).css(CSS.treeContainer).end()
                .end()
                .column(8)
                    .div().css(CSS.editorControls, marginTopLarge).rememberAs(EDITOR_CONTROLS)
                        .add(contentSearch)
                        .div().css(CSS.editorStatus)
                            .span().rememberAs(EDITOR_STATUS)
                                .textContent(resources.constants().nothingSelected())
                            .end()
                            .a().css(marginLeft5, clickable)
                                .on(click, event -> readUnsupported()).rememberAs(UNSUPPORTED)
                                .title(resources.constants().unsupportedFileTypeDesc())
                                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                .data(UIConstants.CONTAINER, UIConstants.BODY)
                                .data(UIConstants.PLACEMENT, UIConstants.TOP)
                                    .textContent(resources.constants().unsupportedFileType())
                            .end()
                        .end()
                        .div().css(editorButtons)
                            .a().css(btn, btnDefault, clickable)
                                .title(resources.constants().copyToClipboard())
                                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                .data(UIConstants.CONTAINER, UIConstants.BODY)
                                .data(UIConstants.PLACEMENT, UIConstants.TOP)
                                .rememberAs(COPY_TO_CLIPBOARD)
                                    .span().css(fontAwesome("clipboard")).end()
                            .end()
                            .a().css(btn, btnDefault, clickable)
                                .title(resources.constants().download())
                                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                .data(UIConstants.CONTAINER, UIConstants.BODY)
                                .data(UIConstants.PLACEMENT, UIConstants.TOP)
                                .rememberAs(DOWNLOAD)
                                    .span().css(fontAwesome("download")).end()
                            .end()
                        .end()
                    .end()
                    .div().css(marginTopLarge, marginBottomLarge)
                        .add(editor.asElement())
                    .end()
                .end()
            .end();
        // @formatter:on

        Element root = builder.build();
        treeContainer = builder.referenceFor(TREE_CONTAINER);
        editorControls = builder.referenceFor(EDITOR_CONTROLS);
        editorStatus = builder.referenceFor(EDITOR_STATUS);
        unsupported = builder.referenceFor(UNSUPPORTED);
        copyToClipboardLink = builder.referenceFor(COPY_TO_CLIPBOARD);
        downloadLink = builder.referenceFor(DOWNLOAD);

        Elements.setVisible(unsupported, false);
        Clipboard clipboard = new Clipboard(copyToClipboardLink);
        clipboard.onCopy(event -> copyToClipboard(event.client));

        initElement(root);
    }

    private void copyToClipboard(Clipboard clipboard) {
        String value = editor.getEditor().getSession().getValue();
        if (!Strings.isNullOrEmpty(value)) {
            clipboard.setText(value);
            Tooltip tooltip = Tooltip.element(copyToClipboardLink);
            tooltip.hide()
                    .setTitle(resources.constants().copied())
                    .show()
                    .onHide(() -> tooltip.setTitle(resources.constants().copyToClipboard()));
            Browser.getWindow().setTimeout(tooltip::hide, 1000);
        }
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
        int searchHeight = treeSearch.asElement().getOffsetHeight();
        int treeHeight = height - 2 * MARGIN_BIG - searchHeight - 2 * MARGIN_SMALL;
        int editorHeight = height - 3 * MARGIN_BIG - editorControls.getOffsetHeight();
        editorHeight = max(editorHeight, MIN_HEIGHT);

        treeContainer.getStyle().setHeight(treeHeight, PX);
        editor.asElement().getStyle().setHeight(editorHeight, PX);
        editor.getEditor().resize();
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
                    if (selectionContext.node.id == Ids.CONTENT_TREE_ROOT) {
                        // root node
                        editorStatus.setTextContent(resources.constants().nothingSelected());
                        editor.getEditor().getSession().setValue("");
                    } else {
                        ContentEntry contentEntry = selectionContext.node.data;
                        onSelect(contentEntry);
                    }
                } else {
                    editorStatus.setTextContent(resources.constants().nothingSelected());
                    editor.getEditor().getSession().setValue("");
                }
            }
        });
    }

    private void readUnsupported() {
        Node<ContentEntry> selected = tree.api().getSelected();
        if (selected != null) {
            ContentEntry contentEntry = selected.data;
            presenter.loadContent(contentEntry, result -> {
                editor.setModeFromPath(contentEntry.name);
                editor.getEditor().getSession().setValue(result);
            });
        }
    }

    private void onSelect(ContentEntry contentEntry) {
        updateStatus(contentEntry);
        Elements.setVisible(unsupported, !(isSupported(contentEntry.name) || contentEntry.directory));

        if (contentEntry.directory) {
            editor.getEditor().getSession().setValue("");

        } else {
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, presenter.getContent());
            Operation operation = new Operation.Builder(READ_CONTENT, address).param(PATH, contentEntry.path).build();
            downloadLink.setAttribute("href", dispatcher.downloadUrl(operation)); //NON-NLS

            if (isSupported(contentEntry.name)) {
                presenter.loadContent(contentEntry, result -> {
                    editor.setModeFromPath(contentEntry.name);
                    editor.getEditor().getSession().setValue(result);
                });
            } else {
                editor.getEditor().getSession().setValue("");
            }
        }
    }

    private void updateStatus(ContentEntry contentEntry) {
        StringBuilder builder = new StringBuilder();
        builder.append(contentEntry.name).append(" - ");
        if (contentEntry.directory) {
            builder.append(resources.constants().directory());
        } else {
            builder.append(Format.humanReadableFileSize(contentEntry.fileSize));
        }
        editorStatus.setTextContent(builder.toString());
    }

    private boolean isSupported(String name) {
        int index = name.lastIndexOf('.');
        return index != -1 && SUPPORTED_FILE_TYPES.contains(name.substring(index + 1));
    }
}
