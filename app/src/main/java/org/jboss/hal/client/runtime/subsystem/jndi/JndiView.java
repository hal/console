/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.jndi;

import javax.inject.Inject;

import elemental2.core.JsArray;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Search;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_SMALL;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.*;

public class JndiView extends HalViewImpl implements JndiPresenter.MyView {

    private static final String JAVA_CONTEXTS = "java: contexts";
    private static final String APPLICATIONS = "applications";

    private HTMLElement header;
    private HTMLElement treeContainer;
    private Tree<JndiContext> tree;
    private HTMLElement hint;
    private Search search;
    private Form<ModelNode> details;
    private JndiPresenter presenter;

    @Inject
    public JndiView(JndiResources jndiResources, Resources resources) {

        search = new Search.Builder(Ids.JNDI_SEARCH, query -> tree.search(query))
                .onClear(() -> tree.clearSearch())
                .build();

        Metadata metadata = Metadata.staticDescription(jndiResources.jndi());
        details = new ModelNodeForm.Builder<>(Ids.JNDI_DETAILS, metadata)
                .include("uri", CLASS_NAME, VALUE)
                .readOnly()
                .unsorted()
                .build();
        registerAttachable(details);

        HTMLElement root = row()
                .add(column(4)
                        .add(header = h(1).textContent(resources.constants().jndiTree()).get())
                        .add(div().css(flexRow)
                                .add(div().css(btnGroup, marginRightSmall)
                                        .add(button().css(btn, btnDefault)
                                                .on(click, event -> presenter.reload())
                                                .add(i().css(fontAwesome(CSS.refresh))))
                                        .add(button().css(btn, btnDefault)
                                                .on(click, event -> {
                                                    Node<JndiContext> selection = tree.getSelected();
                                                    if (selection != null) {
                                                        tree.selectNode(selection.id, true);
                                                    }
                                                })
                                                .add(i().css(fontAwesome("minus")))))
                                .add(search))
                        .add(treeContainer = div().css(CSS.treeContainer).get()))
                .add(column(8)
                        .add(h(1).textContent(resources.constants().details()))
                        .add(hint = p().textContent(resources.constants().noDetails()).get())
                        .add(details))
                .get();
        initElement(root);
    }

    @Override
    public void setPresenter(JndiPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void attach() {
        super.attach();
        adjustHeight();
    }

    private void adjustHeight() {
        int headerHeight = (int) header.offsetHeight;
        int searchHeight = (int) search.element().offsetHeight;
        int offset = applicationOffset() + 2 * MARGIN_BIG + headerHeight + searchHeight + 2 * MARGIN_SMALL;
        treeContainer.style.height = vh(offset);
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(ModelNode jndi) {
        JndiParser jndiParser = new JndiParser();
        JsArray<Node<JndiContext>> nodes = new JsArray<>();

        if (jndi.hasDefined(JAVA_CONTEXTS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_JAVA_CONTEXTS_ROOT, "Java Contexts", jndiContext)
                    .root()
                    .folder()
                    .open()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(JAVA_CONTEXTS).asPropertyList());
        }

        if (jndi.hasDefined(APPLICATIONS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_APPLICATIONS_ROOT, "Applications", jndiContext)
                    .root()
                    .folder()
                    .open()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(APPLICATIONS).asPropertyList());
        }

        tree = new Tree<>(Ids.JNDI_TREE, nodes);
        Elements.removeChildrenFrom(treeContainer);
        treeContainer.appendChild(tree.element());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> {
            if (!"ready".equals(selectionContext.action)) {
                boolean hasSelection = selectionContext.selected.length != 0;
                boolean validSelection = hasSelection && selectionContext.node.data.hasDetails;
                setVisible(hint, !validSelection);
                setVisible(details.element(), validSelection);
                if (validSelection) {
                    JndiContext jndiContext = selectionContext.node.data;
                    ModelNode modelNode = new ModelNode();
                    if (jndiContext.uri != null) {
                        modelNode.get("uri").set(jndiContext.uri);
                    }
                    if (jndiContext.className != null) {
                        modelNode.get(CLASS_NAME).set(jndiContext.className);
                    }
                    if (jndiContext.value != null) {
                        modelNode.get(VALUE).set(jndiContext.value);
                    }
                    details.view(modelNode);
                }
            }
        });

        setVisible(hint, true);
        setVisible(details.element(), false);
    }
}
