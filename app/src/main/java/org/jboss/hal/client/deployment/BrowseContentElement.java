/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.deployment;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.editor.AceEditor;
import org.jboss.hal.ballroom.editor.Options;
import org.jboss.hal.ballroom.form.FileItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.SelectionContext;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.js.Browser;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Strings;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.common.collect.Sets;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.core.JsArray;
import elemental2.dom.File;
import elemental2.dom.File.ConstructorContentsArrayUnionType;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.promise.Promise;

import static java.lang.Math.max;
import static java.util.Collections.emptyList;

import static com.google.common.base.Strings.nullToEmpty;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.img;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.ballroom.Skeleton.applicationHeight;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.client.deployment.ContentParser.NODE_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BROWSE_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INPUT_STREAM_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATHS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TARGET_PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.btnToolbar;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.flexRow;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.height;
import static org.jboss.hal.resources.CSS.imgResponsive;
import static org.jboss.hal.resources.CSS.imgThumbnail;
import static org.jboss.hal.resources.CSS.marginBottomLarge;
import static org.jboss.hal.resources.CSS.marginBottomSmall;
import static org.jboss.hal.resources.CSS.marginLeftSmall;
import static org.jboss.hal.resources.CSS.marginRightSmall;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.px;
import static org.jboss.hal.resources.CSS.refresh;
import static org.jboss.hal.resources.CSS.vh;

/** UI element to browse and modify the content of an item from the content repository. */
// TODO Use metadata to show/hide buttons according to the security context
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class BrowseContentElement implements IsElement<HTMLElement>, Attachable {

    @SuppressWarnings("HardCodedStringLiteral") private static final Set<String> EDITOR_FILE_TYPES = Sets.newHashSet(
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

    @SuppressWarnings("HardCodedStringLiteral") private static final Set<String> IMAGE_FILE_TYPES = Sets.newHashSet(
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
    private static final AddressTemplate CONTENT_TEMPLATE = AddressTemplate.of("/deployment=*");

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;

    private final HTMLElement root;
    private final Search treeSearch;
    private Tree<ContentEntry> tree;
    private final EmptyState pleaseSelect;
    private final EmptyState deploymentPreview;
    private final EmptyState explodedPreview;
    private final EmptyState unsupportedFileType;
    private final AceEditor editor;

    private final HTMLButtonElement collapseButton;
    private final HTMLButtonElement expandAllButton;
    private final Optional<HTMLButtonElement> addContentButton;
    private final Optional<HTMLButtonElement> uploadContentButton;
    private final HTMLElement downloadContentLink;
    private final Optional<HTMLButtonElement> removeContentButton;
    private final HTMLElement treeContainer;
    private final HTMLElement editorControls;
    private final HTMLElement editorStatus;
    private final Optional<HTMLButtonElement> saveContentButton;
    private final HTMLElement previewContainer;
    private final HTMLElement previewHeader;
    private final HTMLElement previewImageContainer;
    private final HTMLImageElement previewImage;

    private Content content;
    private int surroundingHeight;

    // ------------------------------------------------------ ui setup

    BrowseContentElement(Dispatcher dispatcher, Environment environment, EventBus eventBus, Metadata metadata,
            Resources resources) {
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.surroundingHeight = 0;

        treeSearch = new Search.Builder(Ids.CONTENT_TREE_SEARCH, query -> tree.search(query))
                .onClear(() -> tree.clearSearch())
                .build();
        treeSearch.element().classList.add(marginLeftSmall);

        Options editorOptions = new Options();
        editorOptions.showGutter = true;
        editorOptions.showLineNumbers = true;
        editorOptions.showPrintMargin = false;
        editor = new AceEditor(Ids.CONTENT_EDITOR, editorOptions);

        Search contentSearch = new Search.Builder(Ids.CONTENT_SEARCH,
                query -> editor.getEditor().find(query))
                .onPrevious(query -> editor.getEditor().findPrevious())
                .onNext(query -> editor.getEditor().findNext())
                .build();
        contentSearch.element().classList.add(marginRightSmall);

        pleaseSelect = new EmptyState.Builder(Ids.BROWSE_CONTENT_SELECT_EMPTY, resources.constants().nothingSelected())
                .icon(Icons.INFO)
                .description(resources.messages().noContentSelectedInDeployment())
                .build();

        deploymentPreview = new EmptyState.Builder(Ids.BROWSE_CONTENT_DEPLOYMENT_EMPTY, Names.DEPLOYMENT)
                .icon(fontAwesome("archive"))
                .description(resources.messages().deploymentPreview())
                .build();

        explodedPreview = new EmptyState.Builder(Ids.BROWSE_CONTENT_EXPLODED_EMPTY, Names.DEPLOYMENT)
                .icon(fontAwesome("folder-open"))
                .description(resources.messages().explodedPreview())
                .build();

        unsupportedFileType = new EmptyState.Builder(Ids.BROWSE_CONTENT_UNSUPPORTED_EMPTY,
                resources.constants().unsupportedFileType())
                .icon(Icons.UNKNOWN)
                .description(resources.messages().unsupportedFileTypeDescription())
                .primaryAction(resources.constants().download(),
                        () -> window.location.assign(downloadUrl((tree.getSelected().data))))
                .secondaryAction(resources.constants().viewInEditor(),
                        () -> viewInEditor(tree.getSelected().data))
                .build();

        HTMLElement crudContainer;
        root = row()
                .add(column(4)
                        .add(div().css(flexRow, marginTopLarge)
                                .add(div().css(btnToolbar)
                                        .add(div().css(btnGroup)
                                                .add(button().css(btn, btnDefault)
                                                        .on(click, event -> refresh())
                                                        .title(resources.constants().refresh())
                                                        .add(i().css(fontAwesome(refresh))))
                                                .add(expandAllButton = button().css(btn, btnDefault)
                                                        .on(click, event -> {
                                                            tree.openAllNodes();
                                                        })
                                                        .title(resources.constants().expandAll())
                                                        .add(i().css(fontAwesome(CSS.list))).element())
                                                .add(collapseButton = button().css(btn, btnDefault)
                                                        .on(click, event -> {
                                                            Node<ContentEntry> selection = tree.getSelected();
                                                            if (selection != null) {
                                                                tree.selectNode(selection.id, true);
                                                            }
                                                        })
                                                        .title(resources.constants().collapse())
                                                        .add(i().css(fontAwesome("minus"))).element()))
                                        .add(crudContainer = div().css(btnGroup)
                                                .add(downloadContentLink = a().css(btn, btnDefault)
                                                        .title(resources.constants().download())
                                                        .attr(UIConstants.TARGET, "_blank") // NON-NLS
                                                        .attr(UIConstants.ROLE, UIConstants.BUTTON)
                                                        .add(span().css(fontAwesome("download"))).element())
                                                .element()))
                                .add(treeSearch))
                        .add(treeContainer = div().css(CSS.treeContainer).element()))
                .add(column(8)
                        .add(div().css(marginTopLarge, marginBottomLarge)
                                .add(previewContainer = div()
                                        .add(previewHeader = h(1)
                                                .textContent(resources.constants().preview()).element())
                                        .add(previewImageContainer = div()
                                                .style("overflow: scroll") // NON-NLS
                                                .add(previewImage = img().css(imgResponsive, imgThumbnail).element())
                                                .element())
                                        .element())
                                .add(editorControls = div().css(CSS.editorControls, marginBottomSmall)
                                        .add(contentSearch)
                                        .add(div().add(editorStatus = span()
                                                .textContent(resources.constants().nothingSelected()).element()))
                                        .element())
                                .add(editor)
                                .add(pleaseSelect)
                                .add(deploymentPreview)
                                .add(explodedPreview)
                                .add(unsupportedFileType)))
                .element();

        boolean supported = !(Browser.isEdge() || Browser.isIE());
        AuthorisationDecision ad = AuthorisationDecision.from(environment, metadata.getSecurityContext());
        if (supported && ad.isAllowed(Constraint.executable(CONTENT_TEMPLATE, ADD_CONTENT))) {
            addContentButton = Optional.of(button().css(btn, btnDefault)
                    .on(click, event -> addContent())
                    .title(resources.constants().newContent())
                    .add(i().css(fontAwesome("file-o"))).element());
            uploadContentButton = Optional.of(button().css(btn, btnDefault)
                    .on(click, event -> uploadContent())
                    .title(resources.constants().uploadContent())
                    .title(resources.constants().addContent())
                    .add(i().css(fontAwesome("upload"))).element());
            saveContentButton = Optional.of(button().css(btn, btnDefault, marginRightSmall)
                    .on(click, event -> saveContent())
                    .title(resources.constants().save())
                    .add(span().css(fontAwesome("floppy-o"))).element());
            crudContainer.insertBefore(addContentButton.get(), downloadContentLink);
            crudContainer.insertBefore(uploadContentButton.get(), downloadContentLink);
            editorControls.insertBefore(saveContentButton.get(), contentSearch.element());

        } else {
            addContentButton = Optional.empty();
            uploadContentButton = Optional.empty();
            saveContentButton = Optional.empty();
        }
        if (ad.isAllowed(Constraint.executable(CONTENT_TEMPLATE, REMOVE_CONTENT))) {
            removeContentButton = Optional.of(button().css(btn, btnDefault)
                    .on(click, event -> removeContent())
                    .title(resources.constants().removeContent())
                    .add(i().css(pfIcon("remove"))).element());
            crudContainer.appendChild(removeContentButton.get());
        } else {
            removeContentButton = Optional.empty();
        }

        saveContentButton.ifPresent(button -> button.disabled = true);
        setVisible(pleaseSelect.element(), true);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, false);
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
    public HTMLElement element() {
        return root;
    }

    void setSurroundingHeight(int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
        adjustEditorHeight();
    }

    private void adjustHeight() {
        int treeOffset = applicationOffset() +
                2 * MARGIN_BIG + treeSearch.element().offsetHeight + MARGIN_SMALL + surroundingHeight;
        int previewHeaderHeight = previewHeader.offsetHeight;
        int previewOffset = applicationOffset() +
                2 * MARGIN_BIG + MARGIN_SMALL + previewHeaderHeight + surroundingHeight;

        treeContainer.style.height = vh(treeOffset);
        previewImageContainer.style.height = vh(previewOffset);
    }

    private void adjustEditorHeight() {
        int editorHeight = applicationHeight() -
                2 * MARGIN_BIG - MARGIN_SMALL - editorControls.offsetHeight - surroundingHeight;

        if (Elements.isVisible(editor.element())) {
            editor.element().style.height = height(px(max(editorHeight, MIN_HEIGHT)));
            editor.getEditor().resize();
        }
    }

    // ------------------------------------------------------ deployment methods

    private String downloadUrl(ContentEntry contentEntry) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
        Operation.Builder builder = new Operation.Builder(address, READ_CONTENT);
        if (contentEntry != null) {
            builder.param(PATH, contentEntry.path);
        }
        return dispatcher.downloadUrl(builder.build());
    }

    private void refresh() {
        String selectedId = selectedId();
        browseContent()
                .then(__ -> awaitTreeReady())
                .then(__ -> {
                    if (selectedId != null) {
                        tree.selectNode(selectedId);
                    }
                    return null;
                });
    }

    // ------------------------------------------------------ CRUD content methods

    void setContent(Content content) {
        this.content = content;
        setVisible(addContentButton.orElse(null), content.isExploded());
        setVisible(uploadContentButton.orElse(null), content.isExploded());
        setVisible(removeContentButton.orElse(null), content.isExploded());
        setVisible(saveContentButton.orElse(null), content.isExploded());
        editor.getEditor().setReadOnly(!content.isExploded());

        browseContent().then(__ -> {
            noSelection();
            return null;
        });
    }

    private void addContent() {
        TextBoxItem targetPathItem = new TextBoxItem(TARGET_PATH);
        targetPathItem.setRequired(true);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.CONTENT_NEW, Metadata.empty())
                .unboundFormItem(targetPathItem)
                .addOnly()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().newContent(), form, (name, model) -> {
            String path = targetPathItem.getValue();
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
            ModelNode contentNode = new ModelNode();
            contentNode.get(INPUT_STREAM_INDEX).set(0);
            contentNode.get(TARGET_PATH).set(path);
            Operation operation = new Operation.Builder(address, ADD_CONTENT)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();
            dispatcher.upload(file(filename(path), ""), operation)
                    .then(__ -> browseContent())
                    .then(__ -> awaitTreeReady())
                    .then(__ -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().newContentSuccess(content.getName(), path)));
                        tree.selectNode(NODE_ID.apply(path));
                        return null;
                    });
        });
        targetPathItem.setValue(selectedPath());
        dialog.show();
    }

    private void uploadContent() {
        LabelBuilder labelBuilder = new LabelBuilder();
        TextBoxItem targetPathItem = new TextBoxItem(TARGET_PATH);
        targetPathItem.setRequired(true);

        FileItem fileItem = new FileItem(FILE, labelBuilder.label(FILE));
        fileItem.addValueChangeHandler(
                event -> targetPathItem.setValue(appendFilename(targetPathItem.getValue(), event.getValue().name)));

        TextBoxItem urlItem = new TextBoxItem(URL, labelBuilder.label(URL));
        urlItem.addValueChangeHandler(
                event -> targetPathItem.setValue(appendFilename(targetPathItem.getValue(), filename(event.getValue()))));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.CONTENT_NEW, Metadata.empty())
                .unboundFormItem(fileItem)
                .unboundFormItem(urlItem)
                .unboundFormItem(targetPathItem)
                .addOnly()
                .build();
        form.addFormValidation(f -> {
            if (fileItem.isEmpty() && urlItem.isEmpty()) {
                return ValidationResult.invalid(resources.messages().uploadContentInvalid());
            }
            return ValidationResult.OK;
        });
        form.setSaveCallback((f, model) -> {
            String path = targetPathItem.getValue();
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());

            ModelNode contentNode = new ModelNode();
            if (fileItem.isEmpty()) {
                contentNode.get(URL).set(urlItem.getValue());
            } else {
                contentNode.get(INPUT_STREAM_INDEX).set(0);
            }
            contentNode.get(TARGET_PATH).set(path);
            Operation operation = new Operation.Builder(address, ADD_CONTENT)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();

            Promise<ModelNode> promise = fileItem.isEmpty()
                    ? dispatcher.execute(operation)
                    : dispatcher.upload(fileItem.getValue(), operation);
            promise.then(__ -> browseContent())
                    .then(__ -> awaitTreeReady())
                    .then(__ -> {
                        // It's safe to use `path` instead of `safePath` in `newContentSuccess()`, because the method
                        // returns encoded SafeHtml.
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().newContentSuccess(content.getName(), path)));
                        tree.selectNode(NODE_ID.apply(path));
                        return null;
                    });
        });

        Dialog dialog = new Dialog.Builder(resources.constants().uploadContent())
                .add(p().innerHtml(resources.messages().uploadContentDescription()).element())
                .add(form.element())
                .primary(resources.constants().upload(), form::save)
                .size(Dialog.Size.MEDIUM)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        targetPathItem.setValue(selectedPath());
        dialog.show();
        form.edit(new ModelNode());
    }

    @SuppressWarnings("unchecked")
    private Promise<Void> browseContent() {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
        Operation operation = new Operation.Builder(address, BROWSE_CONTENT).build();
        return dispatcher.execute(operation)
                .then(result -> {
                    String contentName = SafeHtmlUtils.htmlEscapeAllowEntities(content.getName());
                    Node<ContentEntry> root = new Node.Builder<>(Ids.CONTENT_TREE_ROOT, contentName,
                            new ContentEntry())
                            .root()
                            .folder()
                            .open()
                            .build();
                    JsArray<Node<ContentEntry>> nodes = new JsArray<>();
                    new ContentParser().parse(root, nodes, result.isDefined() ? result.asList() : emptyList());

                    if (tree != null) {
                        tree.destroy();
                        tree = null;
                    }
                    tree = new Tree<>(Ids.CONTENT_TREE, nodes);
                    Elements.removeChildrenFrom(treeContainer);
                    treeContainer.appendChild(tree.element());
                    tree.attach();
                    tree.onSelectionChange((event, selectionContext) -> {
                        if (!"ready".equals(selectionContext.action)) { // NON-NLS
                            onNodeSelected(selectionContext);
                        }
                    });
                    return Promise.resolve((Void) null);
                });
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

    private void saveContent() {
        Node<ContentEntry> selection = tree.getSelected();
        if (selection != null) {
            String filename = selection.data.path.contains("/")
                    ? Strings.substringAfterLast(selection.data.path, "/")
                    : selection.data.path;
            String editorContent = editor.getEditor().getSession().getValue();

            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
            ModelNode contentNode = new ModelNode();
            contentNode.get(INPUT_STREAM_INDEX).set(0);
            contentNode.get(TARGET_PATH).set(selection.data.path);
            Operation operation = new Operation.Builder(address, ADD_CONTENT)
                    .param(CONTENT, new ModelNode().add(contentNode))
                    .build();

            dispatcher.upload(file(filename, editorContent), operation)
                    .then(__ -> {
                        saveContentButton.ifPresent(button -> button.disabled = true);
                        return Promise.resolve((Void) null);
                    })
                    .then(__ -> browseContent())
                    .then(__ -> awaitTreeReady())
                    .then(__ -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().saveContentSuccess(content.getName(), filename)));
                        tree.selectNode(selection.id);
                        return null;
                    });
        }
    }

    private void removeContent() {
        Node<ContentEntry> selection = tree.getSelected();
        if (selection != null) {
            String path = selection.data.path;
            DialogFactory.buildConfirmation(resources.constants().removeContent(),
                    resources.messages().removeContentQuestion(content.getName(), SafeHtmlUtils.fromTrustedString(path)), null,
                    Dialog.Size.MEDIUM,
                    () -> {
                        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
                        Operation operation = new Operation.Builder(address, REMOVE_CONTENT)
                                .param(PATHS, new ModelNode().add(path))
                                .build();
                        dispatcher.execute(operation)
                                .then(__ -> browseContent())
                                .then(__ -> awaitTreeReady())
                                .then(__ -> {
                                    MessageEvent.fire(eventBus, Message.success(
                                            resources.messages()
                                                    .removeContentSuccess(content.getName(),
                                                            SafeHtmlUtils.fromTrustedString(path))));
                                    noSelection();
                                    return null;
                                });
                    }).show();
        }
    }

    // ------------------------------------------------------ UI state

    private void onNodeSelected(SelectionContext<ContentEntry> selection) {
        collapseButton.disabled = selection.selected.length == 0;
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
                            ? contentEntry.name.substring(index + 1)
                            : "";

                    if (EDITOR_FILE_TYPES.contains(extension)) {
                        viewInEditor(contentEntry);

                    } else if (IMAGE_FILE_TYPES.contains(extension)) {
                        viewInPreview(contentEntry);

                    } else {
                        unsupportedFileType(contentEntry);
                    }
                }
            }

        } else {
            noSelection();
        }
    }

    private void noSelection() {
        collapseButton.disabled = true;
        downloadContentLink.classList.add(disabled);
        removeContentButton.ifPresent(button -> button.disabled = true);

        setVisible(pleaseSelect.element(), true);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, false);
    }

    private void deploymentPreview() {
        if (content.isExploded()) {
            downloadContentLink.removeAttribute(UIConstants.HREF);
            downloadContentLink.removeAttribute(UIConstants.DOWNLOAD);
            downloadContentLink.classList.add(disabled);
        } else {
            downloadContentLink.setAttribute(UIConstants.HREF, downloadUrl(null));
            downloadContentLink.setAttribute(UIConstants.DOWNLOAD, content.getName());
            downloadContentLink.classList.remove(disabled);
        }
        removeContentButton.ifPresent(button -> button.disabled = true);

        setVisible(pleaseSelect.element(), false);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), !content.isExploded());
        setVisible(explodedPreview.element(), content.isExploded());
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, false);

        deploymentPreview.setHeader(content.getName());
        deploymentPreview.setPrimaryAction(resources.constants().download(),
                () -> window.location.assign(downloadUrl(null)));
    }

    private void directory() {
        downloadContentLink.removeAttribute(UIConstants.HREF);
        downloadContentLink.removeAttribute(UIConstants.DOWNLOAD);
        downloadContentLink.classList.add(disabled);
        removeContentButton.ifPresent(button -> button.disabled = true);

        setVisible(pleaseSelect.element(), false);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, false);
    }

    private void viewInEditor(ContentEntry contentEntry) {
        downloadContentLink.setAttribute(UIConstants.HREF, downloadUrl(contentEntry));
        downloadContentLink.setAttribute(UIConstants.DOWNLOAD, contentEntry.name);
        downloadContentLink.classList.remove(disabled);
        removeContentButton.ifPresent(button -> button.disabled = false);

        setVisible(pleaseSelect.element(), false);
        setVisible(editorControls, true);
        setVisible(editor.element(), true);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, false);
        adjustEditorHeight();

        editorStatus.innerHTML = contentEntry.name + " - " + Format.humanReadableFileSize(contentEntry.fileSize);
        loadContent(contentEntry, result -> {
            editor.setModeFromPath(contentEntry.name);
            editor.getEditor().getSession().setValue(result);
            editor.getEditor().getSession().on("change", // NON-NLS
                    delta -> saveContentButton.ifPresent(button -> button.disabled = false));
            saveContentButton.ifPresent(button -> button.disabled = true);
        });
    }

    private void viewInPreview(ContentEntry contentEntry) {
        downloadContentLink.setAttribute(UIConstants.HREF, downloadUrl(contentEntry));
        downloadContentLink.setAttribute(UIConstants.DOWNLOAD, contentEntry.name);
        downloadContentLink.classList.remove(disabled);
        removeContentButton.ifPresent(button -> button.disabled = false);
        previewImage.src = downloadUrl(contentEntry);

        setVisible(pleaseSelect.element(), false);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), false);
        setVisible(previewContainer, true);
    }

    private void unsupportedFileType(ContentEntry contentEntry) {
        downloadContentLink.setAttribute(UIConstants.HREF, downloadUrl(contentEntry));
        downloadContentLink.setAttribute(UIConstants.DOWNLOAD, contentEntry.name);
        downloadContentLink.classList.remove(disabled);
        removeContentButton.ifPresent(button -> button.disabled = false);

        setVisible(pleaseSelect.element(), false);
        setVisible(editorControls, false);
        setVisible(editor.element(), false);
        setVisible(deploymentPreview.element(), false);
        setVisible(explodedPreview.element(), false);
        setVisible(unsupportedFileType.element(), true);
        setVisible(previewContainer, false);
    }

    // ------------------------------------------------------ helper methods

    private String selectedId() {
        if (tree != null) {
            Node<ContentEntry> selection = tree.getSelected();
            if (selection != null) {
                return selection.id;
            }
        }
        return null;
    }

    private String selectedPath() {
        String path = null;
        Node<ContentEntry> selection = tree.getSelected();
        if (selection != null && !selection.id.equals(Ids.CONTENT_TREE_ROOT)) {
            path = Strings.strip(selection.data.path, "/");
            if (!selection.data.directory) {
                path = Strings.getParent(path);
            }
            if (path != null) {
                path += "/";
            }
        }
        return nullToEmpty(path);
    }

    private String appendFilename(String path, String file) {
        if (!com.google.common.base.Strings.isNullOrEmpty(path)) {
            if (path.endsWith("/")) {
                return path + file;
            } else {
                return Strings.getParent(path) + "/" + file;
            }
        }
        return file;
    }

    private String filename(String path) {
        if (path != null) {
            return path.contains("/")
                    ? Strings.substringAfterLast(path, "/")
                    : path;
        }
        return null;
    }

    private File file(String name, String content) {
        ConstructorContentsArrayUnionType contents = ConstructorContentsArrayUnionType.of(content);
        return new File(new ConstructorContentsArrayUnionType[] { contents }, name);
    }

    private Promise<Void> awaitTreeReady() {
        return new Promise<>((resolve, reject) -> tree.onReady(e -> resolve.onInvoke((Void) null)));
    }
}
