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
package org.jboss.hal.client.patching;

import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class PatchingView extends PatternFlyViewImpl implements PatchingPresenter.MyView {

    private static final Logger logger = LoggerFactory.getLogger(PatchingView.class);

    @Inject
    public PatchingView(Dispatcher dispatcher,
            Resources resources) {
        Node root = new Node.Builder("root", resources.constants().modelBrowser()).folder().build(); //NON-NLS
        Tree tree = new Tree(Ids.MODEL_BROWSER, SecurityContext.RWX, root, (node, callback) -> {
            logger.debug("Loading nodes for '{}'", node.id); //NON-NLS
            if (root.id.equals(node.id)) {
                Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                        .param(CHILD_TYPE, PROFILE)
                        .build();
                dispatcher.execute(operation, result -> {
                    List<ModelNode> names = result.asList();
                    JsArrayOf<Node> children = JsArrayOf.create();
                    for (ModelNode name : names) {
                        Node child = new Node.Builder(name.asString(), name.asString()).build();
                        children.push(child);
                    }
                    callback.result(children);
                });
            }
        });

        // @formatter:off
        Element layout = new LayoutBuilder()
            .startRow()
                .header(resources.constants().modelBrowser())
                .add(tree.asElement())
            .endRow()
        .build();
        // @formatter:on

        registerAttachable(tree);
        initWidget(Elements.asWidget(layout));
    }
}
