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
package org.jboss.hal.core.modelbrowser;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.ballroom.tree.Tree;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import java.util.Collections;

/**
 * @author Harald Pehl
 */
public class ModelBrowser implements IsElement, Attachable, SecurityContextAware {

    private final Tree<Context> tree;

    public ModelBrowser(final Dispatcher dispatcher, final SecurityContext securityContext,
            final ResourceAddress root) {
        String resource = root == ResourceAddress.ROOT ? Names.MANAGEMENT_MODEL : root.lastValue();
        if ("*".equals(resource)) {
            throw new IllegalArgumentException("Invalid root address: " + root +
                    ". ModelBrowser must be created with a concrete address.");
        }

        Context context = new Context(root, Collections.emptySet());
        Node<Context> rootNode = new Node.Builder<>(IdBuilder.uniqueId(), resource, context)
                .folder()
                .build();
        this.tree = new Tree<>(Ids.MODEL_BROWSER, securityContext, rootNode, new ReadChildren(dispatcher));
    }

    @Override
    public Element asElement() {
        return tree.asElement();
    }

    @Override
    public void attach() {
        tree.attach();
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
