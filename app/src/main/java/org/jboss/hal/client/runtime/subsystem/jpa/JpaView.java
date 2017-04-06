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
package org.jboss.hal.client.runtime.subsystem.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.common.collect.LinkedListMultimap;
import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "ResultOfMethodCallIgnored"})
public class JpaView extends HalViewImpl implements JpaPresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";
    private static final String LEAD_ELEMENT = "leadElement";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static LinkedListMultimap<String, String> mainAttributes = LinkedListMultimap.create();

    static {
        mainAttributes.putAll(CONSTANTS.attributes(), asList(
                "hibernate-persistence-unit",
                "enabled",
                "statistics-enabled"
        ));

        mainAttributes.putAll(CONSTANTS.counter(), asList(
                "session-open-count",
                "session-close-count",
                "completed-transaction-count",
                "successful-transaction-count",
                "prepared-statement-count",
                "close-statement-count",
                "flush-count",
                "connect-count",
                "optimistic-failure-count"
        ));

        mainAttributes.putAll(Names.ENTITY, asList(
                "entity-delete-count",
                "entity-fetch-count",
                "entity-insert-count",
                "entity-load-count",
                "entity-update-count"
        ));

        mainAttributes.putAll(Names.CONNECTION, asList(
                "collection-fetch-count",
                "collection-load-count",
                "collection-recreated-count",
                "collection-remove-count",
                "collection-update-count"
        ));

        mainAttributes.putAll(Names.QUERY, asList(
                "query-cache-hit-count",
                "query-cache-miss-count",
                "query-cache-put-count",
                "query-execution-count",
                "query-execution-max-time",
                "query-execution-max-time-query-string"
        ));

        mainAttributes.putAll(Names.SECOND_LEVEL_CACHE, asList(
                "second-level-cache-hit-count",
                "second-level-cache-miss-count",
                "second-level-cache-put-count"
        ));
    }

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final List<Form<JpaStatistic>> mainForms;
    private final Map<String, NamedNodeTable<NamedNode>> childTables;
    private final Map<String, Form<NamedNode>> childForms;
    private final Element headerElement;
    private final Element leadElement;
    private JpaPresenter presenter;

    @Inject
    public JpaView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.mainForms = new ArrayList<>();
        this.childForms = new HashMap<>();
        this.childTables = new HashMap<>();

        // main attributes
        Tabs mainAttributesTabs = new Tabs();
        String baseId = Ids.build(Ids.JPA_RUNTIME);
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JPA_TEMPLATE);

        for (String section : mainAttributes.keySet()) {
            String sectionId = Ids.asId(section);
            List<String> sectionAttributes = mainAttributes.get(section);
            Form<JpaStatistic> form = new ModelNodeForm.Builder<JpaStatistic>(
                    Ids.build(baseId, Ids.FORM_SUFFIX, sectionId), metadata)
                    .readOnly()
                    .includeRuntime()
                    .include(sectionAttributes)
                    .unsorted()
                    .build();
            registerAttachable(form);
            mainForms.add(form);
            mainAttributesTabs.add(Ids.build(baseId, Ids.TAB_SUFFIX, sectionId), section, form.asElement());
        }

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .section()
                .h(1).rememberAs(HEADER_ELEMENT).end()
                .p().css(lead).rememberAs(LEAD_ELEMENT).end()
                .p().css(clearfix)
                    .span().textContent(metadata.getDescription().getDescription()).end()
                    .a().css(clickable, pullRight).on(click, event -> refresh())
                        .span().css(fontAwesome("refresh"), marginRight5).end()
                        .span().textContent(resources.constants().refresh()).end()
                    .end()
                .end()
                .add(mainAttributesTabs)
            .end();
        // @formatter:on

        headerElement = builder.referenceFor(HEADER_ELEMENT);
        leadElement = builder.referenceFor(LEAD_ELEMENT);

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        navigation.addPrimary(Ids.JPA_RUNTIME_MAIN_ATTRIBUTES_ENTRY, resources.constants().mainAttributes(),
                fontAwesome("list-ul"), builder.<Element>build());

        // child resources
        buildChildPanel(baseId, AddressTemplates.ENTITY_TEMPLATE, "entity");
        navigation.addPrimary(Ids.JPA_RUNTIME_ENTITY_ENTRY, Names.ENTITY, fontAwesome("cubes"),
                buildChildPanel(baseId, AddressTemplates.ENTITY_TEMPLATE, Names.ENTITY));
        navigation.addPrimary(Ids.JPA_RUNTIME_ENTITY_CACHE_ENTRY, Names.ENTITY_CACHE, fontAwesome("database"),
                buildChildPanel(baseId, AddressTemplates.ENTITY_CACHE_TEMPLATE, Names.ENTITY_CACHE));
        navigation.addPrimary(Ids.JPA_RUNTIME_QUERY_CACHE_ENTRY, Names.QUERY_CACHE, pfIcon("storage-domain"),
                buildChildPanel(baseId, AddressTemplates.QUERY_CACHE_TEMPLATE, Names.QUERY_CACHE));
        navigation.addPrimary(Ids.JPA_RUNTIME_COLLECTION_ENTRY, Names.COLLECTION, fontAwesome("tasks"),
                buildChildPanel(baseId, AddressTemplates.COLLECTION_TEMPLATE, Names.COLLECTION));

        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();
        Element root = layoutBuilder.build();
        initElement(root);
    }

    private Element buildChildPanel(String baseId, AddressTemplate template, String title) {
        String resource = template.lastKey();
        Metadata metadata = metadataRegistry.lookup(template);

        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .build();
        NamedNodeTable<NamedNode> table = new NamedNodeTable<>(Ids.build(baseId, resource, Ids.TABLE_SUFFIX), metadata,
                options);

        Form<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(Ids.build(baseId, resource, Ids.FORM_SUFFIX),
                metadata)
                .readOnly()
                .includeRuntime()
                .build();

        registerAttachable(table);
        registerAttachable(form);
        childTables.put(resource, table);
        childForms.put(resource, form);

        // @formatter:off
        return new Elements.Builder()
            .section()
                .h(1).textContent(title).end()
                .p().css(clearfix)
                    .span().textContent(metadata.getDescription().getDescription()).end()
                    .a().css(clickable, pullRight).on(click, event -> refresh())
                        .span().css(fontAwesome("refresh"), marginRight5).end()
                        .span().textContent(resources.constants().refresh()).end()
                    .end()
                .end()
                .add(table.asElement())
                .add(form.asElement())
            .end()
        .build();
        // @formatter:on
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
        DataTable<NamedNode> table = childTables.get(resource);
        table.bindForm(childForms.get(resource));
    }

    @Override
    public void setPresenter(final JpaPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final JpaStatistic statistic) {
        headerElement.setTextContent(statistic.getName());
        leadElement.setTextContent(statistic.getDeployment());

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
            NamedNodeTable<NamedNode> table = childTables.get(childResource);
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
