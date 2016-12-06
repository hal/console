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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class UndertowFilterView extends MbuiViewImpl<UndertowFilterPresenter>
        implements UndertowFilterPresenter.MyView {

    public static UndertowFilterView create(final MbuiContext mbuiContext) {
        return new Mbui_UndertowFilterView(mbuiContext);
    }

    @MbuiElement("undertow-filter-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("undertow-custom-filter-table") NamedNodeTable<NamedNode> customFilterTable;
    @MbuiElement("undertow-custom-filter-form") Form<NamedNode> customFilterForm;
    @MbuiElement("undertow-error-page-table") NamedNodeTable<NamedNode> errorPageTable;
    @MbuiElement("undertow-error-page-form") Form<NamedNode> errorPageForm;
    @MbuiElement("undertow-expression-filter-table") NamedNodeTable<NamedNode> expressionFilterTable;
    @MbuiElement("undertow-expression-filter-form") Form<NamedNode> expressionFilterForm;
    @MbuiElement("undertow-gzip-table") NamedNodeTable<NamedNode> gzipTable;
    @MbuiElement("undertow-gzip-form") Form<NamedNode> gzipForm;
    @MbuiElement("undertow-mod-cluster-table") NamedNodeTable<NamedNode> modClusterTable;
    @MbuiElement("undertow-mod-cluster-form") Form<NamedNode> modClusterForm;
    @MbuiElement("undertow-request-limit-table") NamedNodeTable<NamedNode> requestLimitTable;
    @MbuiElement("undertow-request-limit-form") Form<NamedNode> requestLimitForm;
    @MbuiElement("undertow-response-header-table") NamedNodeTable<NamedNode> responseHeaderTable;
    @MbuiElement("undertow-response-header-form") Form<NamedNode> responseHeaderForm;
    @MbuiElement("undertow-rewrite-table") NamedNodeTable<NamedNode> rewriteTable;
    @MbuiElement("undertow-rewrite-form") Form<NamedNode> rewriteForm;

    UndertowFilterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final ModelNode modelNode) {
        customFilterForm.clear();
        customFilterTable.update(asNamedNodes(failSafePropertyList(modelNode, "custom-filter")));
        errorPageForm.clear();
        errorPageTable.update(asNamedNodes(failSafePropertyList(modelNode, "error-page")));
        expressionFilterForm.clear();
        expressionFilterTable.update(asNamedNodes(failSafePropertyList(modelNode, "expression-filter")));
        gzipForm.clear();
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
}
