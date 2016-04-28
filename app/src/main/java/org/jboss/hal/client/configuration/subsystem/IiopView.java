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
package org.jboss.hal.client.configuration.subsystem;

import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.resources.Names.IIOP_OPENJDK;

/**
 * @author Harald Pehl
 */
public class IiopView extends PatternFlyViewImpl implements IiopPresenter.MyView {

    private ModelNodeForm<ModelNode> form;
    private IiopPresenter presenter;

    @Inject
    public IiopView(MetadataRegistry metadataRegistry) {

        Metadata metadata = metadataRegistry.lookup(IiopPresenter.ROOT_TEMPLATE);
        Element info = new Elements.Builder().p().textContent(metadata.getDescription().getDescription()).end().build();
        form = new ModelNodeForm.Builder<>(Ids.IIOP_FORM, metadata)
                .onSave((form, changedValues) -> presenter.save(changedValues))
                .build();
        registerAttachable(form);

        // @formatter:off
        Element layout = new LayoutBuilder()
            .row()
                .column()
                    .header(IIOP_OPENJDK).end()
                    .add(info)
                    .add(form.asElement())
                .end()
            .end()
        .build();
        // @formatter:on

        initElement(layout);
    }

    @Override
    public void setPresenter(final IiopPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final ModelNode modelNode) {
        form.view(modelNode);
    }

    @Override
    public void clear() {
        form.clear();
    }
}
