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
package org.jboss.hal.client.runtime.subsystem.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.LinkedListMultimap;
import com.google.gwt.core.client.GWT;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.*;

@SuppressWarnings({ "HardCodedStringLiteral", "ResultOfMethodCallIgnored" })
public class JpaView extends HalViewImpl implements JpaPresenter.MyView {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static LinkedListMultimap<String, String> mainAttributes = LinkedListMultimap.create();

    static {
        mainAttributes.putAll(CONSTANTS.attributes(), asList(
                "hibernate-persistence-unit",
                "enabled",
                "statistics-enabled"));

        mainAttributes.putAll(CONSTANTS.counter(), asList(
                "session-open-count",
                "session-close-count",
                "completed-transaction-count",
                "successful-transaction-count",
                "prepared-statement-count",
                "close-statement-count",
                "flush-count",
                "connect-count",
                "optimistic-failure-count"));

        mainAttributes.putAll(Names.ENTITY, asList(
                "entity-delete-count",
                "entity-fetch-count",
                "entity-insert-count",
                "entity-load-count",
                "entity-update-count"));

        mainAttributes.putAll(Names.CONNECTION, asList(
                "collection-fetch-count",
                "collection-load-count",
                "collection-recreated-count",
                "collection-remove-count",
                "collection-update-count"));

        mainAttributes.putAll(Names.QUERY, asList(
                "query-cache-hit-count",
                "query-cache-miss-count",
                "query-cache-put-count",
                "query-execution-count",
                "query-execution-max-time",
                "query-execution-max-time-query-string"));

        mainAttributes.putAll(Names.SECOND_LEVEL_CACHE, asList(
                "second-level-cache-hit-count",
                "second-level-cache-miss-count",
                "second-level-cache-put-count"));
    }

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final List<Form<JpaStatistic>> mainForms;
    private final Map<String, Table<NamedNode>> childTables;
    private final Map<String, Form<NamedNode>> childForms;
    private final HTMLElement headerElement;
    private final HTMLElement leadElement;
    private JpaPresenter presenter;

    @Inject
    public JpaView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.mainForms = new ArrayList<>();
        this.childForms = new HashMap<>();
        this.childTables = new HashMap<>();

        // main attributes
        Tabs mainAttributesTabs = new Tabs(Ids.JPA_RUNTIME_TAB_CONTAINER);
        String baseId = Ids.build(Ids.JPA_RUNTIME);
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JPA_DEPLOYMENT_TEMPLATE);

        for (String section : mainAttributes.keySet()) {
            String sectionId = Ids.asId(section);
            List<String> sectionAttributes = mainAttributes.get(section);
            Form<JpaStatistic> form = new ModelNodeForm.Builder<JpaStatistic>(
                    Ids.build(baseId, Ids.FORM, sectionId), metadata)
                    .readOnly()
                    .includeRuntime()
                    .include(sectionAttributes)
                    .unsorted()
                    .build();
            registerAttachable(form);
            mainForms.add(form);
            mainAttributesTabs.add(Ids.build(baseId, Ids.TAB, sectionId), section, form.element());
        }

        HTMLElement section = section()
                .add(headerElement = h(1).element())
                .add(leadElement = p().css(lead).element())
                .add(p().css(clearfix)
                        .add(span().textContent(metadata.getDescription().getDescription())
                                .add(a().css(clickable, pullRight).on(click, event -> refresh())
                                        .add(span().css(fontAwesome("refresh"), marginRight5))
                                        .add(span().textContent(resources.constants().refresh())))))
                .add(mainAttributesTabs).element();

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        navigation.addPrimary(Ids.JPA_RUNTIME_MAIN_ATTRIBUTES_ITEM, resources.constants().mainAttributes(),
                fontAwesome("list-ul"), section);

        // child resources
        buildChildPanel(baseId, AddressTemplates.ENTITY_DEPLOYMENT_TEMPLATE, "entity");
        navigation.addPrimary(Ids.JPA_RUNTIME_ENTITY_ITEM, Names.ENTITY, fontAwesome("cubes"),
                buildChildPanel(baseId, AddressTemplates.ENTITY_DEPLOYMENT_TEMPLATE, Names.ENTITY));
        navigation.addPrimary(Ids.JPA_RUNTIME_ENTITY_CACHE_ITEM, Names.ENTITY_CACHE, fontAwesome("database"),
                buildChildPanel(baseId, AddressTemplates.ENTITY_CACHE_DEPLOYMENT_TEMPLATE, Names.ENTITY_CACHE));
        navigation.addPrimary(Ids.JPA_RUNTIME_QUERY_CACHE_ITEM, Names.QUERY_CACHE, pfIcon("storage-domain"),
                buildChildPanel(baseId, AddressTemplates.QUERY_CACHE_DEPLOYMENT_TEMPLATE, Names.QUERY_CACHE));
        navigation.addPrimary(Ids.JPA_RUNTIME_COLLECTION_ITEM, Names.COLLECTION, fontAwesome("tasks"),
                buildChildPanel(baseId, AddressTemplates.COLLECTION_DEPLOYMENT_TEMPLATE, Names.COLLECTION));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private HTMLElement buildChildPanel(String baseId, AddressTemplate template, String title) {
        String resource = template.lastName();
        Metadata metadata = metadataRegistry.lookup(template);

        Table<NamedNode> table = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(baseId, resource, Ids.TABLE), metadata)
                .nameColumn()
                .build();

        Form<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, resource, Ids.FORM),
                metadata)
                .readOnly()
                .includeRuntime()
                .build();

        registerAttachable(table);
        registerAttachable(form);
        childTables.put(resource, table);
        childForms.put(resource, form);

        return section()
                .add(h(1).textContent(title))
                .add(p().css(clearfix)
                        .add(span().textContent(metadata.getDescription().getDescription()))
                        .add(a().css(clickable, pullRight).on(click, event -> refresh())
                                .add(span().css(fontAwesome("refresh"), marginRight5))
                                .add(span().textContent(resources.constants().refresh()))))
                .add(table.element())
                .add(form.element()).element();
    }

    @Override
    public void attach() {
        super.attach();
        bindFormToTable("entity");
        bindFormToTable("entity-cache");
        bindFormToTable("query-cache");
        bindFormToTable("collection");
    }

    private void bindFormToTable(String resource) {
        Table<NamedNode> table = childTables.get(resource);
        table.bindForm(childForms.get(resource));
    }

    @Override
    public void setPresenter(JpaPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(JpaStatistic statistic) {
        headerElement.textContent = statistic.getPersistenceUnit();
        leadElement.textContent = statistic.getPath();

        mainForms.forEach(form -> form.view(statistic));

        updateChildResource(statistic, "entity");
        updateChildResource(statistic, "entity-cache");
        updateChildResource(statistic, "query-cache");
        updateChildResource(statistic, "collection");
    }

    private void updateChildResource(JpaStatistic statistic, String childResource) {
        if (statistic.hasDefined(childResource)) {
            List<NamedNode> childResources = asNamedNodes(statistic.get(childResource).asPropertyList());
            Form<NamedNode> form = childForms.get(childResource);
            Table<NamedNode> table = childTables.get(childResource);
            form.clear();
            table.update(childResources);
        }
    }

    private void refresh() {
        if (presenter != null) {
            presenter.reload();
        }
    }
}
