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
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.LinkedListMultimap;
import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "ResultOfMethodCallIgnored"})
public class JpaView extends PatternFlyViewImpl implements JpaPresenter.MyView {

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

        mainAttributes.putAll(CONSTANTS.entity(), asList(
                "entity-delete-count",
                "entity-fetch-count",
                "entity-insert-count",
                "entity-load-count",
                "entity-update-count"
        ));

        mainAttributes.putAll(CONSTANTS.connection(), asList(
                "collection-fetch-count",
                "collection-load-count",
                "collection-recreated-count",
                "collection-remove-count",
                "collection-update-count"
        ));

        mainAttributes.putAll(CONSTANTS.query(), asList(
                "query-cache-hit-count",
                "query-cache-miss-count",
                "query-cache-put-count",
                "query-execution-count",
                "query-execution-max-time",
                "query-execution-max-time-query-string"
        ));

        mainAttributes.putAll(CONSTANTS.secondLevelCache(), asList(
                "second-level-cache-hit-count",
                "second-level-cache-miss-count",
                "second-level-cache-put-count"
        ));
    }

    private final VerticalNavigation navigation;
    private final List<Form<JpaStatistic>> forms;
    private JpaPresenter presenter;

    @Inject
    public JpaView(MetadataRegistry metadataRegistry, Resources resources) {
        forms = new ArrayList<>();
        ModelNodeForm<JpaStatistic> form;
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JPA_TEMPLATE);

        // main attributes
        Tabs mainAttributesTabs = new Tabs();
        for (String section : mainAttributes.keySet()) {
            String sectionId = Ids.asId(section);
            List<String> sectionAttributes = mainAttributes.get(section);
            form = new ModelNodeForm.Builder<JpaStatistic>(Ids.build(Ids.JPA_RUNTIME, "form", sectionId), metadata)
                    .viewOnly()
                    .includeRuntime()
                    .include(sectionAttributes)
                    .unsorted()
                    .build();
            forms.add(form);
            mainAttributesTabs.add(Ids.build(Ids.JPA_RUNTIME, "tab", sectionId), section, form.asElement());
        }
        Element mainAttributesSection = new Elements.Builder()
                .section()
                .h(1).textContent(resources.constants().mainAttributes()).end()
                .p().textContent(metadata.getDescription().getDescription()).end()
                .add(mainAttributesTabs)
                .end()
                .build();

        // entities
        

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.build(Ids.JPA_RUNTIME, "main"), resources.constants().mainAttributes(),
                fontAwesome("list-ul"), mainAttributesSection);

        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();
        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    public void setPresenter(final JpaPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public VerticalNavigation getVerticalNavigation() {
        return navigation;
    }

    @Override
    public void update(final JpaStatistic statistic) {
        forms.forEach(form -> form.view(statistic));
    }
}
