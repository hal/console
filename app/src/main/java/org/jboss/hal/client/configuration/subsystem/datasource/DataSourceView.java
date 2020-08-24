/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.LinkedListMultimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.elytron.CredentialReference.AlternativeValidation;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.BOTH;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.NON_XA;
import static org.jboss.hal.client.configuration.subsystem.datasource.Attribute.Scope.XA;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DataSourceView extends HalViewImpl implements DataSourcePresenter.MyView {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static LinkedListMultimap<Group, Attribute> attributes = LinkedListMultimap.create();

    static {
        // main attributes
        attributes.putAll(new Group(Ids.ATTRIBUTES, CONSTANTS.attributes()), asList(
                new Attribute("datasource-class", NON_XA),
                new Attribute("driver-class", NON_XA),
                new Attribute(DRIVER_NAME),
                new Attribute(ENABLED),
                new Attribute(JNDI_NAME),
                new Attribute(STATISTICS_ENABLED),
                new Attribute("xa-datasource-class", XA)
        ));

        // connection
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.CONNECTION, CONSTANTS.connection()), asList(
                new Attribute(CONNECTION_URL, NON_XA),
                new Attribute("connection-listener-class"),
                new Attribute("connection-listener-property"),
                new Attribute("interleaving", XA),
                new Attribute("jta", NON_XA),
                new Attribute("new-connection-sql"),
                new Attribute("pad-xid", XA),
                new Attribute("same-rm-override", XA),
                new Attribute("transaction-isolation"),
                new Attribute("url-delimiter"),
                new Attribute("url-property", XA),
                new Attribute("url-selector-strategy-class-name"),
                new Attribute("use-ccm"),
                new Attribute("wrap-xa-resource", XA)
        ));

        // pool
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.POOL, CONSTANTS.pool()), asList(
                new Attribute("initial-pool-size"),
                new Attribute(MIN_POOL_SIZE),
                new Attribute(MAX_POOL_SIZE),
                new Attribute("flush-strategy"),
                new Attribute("pool-fair"),
                new Attribute("pool-prefill"),
                new Attribute("pool-use-strict-min"),
                new Attribute("use-fast-fail"),
                new Attribute("capacity-incrementer-class"),
                new Attribute("capacity-incrementer-properties"),
                new Attribute("capacity-decrementer-class"),
                new Attribute("capacity-decrementer-properties"),
                new Attribute("no-recovery", XA),
                new Attribute("tx-separate-pool", XA)
        ));

        // security
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.SECURITY, CONSTANTS.security()), asList(
                new Attribute("user-name"),
                new Attribute("password"),
                new Attribute("authentication-context"),
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

        // credential reference is added as a marker key and not part of the grouped form!
        attributes.put(new Group(Ids.CREDENTIAL_REFERENCE, CREDENTIAL_REFERENCE), null);

        // validation
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.VALIDATION, CONSTANTS.validation()), asList(
                new Attribute("check-valid-connection-sql"),
                new Attribute("valid-connection-checker-class-name"),
                new Attribute("valid-connection-checker-properties"),
                new Attribute("validate-on-match"),
                new Attribute("background-validation"),
                new Attribute("background-validation-millis"),
                new Attribute("stale-connection-checker-class-name"),
                new Attribute("stale-connection-checker-properties"),
                new Attribute("exception-sorter-class-name"),
                new Attribute("exception-sorter-properties")
        ));

        // timeouts
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.TIMEOUT, CONSTANTS.timeouts()), asList(
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
        //noinspection HardCodedStringLiteral,DuplicateStringLiteralInspection
        attributes.putAll(new Group(Ids.STATEMENTS, CONSTANTS.statements() + " / " + CONSTANTS.tracking()), asList(
                new Attribute("spy"),
                new Attribute("tracking"),
                new Attribute("track-statements"),
                new Attribute("share-prepared-statements"),
                new Attribute("prepared-statements-cache-size"),
                new Attribute("enlistment-trace")
        ));
    }

    private final Resources resources;
    private final HTMLElement header;
    private final HTMLElement nonXaInfo;
    private final HTMLElement xaInfo;
    private Form<DataSource> nonXaForm;
    private Form<ModelNode> nonXaCrForm;
    private Form<DataSource> xaForm;
    private Form<ModelNode> xaCrForm;
    private DataSourcePresenter presenter;
    private Map<String, String> originalConnectionProperties;
    private StaticAutoComplete xaAutoCompleteValues = new StaticAutoComplete(Collections.emptyList());
    private StaticAutoComplete nonXaAutoCompleteValues = new StaticAutoComplete(Collections.emptyList());
    @Inject
    public DataSourceView(MetadataRegistry metadataRegistry, CredentialReference credentialReference,
                          Resources resources) {
        this.resources = resources;

        Form.SaveCallback<DataSource> saveCallback = (f, changedValues) -> presenter.saveDataSource(f, changedValues,
                originalConnectionProperties);
        Form.PrepareReset<DataSource> prepareReset = (f) -> presenter.resetDataSource(f);

        Metadata nonXaMeta = metadataRegistry.lookup(DATA_SOURCE_TEMPLATE);
        nonXaInfo = p().textContent(nonXaMeta.getDescription().getDescription()).element();
        GroupedForm.Builder<DataSource> nonXaFormBuilder = new GroupedForm.Builder<DataSource>(Ids.DATA_SOURCE_FORM,
                nonXaMeta)
                .onSave(saveCallback)
                .prepareReset(prepareReset);

        Metadata xaMeta = metadataRegistry.lookup(XA_DATA_SOURCE_TEMPLATE);
        xaInfo = p().textContent(xaMeta.getDescription().getDescription()).element();
        GroupedForm.Builder<DataSource> xaFormBuilder = new GroupedForm.Builder<DataSource>(Ids.XA_DATA_SOURCE_FORM,
                xaMeta).onSave(saveCallback);

        for (Group group : attributes.keySet()) {

            String nonXaId = Ids.build(Ids.DATA_SOURCE_CONFIGURATION, group.id);
            String xaId = Ids.build(Ids.XA_DATA_SOURCE, group.id);

            if (Ids.CREDENTIAL_REFERENCE.equals(group.id)) {
                nonXaCrForm = credentialReference.form(Ids.DATA_SOURCE_CONFIGURATION, nonXaMeta, PASSWORD,
                        () -> nonXaForm.<String>getFormItem(PASSWORD).getValue(),
                        () -> presenter.resourceAddress(),
                        () -> presenter.reload());
                registerAttachable(nonXaCrForm);
                nonXaFormBuilder.customGroup(nonXaId, Names.CREDENTIAL_REFERENCE)
                        .add(nonXaCrForm)
                        .end();

                xaCrForm = credentialReference.form(Ids.XA_DATA_SOURCE, xaMeta, PASSWORD,
                        () -> xaForm.<String>getFormItem(PASSWORD).getValue(),
                        () -> presenter.resourceAddress(),
                        () -> presenter.reload());
                registerAttachable(xaCrForm);
                xaFormBuilder.customGroup(xaId, Names.CREDENTIAL_REFERENCE)
                        .add(xaCrForm)
                        .end();

            } else {
                List<Attribute> groupAttributes = attributes.get(group);

                // non xa form and tab
                List<String> nonXaNames = groupAttributes.stream()
                        .filter(attribute -> attribute.scope == BOTH || attribute.scope == NON_XA)
                        .map(attribute -> attribute.name)
                        .collect(toList());
                nonXaFormBuilder.customGroup(nonXaId, group.title)
                        .include(nonXaNames);

                // xa form and tab
                List<String> xaNames = groupAttributes.stream()
                        .filter(attribute -> attribute.scope == BOTH || attribute.scope == XA)
                        .map(attribute -> attribute.name)
                        .collect(toList());
                xaFormBuilder.customGroup(xaId, group.title)
                        .include(xaNames);

                if (Ids.CONNECTION.equals(group.id)) {
                    nonXaFormBuilder.unboundFormItem(new PropertiesItem(CONNECTION_PROPERTIES));
                    xaFormBuilder.unboundFormItem(new PropertiesItem(XA_DATASOURCE_PROPERTIES));
                }
                nonXaFormBuilder.end();
                xaFormBuilder.end();
            }
        }

        nonXaForm = nonXaFormBuilder.build();
        nonXaForm.addFormValidation(new AlternativeValidation<>(PASSWORD, () -> nonXaCrForm.getModel(), resources));
        xaForm = xaFormBuilder.build();
        xaForm.getFormItem(XA_DATASOURCE_PROPERTIES).registerSuggestHandler(xaAutoCompleteValues);
        nonXaForm.getFormItem(CONNECTION_PROPERTIES).registerSuggestHandler(nonXaAutoCompleteValues);
        xaForm.addFormValidation(new AlternativeValidation<>(PASSWORD, () -> xaCrForm.getModel(), resources));
        registerAttachable(nonXaForm);
        registerAttachable(xaForm);

        initElement(row()
                .add(column()
                        .add(header = h(1).textContent(Names.DATASOURCE).element())
                        .add(nonXaInfo)
                        .add(xaInfo)
                        .add(nonXaForm)
                        .add(xaForm)).element());
    }

    @Override
    public void setPresenter(DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear(boolean xa) {
        showHide(xa);
        if (xa) {
            header.textContent = Names.XA_DATASOURCE;
            xaForm.clear();
            if (xaCrForm != null) {
                xaCrForm.clear();
            }
        } else {
            header.textContent = Names.DATASOURCE;
            nonXaForm.clear();
            if (nonXaCrForm != null) {
                nonXaCrForm.clear();
            }
        }
    }

    @Override
    public void update(DataSource dataSource) {
        // TODO Add a suggestion handler for the DRIVER_NAME field
        showHide(dataSource.isXa());
        //noinspection HardCodedStringLiteral
        header.innerHTML = new SafeHtmlBuilder()
                .appendEscaped(dataSource.getName())
                .appendHtmlConstant("<small>")
                .appendEscaped(" (")
                .appendEscaped(
                        dataSource.isEnabled() ? resources.constants().enabled() : resources.constants().disabled())
                .appendEscaped(")")
                .appendHtmlConstant("</small>")
                .toSafeHtml().asString();
        if (dataSource.isXa()) {
            xaForm.view(dataSource);
            Map<String, String> p = failSafePropertyList(dataSource, XA_DATASOURCE_PROPERTIES).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            originalConnectionProperties = p;
            xaForm.getFormItem(XA_DATASOURCE_PROPERTIES).setValue(p);
            String dsClassname = dataSource.hasDefined(XA_DATASOURCE_CLASS) ? dataSource.get(XA_DATASOURCE_CLASS)
                    .asString() : null;
            String driverName = dataSource.get(DRIVER_NAME).asString();
            presenter.readJdbcDriverProperties(true, dsClassname, driverName,
                    dsProperties -> xaAutoCompleteValues.update(dsProperties));
            if (xaCrForm != null) {
                xaCrForm.view(failSafeGet(dataSource, CREDENTIAL_REFERENCE));
            }
        } else {
            nonXaForm.view(dataSource);
            Map<String, String> p = failSafePropertyList(dataSource, CONNECTION_PROPERTIES).stream()
                    .collect(toMap(Property::getName, property -> property.getValue().get(VALUE).asString()));
            originalConnectionProperties = p;
            nonXaForm.getFormItem(CONNECTION_PROPERTIES).setValue(p);
            String dsClassname = dataSource.hasDefined(DATASOURCE_CLASS) ? dataSource.get(DATASOURCE_CLASS)
                    .asString() : null;
            String driverName = dataSource.get(DRIVER_NAME).asString();
            presenter.readJdbcDriverProperties(false, dsClassname, driverName,
                    dsProperties -> nonXaAutoCompleteValues.update(dsProperties));

            if (nonXaCrForm != null) {
                nonXaCrForm.view(failSafeGet(dataSource, CREDENTIAL_REFERENCE));
            }
        }
    }

    private void showHide(boolean xa) {
        Elements.setVisible(nonXaInfo, !xa);
        Elements.setVisible(xaInfo, xa);
        Elements.setVisible(nonXaForm.element(), !xa);
        Elements.setVisible(xaForm.element(), xa);
    }

    private static class Group {
        String id;
        String title;

        private Group(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
