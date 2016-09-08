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
package org.jboss.hal.client.runtime.subsystem.jndi;

import javax.inject.Inject;

import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.meta.security.SecurityContext.RWX;

/**
 * @author Harald Pehl
 */
public class JndiView extends PatternFlyViewImpl implements JndiPresenter.MyView {

    private static final String JAVA_CONTEXTS = "java: contexts";
    private static final String APPLICATIONS = "applications";
    private static final String TREE_HOLDER = "treeHolder";
    private static final String HINT = "hint";

    private JndiPresenter presenter;
    private Element treeHolder;
    private Element hint;
    private Form<ModelNode> details;

    @Inject
    public JndiView(Capabilities capabilities, JndiResources jndiResources, Resources resources) {

        Metadata metadata = new Metadata(RWX, StaticResourceDescription.from(jndiResources.jndi()), capabilities);
        details = new ModelNodeForm.Builder<>(Ids.JNDI_DETAILS, metadata)
                .include("uri", CLASS_NAME, VALUE)
                .viewOnly()
                .unsorted()
                .build();
        registerAttachable(details);

        // @formatter:off
        LayoutBuilder builder = new LayoutBuilder()
            .row()
                .column(4)
                    .h(1).textContent(resources.constants().jndiTree()).end()
                    .div().rememberAs(TREE_HOLDER).end()
                .end()
                .column(8)
                    .h(1).textContent(resources.constants().details()).end()
                    .p().rememberAs(HINT).textContent(resources.constants().jndiDetails()).end()
                    .add(details)
                .end()
            .end();
        // @formatter:on

        treeHolder = builder.referenceFor(TREE_HOLDER);
        hint = builder.referenceFor(HINT);
        Elements.setVisible(hint, true);
        Elements.setVisible(details.asElement(), false);
        initElements(builder.elements());
    }

    @Override
    public void setPresenter(final JndiPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void update(final ModelNode jndi) {
        JndiParser jndiParser = new JndiParser();
        JsArrayOf<Node<JndiContext>> nodes = JsArrayOf.create();

        if (jndi.hasDefined(JAVA_CONTEXTS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_JAVA_CONTEXTS_ROOT, "Java Contexts", jndiContext)
                    .root()
                    .folder()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(JAVA_CONTEXTS).asPropertyList());
        }

        if (jndi.hasDefined(APPLICATIONS)) {
            JndiContext jndiContext = new JndiContext();
            Node<JndiContext> root = new Node.Builder<>(Ids.JNDI_TREE_APPLICATIONS_ROOT, "Applications", jndiContext)
                    .root()
                    .folder()
                    .build();
            jndiParser.parse(nodes, root, jndi.get(APPLICATIONS).asPropertyList());
        }

        Tree<JndiContext> tree = new Tree<>(Ids.JNDI_TREE, nodes);
        Elements.removeChildrenFrom(treeHolder);
        treeHolder.appendChild(tree.asElement());

        tree.attach();
        tree.onSelectionChange((event, selectionContext) -> {
            if (!"ready".equals(selectionContext.action)) { //NON-NLS
                boolean hasSelection = !selectionContext.selected.isEmpty();
                boolean validSelection = hasSelection;
                Elements.setVisible(hint, !validSelection);
                Elements.setVisible(details.asElement(), validSelection);
            }
        });
    }
}
