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

import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ImageElement;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionChangeHandler.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.ui.Skeleton;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
import static java.lang.Math.max;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CONTENT;
import static org.jboss.hal.resources.CSS.*;

/**
 * UI element to browse the content of an item from the content repository.
 *
 * @author Harald Pehl
 */
class BrowseContentElement implements IsElement, Attachable {

    @FunctionalInterface
    interface RefreshCallback {

        void refresh();
    }


    @SuppressWarnings("HardCodedStringLiteral")
    private static final Set<String> EDITOR_FILE_TYPES = Sets.newHashSet(
            "bash",
            "css",
            "htm",
            "html",
            "ini",
            "java",
            "js",
            "jsm",
            "jsx",
            "json",
            "jsf",
            "jsp",
            "jsx",
            "less",
            "md",
            "markdown",
            "MF",
            "php",
            "php",
            "php3",
            "php4",
            "php5",
            "phps",
            "phpt",
            "phtml",
            "properties",
            "rb",
            "ru",
            "sh",
            "sql",
            "txt",
            "ts",
            "typescript",
            "shtml",
            "xhtml",
            "xml");

    @SuppressWarnings("HardCodedStringLiteral")
    private static final Set<String> IMAGE_FILE_TYPES = Sets.newHashSet(
            "bmp",
            "gif",
            "ico",
            "img",
            "jpg",
            "jpeg",
            "png",
            "svg",
            "tiff",
            "webp");

    private static final String COPY_TO_CLIPBOARD = "copyToClipboard";
    private static final String DOWNLOAD = "download";
    private static final String EDITOR_CONTROLS = "contentHeader";
    private static final String EDITOR_STATUS = "editorStatus";
    private static final String PREVIEW_CONTAINER = "previewContainer";
    private static final String PREVIEW_HEADER = "previewHeader";
    private static final String PREVIEW_IMAGE_CONTAINER = "previewImageContainer";
    private static final String PREVIEW_IMAGE = "previewImage";
    private static final String TREE_CONTAINER = "treeContainer";
    private static final int MIN_HEIGHT = 70;

    private final Dispatcher dispatcher;
    private final Resources resources;

    private final ContentParser contentParser;
    private final Search treeSearch;
    private Tree<ContentEntry> tree;
    private final EmptyState pleaseSelect;
    private final EmptyState deploymentPreview;
    private final EmptyState unsupportedFileType;
    private AceEditor editor;

    private final Element treeContainer;
    private final Element editorControls;
    private final Element editorStatus;
    private final Element copyToClipboardLink;
    private final Element downloadLink;
    private final Element previewContainer;
    private final Element previewHeader;
    private final Element previewImageContainer;
    private final ImageElement previewImage;
    private final Element root;

    private String content;
    private int surroundingHeight;


    // ------------------------------------------------------ ui setup

    BrowseContentElement(final Dispatcher dispatcher, final Resources resources,
            final RefreshCallback refreshCallback) {
        this.dispatcher = dispatcher;
        this.resources = resources;
        this.contentParser = new ContentParser();
        this.surroundingHeight = 0;

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

        pleaseSelect = new EmptyState.Builder(resources.constants().nothingSelected())
                .icon(Icons.INFO)
                .description(resources.constants().noContentSelected())
                .build();

        deploymentPreview = new EmptyState.Builder(Names.DEPLOYMENT)
                .icon(fontAwesome("archive"))
                .description(resources.constants().deploymentPreview())
                .build();

        unsupportedFileType = new EmptyState.Builder(resources.constants().unsupportedFileType())
                .icon(Icons.UNKNOWN)
                .description(resources.constants().unsupportedFileTypeDescription())
                .primaryAction(resources.constants().download(), () -> {
                    //noinspection ConstantConditions
                    Browser.getWindow().getLocation().setHref(downloadUrl((tree.api().getSelected().data)));
                })
                .secondaryAction(resources.constants().viewInEditor(), () -> {
                    //noinspection ConstantConditions
                    viewInEditor(tree.api().getSelected().data);
                })
                .build();

        // @formatter:off
        LayoutBuilder builder = new LayoutBuilder()
            .row()
                .column(4)
                    .div().css(flexRow, marginTopLarge)
                        .div().css(btnGroup, marginRightSmall)
                            .button().css(btn, btnDefault).on(click, event -> refreshCallback.refresh())
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
                    .div().css(marginTopLarge, marginBottomLarge)
                        .div().rememberAs(PREVIEW_CONTAINER)
                            .h(1).rememberAs(PREVIEW_HEADER)
                                .textContent(resources.constants().preview())
                            .end()
                            .div().rememberAs(PREVIEW_IMAGE_CONTAINER).style("overflow: scroll")
                                .add("img").css(imgResponsive, imgThumbnail).rememberAs(PREVIEW_IMAGE)
                            .end()
                        .end()
                        .div().css(CSS.editorControls, marginBottomSmall).rememberAs(EDITOR_CONTROLS)
                            .add(contentSearch)
                            .div().css(CSS.editorStatus)
                                .span().rememberAs(EDITOR_STATUS)
                                    .textContent(resources.constants().nothingSelected())
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
                        .add(editor.asElement())
                        .add(pleaseSelect)
                        .add(deploymentPreview)
                        .add(unsupportedFileType)
                    .end()
                .end()
            .end();
        // @formatter:on

        root = builder.build();
        treeContainer = builder.referenceFor(TREE_CONTAINER);
        editorControls = builder.referenceFor(EDITOR_CONTROLS);
        editorStatus = builder.referenceFor(EDITOR_STATUS);
        copyToClipboardLink = builder.referenceFor(COPY_TO_CLIPBOARD);
        downloadLink = builder.referenceFor(DOWNLOAD);
        previewContainer = builder.referenceFor(PREVIEW_CONTAINER);
        previewHeader = builder.referenceFor(PREVIEW_HEADER);
        previewImageContainer = builder.referenceFor(PREVIEW_IMAGE_CONTAINER);
        previewImage = builder.referenceFor(PREVIEW_IMAGE);

        Clipboard clipboard = new Clipboard(copyToClipboardLink);
        clipboard.onCopy(event -> copyToClipboard(event.client));

        Elements.setVisible(pleaseSelect.asElement(), true);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
    }

    @Override
    public void attach() {
        editor.attach();
        editor.getEditor().$blockScrolling = 1;
        Browser.getWindow().setOnresize(event -> adjustHeight());
    }

    @Override
    public Element asElement() {
        return root;
    }

    void setSurroundingHeight(final int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
    }

    private void adjustHeight() {
        int height = Skeleton.applicationHeight();
        int treeHeight = height - 2 * MARGIN_BIG - treeSearch.asElement().getOffsetHeight() - MARGIN_SMALL -
                surroundingHeight;
        int editorHeight = height - 2 * MARGIN_BIG - MARGIN_SMALL - editorControls.getOffsetHeight() -
                surroundingHeight;
        int previewHeaderHeight = previewHeader.getOffsetHeight();
        int previewHeight = height - 2 * MARGIN_BIG - MARGIN_SMALL - previewHeaderHeight - surroundingHeight;

        treeContainer.getStyle().setHeight(treeHeight, PX);
        if (Elements.isVisible(editor.asElement())) {
            editor.asElement().getStyle().setHeight(max(editorHeight, MIN_HEIGHT), PX);
            editor.getEditor().resize();
        }
        if (Elements.isVisible(previewImageContainer)) {
            previewImageContainer.getStyle().setHeight(previewHeight, PX);
        }
    }


    // ------------------------------------------------------ ui visibility / states

    void setContent(final String content, final ModelNode browseContentResult) {
        this.content = content;

        JsArrayOf<Node<ContentEntry>> nodes = JsArrayOf.create();
        Node<ContentEntry> root = new Node.Builder<>(Ids.CONTENT_TREE_ROOT, content, new ContentEntry())
                .root()
                .folder()
                .open()
                .build();
        contentParser.parse(nodes, root, browseContentResult.asList());

        tree = new Tree<>(Ids.CONTENT_TREE, nodes);
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.asElement());
        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> {
            if (!"ready".equals(selectionContext.action)) { //NON-NLS
                selectNode(selectionContext);
            }
        });
        noSelection();
    }

    private void noSelection() {
        Elements.setVisible(pleaseSelect.asElement(), true);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustHeight();
    }

    private void deploymentPreview() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), true);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustHeight();

        deploymentPreview.setHeader(content);
        deploymentPreview.setPrimaryAction(resources.constants().download(),
                () -> Browser.getWindow().getLocation().setHref(downloadUrl(null)));
    }

    private void directory() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustHeight();
    }

    private void viewInEditor(ContentEntry contentEntry) {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, true);
        Elements.setVisible(editor.asElement(), true);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustHeight();

        editorStatus.setTextContent(contentEntry.name + " - " + Format.humanReadableFileSize(contentEntry.fileSize));
        downloadLink.setAttribute("href", downloadUrl(contentEntry)); //NON-NLS
        loadContent(contentEntry, result -> {
            editor.setModeFromPath(contentEntry.name);
            editor.getEditor().getSession().setValue(result);
        });
    }

    private void viewInPreview(ContentEntry contentEntry) {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, true);
        adjustHeight();

        previewImage.setSrc(downloadUrl(contentEntry));
    }

    private void unsupportedFileType() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), true);
        Elements.setVisible(previewContainer, false);
        adjustHeight();
    }


    // ------------------------------------------------------ event handler

    private void collapse(final Node<ContentEntry> node) {
        if (node != null) {
            tree.select(node.id, true);
        }
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

    private void selectNode(SelectionContext<ContentEntry> selection) {
        if (!selection.selected.isEmpty()) {
            if (selection.node.id.equals(Ids.CONTENT_TREE_ROOT)) {
                deploymentPreview();

            } else {
                ContentEntry contentEntry = selection.node.data;
                if (contentEntry.directory) {
                    directory();

                } else {
                    int index = contentEntry.name.lastIndexOf('.');
                    String extension = index != -1 && index < contentEntry.name.length() - 1
                            ? contentEntry.name.substring(index + 1) : "";

                    if (EDITOR_FILE_TYPES.contains(extension)) {
                        viewInEditor(contentEntry);

                    } else if (IMAGE_FILE_TYPES.contains(extension)) {
                        viewInPreview(contentEntry);

                    } else {
                        unsupportedFileType();
                    }
                }
            }

        } else {
            noSelection();
        }
    }

    private void loadContent(ContentEntry contentEntry, Dispatcher.SuccessCallback<String> successCallback) {
        if (!contentEntry.directory) {
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content);
            Operation operation = new Operation.Builder(READ_CONTENT, address)
                    .param(PATH, contentEntry.path)
                    .build();
            dispatcher.download(operation, successCallback);
        }
    }


    // ------------------------------------------------------ helper methods

    private String downloadUrl(ContentEntry contentEntry) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content);
        Operation.Builder builder = new Operation.Builder(READ_CONTENT, address);
        if (contentEntry != null) {
            builder.param(PATH, contentEntry.path);
        }
        return dispatcher.downloadUrl(builder.build());
    }
}
