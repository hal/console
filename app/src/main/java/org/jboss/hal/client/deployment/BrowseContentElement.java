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
import elemental2.core.Array;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Clipboard;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionChangeHandler.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import static java.lang.Math.max;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_BIG;
import static org.jboss.hal.core.ui.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.core.ui.Skeleton.applicationHeight;
import static org.jboss.hal.core.ui.Skeleton.applicationOffset;
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

    private final HTMLElement treeContainer;
    private final HTMLElement editorControls;
    private final HTMLElement editorStatus;
    private final HTMLElement copyToClipboardLink;
    private final HTMLElement downloadLink;
    private final HTMLElement previewContainer;
    private final HTMLElement previewHeader;
    private final HTMLElement previewImageContainer;
    private final HTMLImageElement previewImage;
    private final HTMLElement root;

    private String content;
    private int surroundingHeight;


    // ------------------------------------------------------ ui setup

    @SuppressWarnings("ConstantConditions")
    BrowseContentElement(final Dispatcher dispatcher, final Resources resources, final Callback refreshCallback) {
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
                .primaryAction(resources.constants().download(),
                        () -> DomGlobal.window.location.assign(downloadUrl((tree.api().getSelected().data))))
                .secondaryAction(resources.constants().viewInEditor(), () -> {
                    viewInEditor(tree.api().getSelected().data);
                })
                .build();

        root = row()
                .add(column(4)
                        .add(div().css(flexRow, marginTopLarge)
                                .add(div().css(btnGroup, marginRightSmall)
                                        .add(button().css(btn, btnDefault)
                                                .on(click, event -> refreshCallback.execute())
                                                .add(i().css(fontAwesome(CSS.refresh))))
                                        .add(button().css(btn, btnDefault)
                                                .on(click, event -> collapse(tree.api().getSelected()))
                                                .add(i().css(fontAwesome("minus")))))
                                .add(treeSearch))
                        .add(treeContainer = div().css(CSS.treeContainer).asElement()))
                .add(column(8)
                        .add(div().css(marginTopLarge, marginBottomLarge)
                                .add(previewContainer = div()
                                        .add(previewHeader = h(1)
                                                .textContent(resources.constants().preview())
                                                .asElement())
                                        .add(previewImageContainer = div()
                                                .style("overflow: scroll")
                                                .add(previewImage = img().css(imgResponsive, imgThumbnail).asElement())
                                                .asElement())
                                        .asElement())
                                .add(editorControls = div().css(CSS.editorControls, marginBottomSmall)
                                        .add(contentSearch)
                                        .add(div().css(CSS.editorStatus)
                                                .add(editorStatus = span()
                                                        .textContent(resources.constants().nothingSelected())
                                                        .asElement()))
                                        .add(div().css(editorButtons)
                                                .add(copyToClipboardLink = a().css(btn, btnDefault, clickable)
                                                        .title(resources.constants().copyToClipboard())
                                                        .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                                        .data(UIConstants.CONTAINER, UIConstants.BODY)
                                                        .data(UIConstants.PLACEMENT, UIConstants.TOP)
                                                        .add(span().css(fontAwesome("clipboard")))
                                                        .asElement())
                                                .add(downloadLink = a().css(btn, btnDefault, clickable)
                                                        .title(resources.constants().download())
                                                        .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                                        .data(UIConstants.CONTAINER, UIConstants.BODY)
                                                        .data(UIConstants.PLACEMENT, UIConstants.TOP)
                                                        .add(span().css(fontAwesome("download")))
                                                        .asElement()))
                                        .asElement())
                                .add(editor)
                                .add(pleaseSelect)
                                .add(deploymentPreview)
                                .add(unsupportedFileType)))
                .asElement();

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
        adjustHeight();
        adjustEditorHeight();
        DomGlobal.window.onresize = event -> {
            adjustEditorHeight();
            return null;
        };
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    void setSurroundingHeight(final int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
        adjustEditorHeight();
    }

    private void adjustHeight() {
        int treeOffset = (int) (applicationOffset() + 2 * MARGIN_BIG + treeSearch.asElement().offsetHeight + MARGIN_SMALL + surroundingHeight);
        int previewHeaderHeight = (int) previewHeader.offsetHeight;
        int previewOffset = applicationOffset() + 2 * MARGIN_BIG + MARGIN_SMALL + previewHeaderHeight + surroundingHeight;

        treeContainer.style.height = vh(treeOffset);
        previewImageContainer.style.height = vh(previewOffset);
    }

    private void adjustEditorHeight() {
        int editorHeight = (int) (applicationHeight() - 2 * MARGIN_BIG - MARGIN_SMALL - editorControls.offsetHeight - surroundingHeight);

        if (Elements.isVisible(editor.asElement())) {
            editor.asElement().style.height = height(px(max(editorHeight, MIN_HEIGHT)));
            editor.getEditor().resize();
        }
    }


    // ------------------------------------------------------ ui visibility / states

    void setContent(final String content, final ModelNode browseContentResult) {
        this.content = content;

        Array<Node<ContentEntry>> nodes = new Array<>();
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
    }

    private void deploymentPreview() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), true);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);

        deploymentPreview.setHeader(content);
        deploymentPreview.setPrimaryAction(resources.constants().download(),
                () -> DomGlobal.window.location.assign(downloadUrl(null)));
    }

    private void directory() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
    }

    private void viewInEditor(ContentEntry contentEntry) {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, true);
        Elements.setVisible(editor.asElement(), true);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustEditorHeight();

        editorStatus.textContent = contentEntry.name + " - " + Format.humanReadableFileSize(contentEntry.fileSize);
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

        previewImage.src = downloadUrl(contentEntry);
    }

    private void unsupportedFileType() {
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), true);
        Elements.setVisible(previewContainer, false);
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
            DomGlobal.setTimeout((o) -> tooltip.hide(), 1000);
        }
    }

    private void selectNode(SelectionContext<ContentEntry> selection) {
        if (selection.selected.getLength() != 0) {
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
            Operation operation = new Operation.Builder(address, READ_CONTENT)
                    .param(PATH, contentEntry.path)
                    .build();
            dispatcher.download(operation, successCallback);
        }
    }


    // ------------------------------------------------------ helper methods

    private String downloadUrl(ContentEntry contentEntry) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content);
        Operation.Builder builder = new Operation.Builder(address, READ_CONTENT);
        if (contentEntry != null) {
            builder.param(PATH, contentEntry.path);
        }
        return dispatcher.downloadUrl(builder.build());
    }
}
