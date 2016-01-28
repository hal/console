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
package org.jboss.hal.client.utb;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.PatternFlyViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import javax.inject.Inject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class UnderTheBridgeView extends PatternFlyViewImpl implements UnderTheBridgePresenter.MyView {

    private static final String VIDEO = "https://youtu.be/GLvohMXgcBo";
    private final ModelNodeForm<ModelNode> form;
    private UnderTheBridgePresenter presenter;

    @Inject
    public UnderTheBridgeView(final Environment environment,
            final StatementContext statementContext,
            final UnderTheBridgeResources resources) {

        ResourceAddress address = AddressTemplate.of(environment.isStandalone() ? "" : "/profile=full-ha")
                .resolve(statementContext);
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                .param(CHILD_TYPE, "subsystem")
                .build();

        ResourceDescription description = StaticResourceDescription.from(resources.underTheBridge());
        form = new ModelNodeForm.Builder<>("under-the-bridge", SecurityContext.RWX, description)
                .onSave((f, changedValues) -> presenter.saveModel(f.getModel()))
                .build();
        registerAttachable(form);

        // @formatter:off
        Elements.Builder info = new Elements.Builder()
                .p()
                    .innerText(description.getDescription())
                .end()
                .p()
                    .innerHtml(new SafeHtmlBuilder().appendEscaped("If you're wondering about the name of this page, " +
                            "I came up with the idea for this demo while I was listening to ")
                            .appendHtmlConstant("<a href=\"" + VIDEO + "\" target=\"_blank\">")
                            .appendEscaped("Under The Bridge")
                            .appendHtmlConstant("</a> by Red Hot Chili Peppers.")
                            .toSafeHtml())
                .end()
                .add(form.asElement());

        Element layout = new LayoutBuilder()
            .startRow()
                .header("Under The Bridge")
                .add(info.elements())
            .endRow()
        .build();
        // @formatter:on

        initWidget(Elements.asWidget(layout));
    }

    @Override
    public void setPresenter(final UnderTheBridgePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(final ModelNode model) {
        form.view(model);
    }
}
