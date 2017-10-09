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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.ResponseHeader;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused", "WeakerAccess"})
public abstract class FilterView extends MbuiViewImpl<FilterPresenter>
        implements FilterPresenter.MyView {

    public static FilterView create(final MbuiContext mbuiContext) {
        return new Mbui_FilterView(mbuiContext);
    }

    @MbuiElement("undertow-filter-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("undertow-custom-filter-table") Table<NamedNode> customFilterTable;
    @MbuiElement("undertow-custom-filter-form") Form<NamedNode> customFilterForm;
    @MbuiElement("undertow-error-page-table") Table<NamedNode> errorPageTable;
    @MbuiElement("undertow-error-page-form") Form<NamedNode> errorPageForm;
    @MbuiElement("undertow-expression-filter-table") Table<NamedNode> expressionFilterTable;
    @MbuiElement("undertow-expression-filter-form") Form<NamedNode> expressionFilterForm;
    @MbuiElement("undertow-gzip-table") Table<NamedNode> gzipTable;
    @MbuiElement("undertow-gzip-form") Form<NamedNode> gzipForm;
    @MbuiElement("undertow-mod-cluster-table") Table<NamedNode> modClusterTable;
    @MbuiElement("undertow-mod-cluster-form") Form<NamedNode> modClusterForm;
    @MbuiElement("undertow-request-limit-table") Table<NamedNode> requestLimitTable;
    @MbuiElement("undertow-request-limit-form") Form<NamedNode> requestLimitForm;
    @MbuiElement("undertow-response-header-table") Table<NamedNode> responseHeaderTable;
    @MbuiElement("undertow-response-header-form") Form<NamedNode> responseHeaderForm;
    @MbuiElement("undertow-rewrite-table") Table<NamedNode> rewriteTable;
    @MbuiElement("undertow-rewrite-form") Form<NamedNode> rewriteForm;

    FilterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        List<String> responseHeader = Arrays.stream(ResponseHeader.values())
                .map(ResponseHeader::header)
                .collect(toList());
        responseHeaderForm.getFormItem(HEADER_NAME).registerSuggestHandler(new StaticAutoComplete(responseHeader));
        clearHostFields();
    }

    private void clearHostFields() {
        customFilterForm.getFormItem(HOSTS).setEnabled(false);
        errorPageForm.getFormItem(HOSTS).setEnabled(false);
        expressionFilterForm.getFormItem(HOSTS).setEnabled(false);
        gzipForm.getFormItem(HOSTS).setEnabled(false);
        modClusterForm.getFormItem(HOSTS).setEnabled(false);
        requestLimitForm.getFormItem(HOSTS).setEnabled(false);
        responseHeaderForm.getFormItem(HOSTS).setEnabled(false);
        rewriteForm.getFormItem(HOSTS).setEnabled(false);
    }

    @Override
    public void attach() {
        super.attach();
        customFilterTable.onSelectionChange(t -> updateHostRefs(t, customFilterForm));
        errorPageTable.onSelectionChange(t -> updateHostRefs(t, errorPageForm));
        expressionFilterTable.onSelectionChange(t -> updateHostRefs(t, expressionFilterForm));
        gzipTable.onSelectionChange(t -> updateHostRefs(t, gzipForm));
        modClusterTable.onSelectionChange(t -> updateHostRefs(t, modClusterForm));
        requestLimitTable.onSelectionChange(t -> updateHostRefs(t, requestLimitForm));
        responseHeaderTable.onSelectionChange(t -> updateHostRefs(t, responseHeaderForm));
        rewriteTable.onSelectionChange(t -> updateHostRefs(t, rewriteForm));
    }

    @SuppressWarnings("ConstantConditions")
    private void updateHostRefs(final Table<NamedNode> table, final Form<NamedNode> form) {
        FormItem<String> formItem = form.getFormItem(HOSTS);
        if (formItem != null) {
            if (table.hasSelection()) {
                ResourceAddress filterRefAddress = HOST_TEMPLATE.append(FILTER_REF + "=" + table.selectedRow().getName())
                        .resolve(mbuiContext.statementContext());
                Operation filterRefOp = new Operation.Builder(filterRefAddress, READ_RESOURCE_OPERATION).build();
                ResourceAddress locationFilterRefAddress = HOST_TEMPLATE
                        .append(LOCATION + "=*")
                        .append(FILTER_REF + "=" + table.selectedRow().getName())
                        .resolve(mbuiContext.statementContext());
                Operation locationFilterRefOp = new Operation.Builder(locationFilterRefAddress, READ_RESOURCE_OPERATION)
                        .build();

                mbuiContext.dispatcher().execute(new Composite(filterRefOp, locationFilterRefOp),
                        (CompositeResult result) -> {
                            SortedSet<String> hosts = new TreeSet<>();
                            result.step(0).get(RESULT).asList().stream()
                                    .map(node -> {
                                        ResourceAddress adr = new ResourceAddress(node.get(ADDRESS));
                                        ResourceAddress host = adr.getParent();
                                        ResourceAddress server = host.getParent();
                                        return server.lastValue() + "/" + host.lastValue();
                                    })
                                    .forEach(hosts::add);
                            result.step(1).get(RESULT).asList().stream()
                                    .map(node -> {
                                        ResourceAddress adr = new ResourceAddress(node.get(ADDRESS));
                                        ResourceAddress host = adr.getParent().getParent();
                                        ResourceAddress server = host.getParent();
                                        return server.lastValue() + "/" + host.lastValue();
                                    })
                                    .forEach(hosts::add);
                            formItem.setValue(String.join(", ", hosts));
                        },
                        (op1, failure) -> formItem.clearValue());
            } else {
                formItem.clearValue();
            }
        }
    }

    @Override
    public void update(final ModelNode modelNode) {
        clearHostFields();

        customFilterForm.clear();
        customFilterTable.update(asNamedNodes(failSafePropertyList(modelNode, "custom-filter")));
        errorPageForm.clear();
        errorPageTable.update(asNamedNodes(failSafePropertyList(modelNode, "error-page")));
        expressionFilterForm.clear();
        expressionFilterTable.update(asNamedNodes(failSafePropertyList(modelNode, "expression-filter")));
        gzipTable.update(asNamedNodes(failSafePropertyList(modelNode, "gzip")));
        modClusterForm.clear();
        modClusterTable.update(asNamedNodes(failSafePropertyList(modelNode, "mod-cluster")));
        requestLimitForm.clear();
        requestLimitTable.update(asNamedNodes(failSafePropertyList(modelNode, "request-limit")));
        responseHeaderForm.clear();
        responseHeaderTable.update(asNamedNodes(failSafePropertyList(modelNode, "response-header")));
        rewriteForm.clear();
        rewriteTable.update(asNamedNodes(failSafePropertyList(modelNode, "rewrite")));
    }

    void noop() {
        // noop
    }
}
