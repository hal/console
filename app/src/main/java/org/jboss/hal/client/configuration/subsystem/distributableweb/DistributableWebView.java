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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.selectpicker;

@MbuiView
@SuppressWarnings("WeakerAccess")
public abstract class DistributableWebView extends MbuiViewImpl<DistributableWebPresenter>
        implements DistributableWebPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static DistributableWebView create(MbuiContext mbuiContext) {
        return new Mbui_DistributableWebView(mbuiContext);
    }

    @MbuiElement("dw-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("dw-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("dw-hotrod-session-management-table") Table<NamedNode> hotRodSessionManagementTable;
    @MbuiElement("dw-hotrod-session-management-form") Form<NamedNode> hotRodSessionManagementForm;
    @MbuiElement("dw-hotrod-sso-management-table") Table<NamedNode> hotRodSSOManagementTable;
    @MbuiElement("dw-hotrod-sso-management-form") Form<NamedNode> hotRodSSOManagementForm;
    @MbuiElement("dw-infinispan-session-management-table") Table<NamedNode> infinispanSessionManagementTable;
    @MbuiElement("dw-infinispan-session-management-form") Form<NamedNode> infinispanSessionManagementForm;
    @MbuiElement("dw-infinispan-sso-management-table") Table<NamedNode> infinispanSSOManagementTable;
    @MbuiElement("dw-infinispan-sso-management-form") Form<NamedNode> infinispanSSOManagementForm;
    private HTMLElement currentRouting;
    private HTMLSelectElement selectRouting;
    private Map<Routing, Form<ModelNode>> routingForms;

    DistributableWebView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        selectRouting = select().css(selectpicker)
                .id(Ids.DISTRIBUTABLE_WEB_ROUTING_SELECT)
                .apply(s -> {
                    s.multiple = false;
                    s.size = 1;
                })
                .on(EventType.change, event -> {
                    String value = ((HTMLSelectElement) event.currentTarget).value;
                    Routing routing = Routing.fromResource(value);
                    if (routing != null) {
                        presenter.switchRouting(routing);
                    }
                })
                .get();
        for (Routing routing : Routing.values()) {
            selectRouting.appendChild(Elements.option()
                    .apply(o -> {
                        o.value = routing.resource;
                        o.text = routing.type;
                    })
                    .get());
        }

        routingForms = new HashMap<>();
        for (Routing routing : Routing.values()) {
            Metadata metadata = mbuiContext.metadataRegistry().lookup(routing.template());
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(routing.baseId, Ids.FORM), metadata)
                    .onSave((f, changedValues) -> presenter.saveRouting(routing, changedValues))
                    .prepareReset(f -> presenter.resetRouting(routing, f))
                    .build();
            setVisible(form.element(), false);
            routingForms.put(routing, form);
        }

        HTMLElement section = section()
                .add(div().css(CSS.headerForm)
                        .add(label()
                                .apply(l -> l.htmlFor = Ids.DISTRIBUTABLE_WEB_ROUTING_SELECT)
                                .textContent(mbuiContext.resources().constants().switchRouting()))
                        .add(selectRouting)
                        .get())
                .add(h(1).textContent(Names.ROUTING)
                        .add(currentRouting = span().get()))
                .addAll(routingForms.values().stream().map(Form::element).collect(toList()))
                .get();

        navigation.insertPrimary(Ids.DISTRIBUTABLE_WEB_ROUTING_ITEM, null, Names.ROUTING,
                pfIcon("route"), section);
    }

    @Override
    public void attach() {
        super.attach();
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $("#" + Ids.DISTRIBUTABLE_WEB_ROUTING_SELECT).selectpicker(options);
        routingForms.values().forEach(Attachable::attach);
    }

    @Override
    public void detach() {
        super.detach();
        routingForms.values().forEach(Attachable::detach);
    }


    // ------------------------------------------------------ update

    @Override
    public void updateConfiguration(ModelNode node) {
        configurationForm.view(node);
    }

    @Override
    public void updateRouting(Routing routing, ModelNode node) {
        if (routing != null) {
            currentRouting.textContent = ": " + routing.type;
            SelectBoxBridge.Single.element(selectRouting).setValue(routing.resource);

            for (Map.Entry<Routing, Form<ModelNode>> entry : routingForms.entrySet()) {
                boolean active = routing == entry.getKey();
                if (active) {
                    entry.getValue().view(node);
                }
                setVisible(entry.getValue().element(), active);
            }
        }
    }

    @Override
    public void updateHotRodSessionManagement(List<NamedNode> nodes) {
        hotRodSessionManagementForm.clear();
        hotRodSessionManagementTable.update(nodes);
    }

    @Override
    public void updateHotRodSSOManagement(List<NamedNode> nodes) {
        hotRodSSOManagementForm.clear();
        hotRodSSOManagementTable.update(nodes);
    }

    @Override
    public void updateInfinispanSessionManagement(List<NamedNode> nodes) {
        infinispanSessionManagementForm.clear();
        infinispanSessionManagementTable.update(nodes);
    }

    @Override
    public void updateInfinispanSSOManagement(List<NamedNode> nodes) {
        infinispanSSOManagementForm.clear();
        infinispanSSOManagementTable.update(nodes);
    }
}