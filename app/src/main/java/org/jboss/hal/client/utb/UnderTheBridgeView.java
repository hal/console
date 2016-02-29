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

import com.google.common.collect.Maps;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class UnderTheBridgeView extends PatternFlyViewImpl implements UnderTheBridgePresenter.MyView {

    private static final String VIDEO = "https://youtu.be/GLvohMXgcBo";
    private static final Map<String, String[]> ATTRIBUTES = Maps.newLinkedHashMap();

    static {
        ATTRIBUTES.put("string-attributes", new String[]{
                "string-required",
                "string-optional",
                "string-default",
                "string-expression",
                "string-restart",
                "string-suggestion",
                "string-suggestion-expression"});
        ATTRIBUTES.put("int-attributes", new String[]{
                "int-required",
                "int-optional",
                "int-default",
                "int-expression",
                "int-restart",
                "int-unit",
                "int-unit-expression"});
        ATTRIBUTES.put("long-attributes", new String[]{
                "long-required",
                "long-optional",
                "long-default",
                "long-expression",
                "long-restart",
                "long-unit",
                "long-unit-expression"});
        ATTRIBUTES.put("boolean-attributes", new String[]{
                "boolean-required",
                "boolean-optional",
                "boolean-default"});
        ATTRIBUTES.put("single-select-attributes", new String[]{
                "single-select-required",
                "single-select-optional",
                "single-select-default"});
        ATTRIBUTES.put("multi-select-attributes", new String[]{
                "multi-select-required",
                "multi-select-optional",
                "multi-select-default"});
        ATTRIBUTES.put("list-attributes", new String[]{
                "list-required",
                "list-optional",
                "list-default",
                "list-expression",
                "list-suggestion",
                "list-suggestion-expression"});
        ATTRIBUTES.put("property-attributes", new String[]{
                "properties-required",
                "properties-optional",
                "properties-default",
                "properties-expression",
                "properties-suggestion",
                "properties-suggestion-expression"});
    }

    private final List<ModelNodeForm<ModelNode>> forms;
    private UnderTheBridgePresenter presenter;

    @Inject
    public UnderTheBridgeView(final Environment environment,
            final StatementContext statementContext,
            final UnderTheBridgeResources resources,
            final Capabilities capabilities) {
        this.forms = new ArrayList<>();

        ResourceAddress address = AddressTemplate.of(environment.isStandalone() ? "" : "/profile=full-ha")
                .resolve(statementContext);
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                .param(CHILD_TYPE, "subsystem")
                .build();

        Tabs tabs = new Tabs();
        ResourceDescription description = StaticResourceDescription.from(resources.underTheBridge());
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> presenter.saveModel(form.getModel());

        for (Map.Entry<String, String[]> entry : ATTRIBUTES.entrySet()) {
            forms.add(new ModelNodeForm.Builder<>(IdBuilder.build("form", entry.getKey()),
                    SecurityContext.RWX, description, capabilities)
                    .include(entry.getValue()).onSave(saveCallback).build());
            tabs.add(IdBuilder.build("tab", entry.getKey()), new LabelBuilder().label(entry.getKey()),
                    forms.get(forms.size() - 1).asElement());
        }

        for (ModelNodeForm<ModelNode> form : forms) {
            for (FormItem item : form.getFormItems()) {
                if (item.getName().contains("-suggestion")) {
                    item.registerSuggestHandler(new Typeahead.ReadChildrenNamesBuilder(operation).build());
                }
            }
            registerAttachable(form);
        }

        // @formatter:off
        Element layout = new LayoutBuilder()
            .row()
                .column()
                    .header("Under The Bridge")
                    .add(
                        new Elements.Builder()
                            .p()
                                .innerText(description.getDescription())
                            .end()
                            .p()
                                .innerHtml(new SafeHtmlBuilder().appendEscaped("If you're wondering about the name of " +
                                        "this page, I came up with the idea for this demo while I was listening to ")
                                        .appendHtmlConstant("<a href=\"" + VIDEO + "\" target=\"_blank\">")
                                        .appendEscaped("Under The Bridge")
                                        .appendHtmlConstant("</a> by Red Hot Chili Peppers.")
                                        .toSafeHtml())
                            .end()
                            .add(tabs.asElement())
                        .elements()
                    )
                .end()
            .end()
        .build();
        // @formatter:on

        initElement(layout);
    }

    @Override
    public void setPresenter(final UnderTheBridgePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(final ModelNode model) {
        for (ModelNodeForm<ModelNode> form : forms) {
            form.view(model);
        }
    }
}
