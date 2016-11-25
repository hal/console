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
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings({"WeakerAccess", "DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class SecurityDomainView extends MbuiViewImpl<SecurityDomainPresenter>
        implements SecurityDomainPresenter.MyView {

    public static SecurityDomainView create(final MbuiContext mbuiContext) {
        return new Mbui_SecurityDomainView(mbuiContext);
    }

    @MbuiElement("security-domain-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("security-domain-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("security-domain-acl-module-table") DataTable<NamedNode> aclModuleTable;
    @MbuiElement("security-domain-acl-module-form") Form<NamedNode> aclModuleForm;
    @MbuiElement("security-domain-provider-module-table") DataTable<NamedNode> providerModuleTable;
    @MbuiElement("security-domain-provider-module-form") Form<NamedNode> providerModuleForm;
    @MbuiElement("security-domain-login-module-table") DataTable<NamedNode> loginModuleTable;
    @MbuiElement("security-domain-login-module-form") Form<NamedNode> loginModuleForm;
    @MbuiElement("security-domain-policy-module-table") DataTable<NamedNode> policyModuleTable;
    @MbuiElement("security-domain-policy-module-form") Form<NamedNode> policyModuleForm;
    @MbuiElement("security-domain-trust-module-table") DataTable<NamedNode> trustModuleTable;
    @MbuiElement("security-domain-trust-module-form") Form<NamedNode> trustModuleForm;
    @MbuiElement("security-domain-mapping-module-table") DataTable<NamedNode> mappingModuleTable;
    @MbuiElement("security-domain-mapping-module-form") Form<NamedNode> mappingModuleForm;

    public SecurityDomainView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final SecurityDomain securityDomain) {
        configurationForm.view(securityDomain);

        aclModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "acl/classic/" + ACL_MODULE)))
                .refresh(RefreshMode.RESET);
        aclModuleForm.clear();

        providerModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "audit/classic/" + PROVIDER_MODULE)))
                .refresh(RefreshMode.RESET);
        providerModuleForm.clear();

        loginModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "authentication/classic/" + LOGIN_MODULE)))
                .refresh(RefreshMode.RESET);
        loginModuleForm.clear();

        policyModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "authorization/classic/" + POLICY_MODULE)))
                .refresh(RefreshMode.RESET);
        policyModuleForm.clear();

        trustModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "identity-trust/classic/" + TRUST_MODULE)))
                .refresh(RefreshMode.RESET);
        trustModuleForm.clear();

        mappingModuleTable.api()
                .clear()
                .add(asNamedNodes(failSafePropertyList(securityDomain, "mapping/classic/" + MAPPING_MODULE)))
                .refresh(RefreshMode.RESET);
        mappingModuleForm.clear();
    }
}
