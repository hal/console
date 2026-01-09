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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.elemento.EventType;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.option;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.Elements.select;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AFFINITY;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.selectpicker;
import static org.jboss.hal.resources.Ids.DISTRIBUTABLE_WEB_ROUTING_SELECT;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.ITEM;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.resources.Ids.build;

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
    private Table<NamedNode> hotRodSessionManagementTable;
    private Form<NamedNode> hotRodSessionManagementForm;
    private AffinityElement hotRodSessionManagementAffinityElement;
    @MbuiElement("dw-hotrod-sso-management-table") Table<NamedNode> hotRodSSOManagementTable;
    @MbuiElement("dw-hotrod-sso-management-form") Form<NamedNode> hotRodSSOManagementForm;
    private Table<NamedNode> infinispanSessionManagementTable;
    private Form<NamedNode> infinispanSessionManagementForm;
    private AffinityElement infinispanSessionManagementAffinityElement;
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
                .id(DISTRIBUTABLE_WEB_ROUTING_SELECT)
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
                }).element();
        for (Routing routing : Routing.values()) {
            selectRouting.appendChild(option()
                    .apply(o -> {
                        o.value = routing.resource;
                        o.text = routing.type;
                    }).element());
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
                                .apply(l -> l.htmlFor = DISTRIBUTABLE_WEB_ROUTING_SELECT)
                                .textContent(mbuiContext.resources().constants().switchRouting()))
                        .add(selectRouting).element())
                .add(h(1).textContent(Names.ROUTING)
                        .add(currentRouting = span().element()))
                .addAll(routingForms.values().stream().map(Form::element).collect(toList())).element();

        navigation.insertPrimary(Ids.DISTRIBUTABLE_WEB_ROUTING_ITEM, null, Names.ROUTING,
                pfIcon("route"), section);

        // -- hotrod session management
        String hotRodId = "dw-hotrod-session-management";
        String hotRodNavLabel = SessionManagement.HOTROD.type.substring(0, SessionManagement.HOTROD.type.lastIndexOf(' ')); // remove
                                                                                                                            // "
                                                                                                                            // Management";

        hotRodSessionManagementForm = createMgmtForm(hotRodId, SessionManagement.HOTROD);
        Metadata metadata = mbuiContext.metadataRegistry().lookup(SessionManagement.HOTROD.template);
        hotRodSessionManagementTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(hotRodId, Ids.TABLE), metadata)
                .button(mbuiContext.tableButtonFactory().add(
                        Ids.build(hotRodId, Ids.TABLE, Ids.ADD),
                        SessionManagement.HOTROD.type,
                        SessionManagement.HOTROD.template,
                        (name, address) -> presenter.reload()))
                .button(mbuiContext.tableButtonFactory().remove(
                        SessionManagement.HOTROD.type,
                        SessionManagement.HOTROD.template,
                        table -> table.selectedRow().getName(),
                        () -> presenter.reload()))
                .nameColumn()
                .build();
        hotRodSessionManagementAffinityElement = new AffinityElement(SessionManagement.HOTROD, mbuiContext.metadataRegistry(),
                mbuiContext.resources());

        registerAttachable(hotRodSessionManagementAffinityElement);

        HTMLElement hotRodSection = createMgmtSection(hotRodId, hotRodSessionManagementForm.element(),
                hotRodSessionManagementAffinityElement.element(),
                hotRodSessionManagementTable, SessionManagement.HOTROD);

        navigation.insertPrimary(build(hotRodId, ITEM), build("dw-hotrod-sso-management", ITEM), hotRodNavLabel,
                "pficon pficon-users", hotRodSection);

        registerAttachable(hotRodSessionManagementTable, hotRodSessionManagementForm);

        // -- infinispan session management
        String infinispanId = "dw-infinispan-session-management";
        String infinispanNavLabel = SessionManagement.INFINISPAN.type.substring(0,
                SessionManagement.INFINISPAN.type.lastIndexOf(' '));

        infinispanSessionManagementForm = createMgmtForm(infinispanId, SessionManagement.INFINISPAN);
        Metadata metadata1 = mbuiContext.metadataRegistry().lookup(SessionManagement.INFINISPAN.template);
        infinispanSessionManagementTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(infinispanId, Ids.TABLE), metadata1)
                .button(mbuiContext.tableButtonFactory().add(
                        SessionManagement.INFINISPAN.template,
                        table -> presenter.addInfinispanSessionManagement(infinispanId, metadata1)))
                .button(mbuiContext.tableButtonFactory().remove(
                        SessionManagement.INFINISPAN.type,
                        SessionManagement.INFINISPAN.template,
                        table -> table.selectedRow().getName(),
                        () -> presenter.reload()))
                .nameColumn()
                .build();
        infinispanSessionManagementAffinityElement = new AffinityElement(SessionManagement.INFINISPAN,
                mbuiContext.metadataRegistry(), mbuiContext.resources());

        registerAttachable(infinispanSessionManagementAffinityElement);

        HTMLElement infinispanSection = createMgmtSection(infinispanId, infinispanSessionManagementForm.element(),
                infinispanSessionManagementAffinityElement.element(),
                infinispanSessionManagementTable, SessionManagement.INFINISPAN);

        navigation.insertPrimary(build(infinispanId, ITEM), build("dw-infinispan-sso-management", ITEM), infinispanNavLabel,
                "pficon pficon-users", infinispanSection);

        registerAttachable(infinispanSessionManagementTable, infinispanSessionManagementForm);
    }

    @Override
    public void attach() {
        super.attach();
        SelectBoxBridge.Options options = SelectBoxBridge.Defaults.get();
        $("#" + Ids.DISTRIBUTABLE_WEB_ROUTING_SELECT).selectpicker(options);
        routingForms.values().forEach(Attachable::attach);

        hotRodSessionManagementTable.bindForm(hotRodSessionManagementForm);
        hotRodSessionManagementTable.onSelectionChange((table -> {
            NamedNode row = table.selectedRow();
            String mgtmName = row != null ? row.getName() : null;
            List<Property> affinities = row != null ? ModelNodeHelper.failSafePropertyList(table.selectedRow(), AFFINITY)
                    : Collections.emptyList();
            hotRodSessionManagementAffinityElement.update(mgtmName, affinities);
        }));

        infinispanSessionManagementTable.bindForm(infinispanSessionManagementForm);
        infinispanSessionManagementTable.onSelectionChange((table -> {
            NamedNode row = table.selectedRow();
            String mgtmName = row != null ? row.getName() : null;
            List<Property> affinities = row != null ? ModelNodeHelper.failSafePropertyList(table.selectedRow(), AFFINITY)
                    : Collections.emptyList();
            infinispanSessionManagementAffinityElement.update(mgtmName, affinities);
        }));
    }

    @Override
    public void detach() {
        super.detach();
        routingForms.values().forEach(Attachable::detach);
    }

    @Override
    public void setPresenter(DistributableWebPresenter presenter) {
        this.presenter = presenter;
        this.hotRodSessionManagementAffinityElement.setPresenter(presenter);
        this.infinispanSessionManagementAffinityElement.setPresenter(presenter);
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

    private Form<NamedNode> createMgmtForm(String mgmtId, SessionManagement sessionManagement) {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(sessionManagement.template);
        return new ModelNodeForm.Builder<NamedNode>(build(mgmtId, FORM), metadata)
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    saveForm(sessionManagement.type, name, sessionManagement.template.resolve(statementContext(), name),
                            changedValues, metadata);
                })
                .prepareReset(form -> {
                    String name = form.getModel().getName();
                    resetForm(sessionManagement.type, name, sessionManagement.template.resolve(statementContext(), name), form,
                            metadata);
                })
                .build();
    }

    private HTMLElement createMgmtSection(String mgmtId, HTMLElement formElement, HTMLElement affinityElement,
            Table<NamedNode> table, SessionManagement sessionManagement) {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(sessionManagement.template);
        Tabs tabs = new Tabs(build(mgmtId, "container"));
        tabs.add(build(mgmtId, TAB), mbuiContext.resources().constants().attributes(), formElement);

        tabs.add(build(mgmtId, AFFINITY, TAB), Names.AFFINITY, affinityElement);

        return section()
                .add(h(1).textContent(sessionManagement.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(tabs).element();
    }
}
