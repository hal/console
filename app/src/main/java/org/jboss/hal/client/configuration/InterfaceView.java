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
package org.jboss.hal.client.configuration;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;

import javax.inject.Inject;

import static org.jboss.hal.resources.Ids.PATHS_FORM;
import static org.jboss.hal.resources.Names.INTERFACE;

/**
 * @author Harald Pehl
 */
public class InterfaceView extends PatternFlyViewImpl implements InterfacePresenter.MyView {

    private final ModelNodeForm<ModelNode> form;
//    private final Dialog dialog;
    private InterfacePresenter presenter;

    @Inject
    public InterfaceView(ResourceDescriptions descriptions,
            SecurityFramework securityFramework) {

        ResourceDescription description = descriptions.lookup(InterfacePresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(InterfacePresenter.ROOT_TEMPLATE);

//        new Dialog.Builder(resources.messages())

        form = new ModelNodeForm.Builder<>(PATHS_FORM, securityContext, description)
                .exclude("resolved-address")
                .onSave((form, changedValues) -> presenter.saveInterface(changedValues))
                .build();

        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header(INTERFACE)
                .add(form.asElement())
            .endRow()
        .build();
        // @formatter:on

        registerAttachable(form);
        initWidget(Elements.asWidget(element));

    }

    @Override
    public void setPresenter(final InterfacePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void update(final ModelNode interfce) {
        form.view(interfce);
    }
}
