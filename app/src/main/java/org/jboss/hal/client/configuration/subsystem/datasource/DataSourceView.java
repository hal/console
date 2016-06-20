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
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.LinkedListMultimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.BOTH;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.NON_XA;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.XA;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.IdBuilder.asId;

/**
 * TODO Add support for nested 'connection-properties' (non-xa) and 'xa-datasource-properties' (xa)
 *
 * @author Harald Pehl
 */
public class DataSourceView extends PatternFlyViewImpl implements DataSourcePresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static LinkedListMultimap<String, Attribute> attributes = LinkedListMultimap.create();

    static {
        // main attributes
        attributes.putAll(CONSTANTS.attribute(), asList(
                new Attribute(JNDI_NAME),
                new Attribute(DRIVER_NAME),
                new Attribute(ENABLED),
                new Attribute(STATISTICS_ENABLED)
        ));

        // connection
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.connection(), asList(
                new Attribute(CONNECTION_URL, XA),
                new Attribute("url-delimiter"),
                new Attribute("url-selector-strategy-class-name"),
                new Attribute("new-connection-sql"),
                new Attribute("connection-listener-class"),
                new Attribute("connection-listener-property"),
                new Attribute("transaction-isolation"),
                new Attribute("jta", NON_XA),
                new Attribute("use-ccm"),
                new Attribute("same-rm-override", XA),
                new Attribute("interleaving", XA),
                new Attribute("pad-xid", XA),
                new Attribute("wrap-xa-resource", XA)
        ));

        // pool
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.pool(), asList(
                new Attribute("min-pool-size"),
                new Attribute("initial-pool-size"),
                new Attribute("max-pool-size"),
                new Attribute("pool-prefill"),
                new Attribute("flush-strategy"),
                new Attribute("pool-use-strict-min"),
                new Attribute("pool-fair"),
                new Attribute("use-fast-fail"),
                new Attribute("capacity-decrementer-class"),
                new Attribute("capacity-decrementer-properties"),
                new Attribute("capacity-incrementer-class"),
                new Attribute("capacity-incrementer-properties"),
                new Attribute("no-recovery", XA),
                new Attribute("tx-separate-pool", XA)
        ));

        // security
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.security(), asList(
                new Attribute("user-name"),
                new Attribute("password"),
                new Attribute("security-domain"),
                new Attribute("allow-multiple-users"),
                new Attribute("reauth-plugin-class-name"),
                new Attribute("reauth-plugin-properties"),
                new Attribute("recovery-username", XA),
                new Attribute("recovery-password", XA),
                new Attribute("recovery-security-domain", XA),
                new Attribute("recovery-plugin-class-name", XA),
                new Attribute("recovery-plugin-properties", XA)
        ));

        // validation
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.validation(), asList(
                new Attribute("valid-connection-checker-class-name"),
                new Attribute("valid-connection-checker-properties"),
                new Attribute("check-valid-connection-sql"),
                new Attribute("validate-on-match"),
                new Attribute("background-validation"),
                new Attribute("background-validation-millis"),
                new Attribute("stale-connection-checker-class-name"),
                new Attribute("stale-connection-checker-properties"),
                new Attribute("exception-sorter-class-name"),
                new Attribute("exception-sorter-properties")
        ));

        // timeouts
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.timeouts(), asList(
                new Attribute("use-try-lock"),
                new Attribute("blocking-timeout-wait-millis"),
                new Attribute("idle-timeout-minutes"),
                new Attribute("set-tx-query-timeout"),
                new Attribute("query-timeout"),
                new Attribute("allocation-retry"),
                new Attribute("allocation-retry-wait-millis"),
                new Attribute("xa-resource-timeout", XA)
        ));

        // statements / tracking
        //noinspection HardCodedStringLiteral
        attributes.putAll(CONSTANTS.statements() + " / " + CONSTANTS.tracking(), asList(
                new Attribute("spy"),
                new Attribute("tracking"),
                new Attribute("track-statements"),
                new Attribute("share-prepared-statements"),
                new Attribute("prepared-statements-cache-size"),
                new Attribute("enlistment-trace")
        ));
    }

    private final Resources resources;
    private final List<Form<DataSource>> nonXaForms;
    private final List<Form<DataSource>> xaForms;
    private final Element header;
    private final Element nonXaInfo;
    private final Element xaInfo;
    private final Tabs nonXaTabs;
    private final Tabs xaTabs;
    private DataSourcePresenter presenter;

    @Inject
    public DataSourceView(MetadataRegistry metadataRegistry, Resources resources) {
        this.resources = resources;

        Metadata nonXaMeta = metadataRegistry.lookup(DATA_SOURCE_TEMPLATE);
        Metadata xaMeta = metadataRegistry.lookup(XA_DATA_SOURCE_TEMPLATE);
        nonXaInfo = new Elements.Builder().p().textContent(nonXaMeta.getDescription().getDescription()).end()
                .build();
        xaInfo = new Elements.Builder().p().textContent(xaMeta.getDescription().getDescription()).end().build();

        nonXaForms = new ArrayList<>();
        xaForms = new ArrayList<>();
        nonXaTabs = new Tabs();
        xaTabs = new Tabs();
        ModelNodeForm<DataSource> form;
        Form.SaveCallback<DataSource> saveCallback = (f, changedValues) -> presenter.saveDataSource(changedValues);

        for (String section : attributes.keySet()) {
            String sectionId = asId(section);
            List<Attribute> sectionAttributes = attributes.get(section);

            // non xa form and tab
            List<String> nonXaNames = sectionAttributes.stream()
                    .filter(attribute -> attribute.scope == BOTH || attribute.scope == NON_XA)
                    .map(attribute -> attribute.name)
                    .collect(toList());
            form = new ModelNodeForm.Builder<DataSource>(IdBuilder.build(DATA_SOURCE, "form", sectionId), nonXaMeta)
                    .include(nonXaNames)
                    .onSave(saveCallback)
                    .build();
            nonXaForms.add(form);
            nonXaTabs.add(IdBuilder.build(DATA_SOURCE, "tab", sectionId), section, form.asElement());

            // xa form and tab
            List<String> xaNames = sectionAttributes.stream()
                    .filter(attribute -> attribute.scope == BOTH || attribute.scope == XA)
                    .map(attribute -> attribute.name)
                    .collect(toList());
            form = new ModelNodeForm.Builder<DataSource>(IdBuilder.build(XA_DATA_SOURCE, "form", sectionId), xaMeta)
                    .include(xaNames)
                    .onSave(saveCallback)
                    .build();
            xaForms.add(form);
            xaTabs.add(IdBuilder.build(XA_DATA_SOURCE, "tab", sectionId), section, form.asElement());
        }

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.DATASOURCE).rememberAs(HEADER_ELEMENT).end()
                    .add(nonXaInfo)
                    .add(xaInfo)
                    .add(nonXaTabs.asElement())
                    .add(xaTabs.asElement())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        header = layoutBuilder.referenceFor(HEADER_ELEMENT);
        registerAttachables(nonXaForms);
        registerAttachables(xaForms);
        initElement(root);
    }

    @Override
    public void setPresenter(final DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear(boolean xa) {
        showHide(xa);
        if (xa) {
            header.setTextContent(Names.XA_DATASOURCE);
            for (Form<DataSource> form : xaForms) {
                form.clear();
            }
        } else {
            header.setTextContent(Names.DATASOURCE);
            for (Form<DataSource> form : nonXaForms) {
                form.clear();
            }
        }
    }

    @Override
    public void update(final DataSource dataSource) {
        // TODO Add a suggestion handler for the DRIVER_NAME field
        showHide(dataSource.isXa());
        //noinspection HardCodedStringLiteral
        header.setInnerHTML(new SafeHtmlBuilder()
                .appendEscaped(dataSource.getName())
                .appendEscaped(" (")
                .appendHtmlConstant("<small>")
                .appendEscaped(dataSource.isEnabled() ? resources.constants().enabled() : resources.constants().disabled())
                .appendHtmlConstant("</small>")
                .appendEscaped(" )")
                .toSafeHtml().asString());
        if (dataSource.isXa()) {
            for (Form<DataSource> form : xaForms) {
                form.view(dataSource);
            }
        } else {
            for (Form<DataSource> form : nonXaForms) {
                form.view(dataSource);
            }
        }
    }

    private void showHide(boolean xa) {
        Elements.setVisible(nonXaInfo, !xa);
        Elements.setVisible(xaInfo, xa);
        Elements.setVisible(nonXaTabs.asElement(), !xa);
        Elements.setVisible(xaTabs.asElement(), xa);
    }
}
