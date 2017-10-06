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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.core.Array;
import elemental2.dom.File;
import elemental2.dom.File.ConstructorContentsArrayUnionType;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionChangeHandler.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.Strings;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import rx.Completable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static elemental2.dom.DomGlobal.window;
import static java.lang.Math.max;
import static java.util.Collections.emptyList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.i;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.ballroom.Skeleton.applicationHeight;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/** UI element to browse the content of an item from the content repository. */
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
    private final EventBus eventBus;
    private final Resources resources;
    private final Callback refreshCallback;

    private final ContentParser contentParser;
    private final Search treeSearch;
    private Tree<ContentEntry> tree;
    private final EmptyState pleaseSelect;
    private final EmptyState deploymentPreview;
    private final EmptyState unsupportedFileType;
    private AceEditor editor;

    private final HTMLElement addRemoveControls;
    private final HTMLButtonElement removeButton;
    private final HTMLElement treeContainer;
    private final HTMLElement editorControls;
    private final HTMLElement editorStatus;
    private final HTMLElement downloadButton;
    private final HTMLButtonElement saveButton;
    private final HTMLElement previewContainer;
    private final HTMLElement previewHeader;
    private final HTMLElement previewImageContainer;
    private final HTMLImageElement previewImage;
    private final HTMLElement root;

    private Content content;
    private int surroundingHeight;


    // ------------------------------------------------------ ui setup

    @SuppressWarnings("ConstantConditions")
    BrowseContentElement(Dispatcher dispatcher, EventBus eventBus, Resources resources, Callback refreshCallback) {
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.refreshCallback = refreshCallback;
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
        treeSearch.asElement().classList.add(marginLeftSmall);

        Search contentSearch = new Search.Builder(Ids.CONTENT_SEARCH,
                query -> editor.getEditor().find(query))
                .onPrevious(query -> editor.getEditor().findPrevious())
                .onNext(query -> editor.getEditor().findNext())
                .build();

        Options editorOptions = new Options();
        editorOptions.readOnly = false;
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
                        () -> window.location.assign(downloadUrl((tree.api().getSelected().data))))
                .secondaryAction(resources.constants().viewInEditor(),
                        () -> viewInEditor(tree.api().getSelected().data))
                .build();

        root = row()
                .add(column(4)
                        .add(div().css(flexRow, marginTopLarge)
                                .add(div().css(btnToolbar)
                                        .add(div().css(btnGroup)
                                                .add(button().css(btn, btnDefault)
                                                        .on(click, event -> refreshCallback.execute())
                                                        .title(resources.constants().refresh())
                                                        .add(i().css(fontAwesome(CSS.refresh))))
                                                .add(button().css(btn, btnDefault)
                                                        .on(click, event -> collapse(tree.api().getSelected()))
                                                        .title(resources.constants().collapse())
                                                        .add(i().css(fontAwesome("minus")))))
                                        .add(addRemoveControls = div().css(btnGroup)
                                                .add(button().css(btn, btnDefault)
                                                        .on(click, event -> newContent())
                                                        .title(resources.constants().newContent())
                                                        .add(i().css(fontAwesome("file-o"))))
                                                .add(button().css(btn, btnDefault)
                                                        .on(click, event -> addContent())
                                                        .title(resources.constants().uploadContent())
                                                        .add(i().css(fontAwesome("upload"))))
                                                .add(removeButton = button().css(btn, btnDefault)
                                                        .on(click, event -> removeContent())
                                                        .title(resources.constants().removeContent())
                                                        .add(i().css(pfIcon("remove")))
                                                        .asElement())
                                                .asElement()))
                                .add(treeSearch))
                        .add(treeContainer = div().css(CSS.treeContainer).asElement()))
                .add(column(8)
                        .add(div().css(marginTopLarge, marginBottomLarge)
                                .add(previewContainer = div()
                                        .add(previewHeader = h(1)
                                                .textContent(resources.constants().preview())
                                                .asElement())
                                        .add(previewImageContainer = div()
                                                .style("overflow: scroll") //NON-NLS
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
                                                .add(saveButton = button().css(btn, btnDefault, clickable)
                                                        .on(click, event -> saveContent())
                                                        .title(resources.constants().save())
                                                        .add(span().css(fontAwesome("floppy-o")))
                                                        .asElement())
                                                .add(downloadButton = a().css(btn, btnDefault, clickable)
                                                        .title(resources.constants().download())
                                                        .add(span().css(fontAwesome("download")))
                                                        .asElement()))
                                        .asElement())
                                .add(editor)
                                .add(pleaseSelect)
                                .add(deploymentPreview)
                                .add(unsupportedFileType)))
                .asElement();

        saveButton.disabled = true;
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
        window.onresize = event -> {
            adjustEditorHeight();
            return null;
        };
    }

    @Override
    public void detach() {
        window.onresize = null;
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    void setSurroundingHeight(int surroundingHeight) {
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

    void setContent(Content content, ModelNode browseContentResult) {
        this.content = content;
        Elements.setVisible(addRemoveControls, content.isExploded());

        Array<Node<ContentEntry>> nodes = new Array<>();
        Node<ContentEntry> root = new Node.Builder<>(Ids.CONTENT_TREE_ROOT, content.getName(), new ContentEntry())
                .root()
                .folder()
                .open()
                .build();
        List<ModelNode> modelNodes = browseContentResult.isDefined() ? browseContentResult.asList() : emptyList();
        contentParser.parse(nodes, root, modelNodes);

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
        removeButton.disabled = true;
        Elements.setVisible(pleaseSelect.asElement(), true);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
    }

    private void deploymentPreview() {
        removeButton.disabled = true;
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), !content.isExploded());
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);

        deploymentPreview.setHeader(content.getName());
        deploymentPreview.setPrimaryAction(resources.constants().download(),
                () -> window.location.assign(downloadUrl(null)));
    }

    private void directory() {
        removeButton.disabled = true;
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
    }

    private void viewInEditor(ContentEntry contentEntry) {
        removeButton.disabled = false;
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, true);
        Elements.setVisible(editor.asElement(), true);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, false);
        adjustEditorHeight();

        editorStatus.textContent = contentEntry.name + " - " + Format.humanReadableFileSize(contentEntry.fileSize);
        downloadButton.setAttribute("href", downloadUrl(contentEntry)); //NON-NLS
        loadContent(contentEntry, result -> {
            saveButton.disabled = true;
            editor.setModeFromPath(contentEntry.name);
            editor.getEditor().getSession().setValue(result);
            editor.getEditor().getSession().on("change", delta -> saveButton.disabled = false); //NON-NLS
        });
    }

    private void viewInPreview(ContentEntry contentEntry) {
        removeButton.disabled = false;
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), false);
        Elements.setVisible(previewContainer, true);

        previewImage.src = downloadUrl(contentEntry);
    }

    private void unsupportedFileType() {
        removeButton.disabled = false;
        Elements.setVisible(pleaseSelect.asElement(), false);
        Elements.setVisible(editorControls, false);
        Elements.setVisible(editor.asElement(), false);
        Elements.setVisible(deploymentPreview.asElement(), false);
        Elements.setVisible(unsupportedFileType.asElement(), true);
        Elements.setVisible(previewContainer, false);
    }


    // ------------------------------------------------------ event handler

    private void newContent() {
        NameItem nameItem = new NameItem();
        TextBoxItem pathItem = new TextBoxItem(TARGET_PATH);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.CONTENT_NEW, Metadata.empty())
                .unboundFormItem(nameItem)
                .unboundFormItem(pathItem)
                .addOnly()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().newContent(), form, (name, model) -> {
            String path = fullPath(pathItem.getValue(), nameItem.getValue());
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
            ModelNode contentNode = new ModelNode();
            contentNode.get(INPUT_STREAM_INDEX).set(0);
            contentNode.get(TARGET_PATH).set(path);
            Operation operation = new Operation.Builder(address, ADD_CONTENT)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();
            dispatcher.upload(file(nameItem.getValue(), ""), operation, result -> {
                refreshCallback.execute();
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().newContentSuccess(content.getName(), path)));
            });
        });
        pathItem.setValue(selectedPath());
        dialog.show();
    }

    private void addContent() {

    }

    private void saveContent() {
        Node<ContentEntry> selection = tree.api().getSelected();
        if (selection != null) {
            String filename = Strings.substringAfterLast(selection.data.path, "/");
            String editorContent = editor.getEditor().getSession().getValue();
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
            ModelNode contentNode = new ModelNode();
            contentNode.get(INPUT_STREAM_INDEX).set(0);
            contentNode.get(TARGET_PATH).set(selection.data.path);
            Operation operation = new Operation.Builder(address, ADD_CONTENT)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();
            dispatcher.upload(file(filename, editorContent), operation, result -> {
                saveButton.disabled = true;
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().saveContentSuccess(content.getName(), filename)));
                Completable refresh = Completable.fromAction(refreshCallback::execute);
                Completable select = Completable.fromAction(() -> tree.select(selection.id, false));
                refresh.andThen(select).subscribe();
            });
        }
    }

    private void removeContent() {
        Node<ContentEntry> selection = tree.api().getSelected();
        if (selection != null) {
            String path = selection.data.path;
            DialogFactory.showConfirmation(resources.constants().removeContent(),
                    resources.messages().removeContentQuestion(content.getName(), path), () -> {
                        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
                        Operation operation = new Operation.Builder(address, REMOVE_CONTENT)
                                .param(PATHS, new ModelNode().add(path))
                                .build();
                        dispatcher.execute(operation, result -> {
                            refreshCallback.execute();
                            MessageEvent.fire(eventBus, Message.success(
                                    resources.messages().removeContentSuccess(content.getName(), path)));
                        });
                    });
        }
    }

    private void collapse(Node<ContentEntry> node) {
        if (node != null) {
            tree.select(node.id, true);
        }
    }

    private void selectNode(SelectionContext<ContentEntry> selection) {
        if (selection.selected.length != 0) {
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


    // ------------------------------------------------------ helper methods

    private String selectedPath() {
        String path = null;
        Node<ContentEntry> selection = tree.api().getSelected();
        if (selection != null && !selection.id.equals(Ids.CONTENT_TREE_ROOT)) {
            path = Strings.strip(selection.data.path, "/");
            if (!selection.data.directory) {
                path = Strings.getParent(path);
            }
        }
        return nullToEmpty(path);
    }

    private String fullPath(String path, String name) {
        String localPath = Strings.strip(path, "/");
        String localName = Strings.strip(name, "/");
        return isNullOrEmpty(localPath)
                ? nullToEmpty(localName)
                : nullToEmpty(localPath) + "/" + nullToEmpty(localName);
    }

    private File file(String name, String content) {
        ConstructorContentsArrayUnionType contents = ConstructorContentsArrayUnionType.of(content);
        return new File(new ConstructorContentsArrayUnionType[]{contents}, name);
    }

    private void loadContent(ContentEntry contentEntry, Consumer<String> successCallback) {
        if (!contentEntry.directory) {
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
            Operation operation = new Operation.Builder(address, READ_CONTENT)
                    .param(PATH, contentEntry.path)
                    .build();
            dispatcher.download(operation, successCallback);
        }
    }

    private String downloadUrl(ContentEntry contentEntry) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
        Operation.Builder builder = new Operation.Builder(address, READ_CONTENT);
        if (contentEntry != null) {
            builder.param(PATH, contentEntry.path);
        }
        return dispatcher.downloadUrl(builder.build());
    }
}
