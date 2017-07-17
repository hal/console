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

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.APPLICATION_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SINGLE_SIGN_ON_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SETTING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SINGLE_SIGN_ON;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Claudio Miranda
 */
public class ApplicationSecurityDomainView extends HalViewImpl implements ApplicationSecurityDomainPresenter.MyView {

    private final Form<ModelNode> configurationForm;
    private final Form<ModelNode> ssoForm;
    private final Form<ModelNode> crForm;
    private ApplicationSecurityDomainPresenter presenter;

    @Inject
    public ApplicationSecurityDomainView(final MetadataRegistry metadataRegistry, final Resources resources) {

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
                .prepareReset(f -> presenter.resetSingleSignOn(f))
                .prepareRemove(f -> presenter.removeSingleSignOn(f))
                .build();

        // ------------------------------------------------------ credential reference

        crForm = credentialReferenceForm(ssoMetadata, resources);

        Tabs tabs = new Tabs();
        tabs.add(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB, resources.constants().attributes(), configurationForm.asElement());
        tabs.add(Ids.UNDERTOW_SINGLE_SIGN_ON_TAB, Names.SINGLE_SIGN_ON, ssoForm.asElement());
        tabs.add(Ids.build(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB, CREDENTIAL_REFERENCE, Ids.TAB_SUFFIX), Names.CREDENTIAL_REFERENCE, crForm.asElement());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.APPLICATION_SECURITY_DOMAIN))
                .add(p().textContent(configurationMetadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();

        registerAttachable(configurationForm, ssoForm, crForm);

        initElement(htmlSection);

    }

    private Form<ModelNode> credentialReferenceForm(Metadata metadata,
            final Resources resources) {
        Metadata crMetadata = metadata.forComplexAttribute(CREDENTIAL_REFERENCE);

        EmptyState noCredentialReference = new EmptyState.Builder(resources.constants().noResource())
                .description(resources.messages().credentialReferenceParentNoResource(Names.SINGLE_SIGN_ON))
                .icon(fontAwesome("warning"))
                .build();

        Form<ModelNode> form = new ModelNodeForm.Builder<>(
                Ids.build(Ids.UNDERTOW_APP_SECURITY_DOMAIN, CREDENTIAL_REFERENCE, Ids.FORM_SUFFIX), crMetadata)
                .singleton(() -> presenter.checkSingleSignOn() , noCredentialReference)
                .onSave((f, changedValues) -> presenter.saveCredentialReference(changedValues))
                .build();

        return form;
    }

    @Override
    public void attach() {
        super.attach();
    }

    @Override
    public void setPresenter(final ApplicationSecurityDomainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final ModelNode payload) {
        configurationForm.view(payload);
        ssoForm.view(failSafeGet(payload, SETTING + "/" + SINGLE_SIGN_ON));
        crForm.view(failSafeGet(payload, SETTING + "/" + SINGLE_SIGN_ON + "/" + CREDENTIAL_REFERENCE));
    }
}
