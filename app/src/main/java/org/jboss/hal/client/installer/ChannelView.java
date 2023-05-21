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
package org.jboss.hal.client.installer;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.elemento.Elements.*;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_INDEX;

public class ChannelView extends HalViewImpl implements ChannelPresenter.MyView {

    private final Form<ModelNode> channelForm;
    private ChannelPresenter presenter;

    @Inject
    public ChannelView(MetadataRegistry metadataRegistry, Resources resources) {
        channelForm = ChannelFormFactory.channelForm(metadataRegistry, resources);
        channelForm.setSaveCallback(
                (f, changedValues) -> presenter.saveChannel(f.getModel().get(HAL_INDEX).asInt(-1), changedValues));
        registerAttachable(channelForm);

        initElement(row()
                .add(column()
                        .add(h(1).textContent(Names.CHANNEL).element())
                        .add(section()
                                .add(p().textContent(
                                        "A channel is a YAML file that specifies the versions of the JBoss EAP server artifacts.")))
                        .add(section().add(channelForm))
                        .element()));
    }

    @Override
    public void setPresenter(ChannelPresenter presenter) {
        this.presenter = presenter;
    }

    public void update(Channel channel) {
        channelForm.view(channel);
    }
}
