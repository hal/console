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
package org.jboss.hal.client.rhcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.resources.Ids;

import com.google.common.collect.Maps;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;

@SuppressWarnings({ "HardCodedStringLiteral", "DuplicateStringLiteralInspection" })
public class UnderTheBridgeView extends HalViewImpl implements UnderTheBridgePresenter.MyView {

    private static final String VIDEO = "https://youtu.be/GLvohMXgcBo";
    private static final Map<String, String[]> ATTRIBUTES = Maps.newLinkedHashMap();

    static {
        ATTRIBUTES.put("string-attributes", new String[] {
                "password",
                "string-required",
                "string-optional",
                "string-default",
                "string-expression",
                "string-sensitive",
                "string-sensitive-expression",
                "string-restart",
                "string-suggestion",
                "string-suggestion-expression" });
        ATTRIBUTES.put("int-attributes", new String[] {
                "int-required",
                "int-optional",
                "int-default",
                "int-expression",
                "int-restart",
                "int-unit",
                "int-unit-expression" });
        ATTRIBUTES.put("long-attributes", new String[] {
                "long-required",
                "long-optional",
                "long-default",
                "long-expression",
                "long-restart",
                "long-unit",
                "long-unit-expression" });
        ATTRIBUTES.put("boolean-attributes", new String[] {
                "boolean-required",
                "boolean-optional",
                "boolean-default",
                "boolean-expression" });
        ATTRIBUTES.put("single-select-attributes", new String[] {
                "single-select-required",
                "single-select-optional",
                "single-select-default" });
        ATTRIBUTES.put("multi-select-attributes", new String[] {
                "multi-select-required",
                "multi-select-optional",
                "multi-select-default" });
        ATTRIBUTES.put("list-attributes", new String[] {
                "list-required",
                "list-optional",
                "list-default",
                "list-suggestion" });
        ATTRIBUTES.put("property-attributes", new String[] {
                "properties-required",
                "properties-optional",
                "properties-default" });
    }

    private final List<ModelNodeForm<ModelNode>> forms;
    private UnderTheBridgePresenter presenter;

    @Inject
    public UnderTheBridgeView(Dispatcher dispatcher, StatementContext statementContext, Environment environment) {
        this.forms = new ArrayList<>();

        Tabs tabs = new Tabs("utb-tab-container");
        ResourceDescription description = StaticResourceDescription.from(RhcpResources.INSTANCE.underTheBridge());
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> presenter.saveModel(form.getModel());

        for (Map.Entry<String, String[]> entry : ATTRIBUTES.entrySet()) {
            ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(entry.getKey(), Ids.FORM),
                    Metadata.staticDescription(description))
                            .include(entry.getValue())
                            .onSave(saveCallback)
                            .build();
            forms.add(form);
            tabs.add(Ids.build(entry.getKey(), Ids.TAB), new LabelBuilder().label(entry.getKey()),
                    form.element());
        }

        AddressTemplate template = AddressTemplate.of(
                environment.isStandalone() ? "/subsystem=*" : "/profile=full-ha/subsystem=*");
        for (ModelNodeForm<ModelNode> form : forms) {
            for (FormItem item : form.getBoundFormItems()) {
                if (item.getName().contains("-suggestion")) {
                    item.registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                            template));
                }
            }
            registerAttachable(form);
        }

        HTMLElement layout = row()
                .add(column()
                        .add(h(1).textContent("Under the Bridge"))
                        .add(p().textContent(description.getDescription()))
                        .add(p().innerHtml(
                                new SafeHtmlBuilder().appendEscaped("If you're wondering about the name of " +
                                        "this page, I came up with the idea for this demo while I was listening to ")
                                        .appendHtmlConstant("<a href=\"" + VIDEO + "\" target=\"_blank\">")
                                        .appendEscaped("Under The Bridge")
                                        .appendHtmlConstant("</a> by Red Hot Chili Peppers.")
                                        .toSafeHtml()))
                        .add(tabs))
                .element();
        initElement(layout);
    }

    @Override
    public void setPresenter(UnderTheBridgePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show(ModelNode model) {
        for (ModelNodeForm<ModelNode> form : forms) {
            form.view(model);
        }
    }
}
