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
package org.jboss.hal.client.tools;

import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.model.ResourceAddress;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class ModelBrowserView extends PatternFlyViewImpl implements ModelBrowserPresenter.MyView {

    private final ModelBrowser modelBrowser;

    @Inject
    public ModelBrowserView(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
        initElements(modelBrowser);
    }

    @Override
    public void setRoot(final ResourceAddress root) {
        modelBrowser.setRoot(root, root == ResourceAddress.ROOT, true);
    }
}
