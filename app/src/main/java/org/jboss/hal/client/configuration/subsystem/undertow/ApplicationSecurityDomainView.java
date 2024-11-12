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
package org.jboss.hal.client.configuration.subsystem.undertow;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.APPLICATION_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SINGLE_SIGN_ON_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SETTING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SINGLE_SIGN_ON;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

public class ApplicationSecurityDomainView extends HalViewImpl implements ApplicationSecurityDomainPresenter.MyView {

    private final Form<ModelNode> configurationForm;
    private final Form<ModelNode> ssoForm;
    private final Form<ModelNode> crForm;
    private ApplicationSecurityDomainPresenter presenter;

    @Inject
    public ApplicationSecurityDomainView(MetadataRegistry metadataRegistry, Resources resources, CredentialReference cr) {

        // ------------------------------------------------------ main attributes

        Metadata configurationMetadata = metadataRegistry.lookup(APPLICATION_SECURITY_DOMAIN_TEMPLATE);
        configurationForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_APP_SECURITY_DOMAIN_FORM, configurationMetadata)
                .onSave((form, changedValues) -> presenter.save(changedValues))
                .prepareReset(form -> presenter.reset(form))
                .build();

        // ------------------------------------------------------ single sign-on singleton

        Metadata ssoMetadata = metadataRegistry.lookup(SINGLE_SIGN_ON_TEMPLATE);
        ssoForm = new ModelNodeForm.Builder<>(Ids.UNDERTOW_SINGLE_SIGN_ON_FORM, ssoMetadata)
                .singleton(() -> presenter.checkSingleSignOn(),
                        () -> presenter.addSingleSignOn())
                .onSave((f, changedValues) -> presenter.saveSingleSignOn(changedValues))
                .exclude(CREDENTIAL_REFERENCE + ".*")
                .prepareReset(f -> presenter.resetSingleSignOn(f))
                .prepareRemove(f -> presenter.removeSingleSignOn(f))
                .build();

        // ------------------------------------------------------ credential reference

        crForm = cr.form(Ids.UNDERTOW_APP_SECURITY_DOMAIN, ssoMetadata, CREDENTIAL_REFERENCE, null, null,
                () -> presenter.checkSingleSignOn(),
                () -> presenter.resolveSingleSignOn(),
                () -> presenter.addSingleSignOn(),
                () -> presenter.reload());

        Tabs tabs = new Tabs(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB_CONTAINER);
        tabs.add(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB, resources.constants().attributes(),
                configurationForm.element());
        tabs.add(Ids.UNDERTOW_SINGLE_SIGN_ON_TAB, Names.SINGLE_SIGN_ON, ssoForm.element());
        tabs.add(Ids.build(Ids.UNDERTOW_APP_SECURITY_DOMAIN, CREDENTIAL_REFERENCE, Ids.TAB),
                Names.CREDENTIAL_REFERENCE, crForm.element());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.APPLICATION_SECURITY_DOMAIN))
                .add(p().textContent(configurationMetadata.getDescription().getDescription()))
                .add(tabs).element();

        registerAttachable(configurationForm, ssoForm, crForm);

        initElement(htmlSection);

    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public void setPresenter(ApplicationSecurityDomainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(ModelNode payload) {
        configurationForm.view(payload);
        ssoForm.view(failSafeGet(payload, SETTING + "/" + SINGLE_SIGN_ON));
        crForm.view(failSafeGet(payload, SETTING + "/" + SINGLE_SIGN_ON + "/" + CREDENTIAL_REFERENCE));
    }
}
