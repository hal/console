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
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.subsystem.elytron.CredentialReference;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.APPLICATION_SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SINGLE_SIGN_ON_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Claudio Miranda
 */
public class ApplicationSecurityDomainView extends HalViewImpl implements ApplicationSecurityDomainPresenter.MyView {

    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Form<ModelNode> configurationForm;
    private final Form<ModelNode> ssoForm;
    private final Form<ModelNode> crForm;
    private final VerticalNavigation navigation;
    private ApplicationSecurityDomainPresenter presenter;

    @Inject
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    public ApplicationSecurityDomainView(final Dispatcher dispatcher, final MetadataRegistry metadataRegistry,
            final Resources resources, final CredentialReference cr) {
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;

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

        // the credential-reference doesn't have an alternative, so they are null
        crForm = cr.form(Ids.UNDERTOW_APP_SECURITY_DOMAIN, ssoMetadata, null, null,
                () -> presenter.credentialReferenceTemplate(),
                () -> presenter.reload());

        Tabs tabs = new Tabs();
        tabs.add(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB, resources.constants().attributes(), configurationForm.asElement());
        tabs.add(Ids.UNDERTOW_SINGLE_SIGN_ON_TAB, "Single Sign-On", ssoForm.asElement());
        tabs.add(Ids.build(Ids.UNDERTOW_APP_SECURITY_DOMAIN_TAB, CREDENTIAL_REFERENCE, Ids.TAB_SUFFIX), Names.CREDENTIAL_REFERENCE, crForm.asElement());

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.APPLICATION_SECURITY_DOMAIN))
                .add(p().textContent(configurationMetadata.getDescription().getDescription()))
                .add(tabs)
                .asElement();

        navigation = new VerticalNavigation();
        navigation.addPrimary(Ids.UNDERTOW_APP_SECURITY_DOMAIN_ENTRY, Names.APPLICATION_SECURITY_DOMAIN, pfIcon("settings"), htmlSection);

        registerAttachable(navigation, configurationForm, ssoForm, crForm);

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
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
        ssoForm.view(failSafeGet(payload, "setting/single-sign-on"));
        crForm.view(failSafeGet(payload, "setting/single-sign-on/credential-reference"));
    }
}
