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
package org.jboss.hal.client.configuration;

import elemental.dom.Element;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;

import javax.inject.Inject;

import static org.jboss.hal.resources.Ids.PATHS_FORM;
import static org.jboss.hal.resources.Names.INTERFACE;

/**
 * @author Harald Pehl
 */
public class InterfaceView extends PatternFlyViewImpl implements InterfacePresenter.MyView {

    private final ModelNodeForm<ModelNode> form;
//    private final Dialog dialog;
    private InterfacePresenter presenter;

    @Inject
    public InterfaceView(SecurityFramework securityFramework,
            ResourceDescriptions descriptions,
            Capabilities capabilities) {

        ResourceDescription description = descriptions.lookup(InterfacePresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(InterfacePresenter.ROOT_TEMPLATE);

//        new Dialog.Builder(resources.messages())

        form = new ModelNodeForm.Builder<>(PATHS_FORM, new Metadata(securityContext, description, capabilities))
                .exclude("resolved-address")
                .onSave((form, changedValues) -> presenter.saveInterface(changedValues))
                .build();

        // @formatter:off
        Element element = new LayoutBuilder()
            .row()
                .column()
                    .header(INTERFACE).end()
                    .add(form.asElement())
                .end()
            .end()
        .build();
        // @formatter:on

        registerAttachable(form);
        initElement(element);

    }

    @Override
    public void setPresenter(final InterfacePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void update(final ModelNode interfce) {
        form.view(interfce);
    }
}
