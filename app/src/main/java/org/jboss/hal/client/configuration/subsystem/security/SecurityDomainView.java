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
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACL_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGIN_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAPPING_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.POLICY_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRUST_MODULE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

@MbuiView
@Deprecated
@SuppressWarnings({ "WeakerAccess", "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "unused" })
public abstract class SecurityDomainView extends MbuiViewImpl<SecurityDomainPresenter>
        implements SecurityDomainPresenter.MyView {

    public static SecurityDomainView create(final MbuiContext mbuiContext) {
        return new Mbui_SecurityDomainView(mbuiContext);
    }

    @MbuiElement("security-domain-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("security-domain-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("security-domain-authentication-table") Table<NamedNode> authenticationTable;
    @MbuiElement("security-domain-authentication-form") Form<NamedNode> authenticationForm;
    @MbuiElement("security-domain-authorization-table") Table<NamedNode> authorizationTable;
    @MbuiElement("security-domain-authorization-form") Form<NamedNode> authorizationForm;
    @MbuiElement("security-domain-audit-table") Table<NamedNode> auditTable;
    @MbuiElement("security-domain-audit-form") Form<NamedNode> auditForm;
    @MbuiElement("security-domain-acl-table") Table<NamedNode> aclTable;
    @MbuiElement("security-domain-acl-form") Form<NamedNode> aclForm;
    @MbuiElement("security-domain-trust-table") Table<NamedNode> trustTable;
    @MbuiElement("security-domain-trust-form") Form<NamedNode> trustForm;
    @MbuiElement("security-domain-mapping-table") Table<NamedNode> mappingTable;
    @MbuiElement("security-domain-mapping-form") Form<NamedNode> mappingForm;

    public SecurityDomainView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final SecurityDomain securityDomain) {
        configurationForm.view(securityDomain);

        authenticationForm.clear();
        authorizationForm.clear();
        auditForm.clear();
        aclForm.clear();
        trustForm.clear();
        mappingForm.clear();

        authenticationTable.update(
                asNamedNodes(failSafePropertyList(securityDomain, "authentication/classic/" + LOGIN_MODULE)));
        authorizationTable.update(
                asNamedNodes(failSafePropertyList(securityDomain, "authorization/classic/" + POLICY_MODULE)));
        auditTable.update(asNamedNodes(failSafePropertyList(securityDomain, "audit/classic/" + PROVIDER_MODULE)));
        aclTable.update(asNamedNodes(failSafePropertyList(securityDomain, "acl/classic/" + ACL_MODULE)));
        trustTable.update(asNamedNodes(failSafePropertyList(securityDomain, "identity-trust/classic/" + TRUST_MODULE)));
        mappingTable.update(asNamedNodes(failSafePropertyList(securityDomain, "mapping/classic/" + MAPPING_MODULE)));
    }
}
