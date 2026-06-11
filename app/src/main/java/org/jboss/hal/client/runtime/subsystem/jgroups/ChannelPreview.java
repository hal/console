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
package org.jboss.hal.client.runtime.subsystem.jgroups;

import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;

import com.google.common.collect.ImmutableMap;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;

class ChannelPreview extends PreviewContent<NamedNode> {

    private final ChannelDonut bytes;
    private final ChannelDonut messages;
    private final ModelNodeForm<NamedNode> form;

    ChannelPreview(NamedNode channel, Metadata metadata) {
        super(channel.getName());
        getHeaderContainer().title = channel.getName();

        String RECEIVED_BYTES = "received-bytes";
        String SENT_BYTES = "sent-bytes";
        String RECEIVED_MESSAGES = "received-messages";
        String SENT_MESSAGES = "sent-messages";

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_CHANNEL_RUNTIME, Ids.FORM), metadata)
                .includeRuntime()
                .exclude(RECEIVED_BYTES, SENT_BYTES, RECEIVED_MESSAGES, SENT_MESSAGES)
                .readOnly()
                .build();

        bytes = new ChannelDonut("bytes", SENT_BYTES, RECEIVED_BYTES);
        messages = new ChannelDonut("messages", SENT_MESSAGES, RECEIVED_MESSAGES);

        registerAttachable(form, bytes.getDonut(), messages.getDonut());
        previewBuilder().addAll(form, bytes.getDonut(), messages.getDonut());
    }

    @Override
    public void update(NamedNode channel) {
        form.view(channel);
        if (channel.hasDefined(ADDRESS)) {
            bytes.updateDonut(channel);
            messages.updateDonut(channel);
        }
    }

    private static class ChannelDonut {
        private final Donut donut;
        private final String sent;
        private final String received;

        public ChannelDonut(String unit, String sent, String received) {
            this.sent = sent;
            this.received = received;

            donut = new Donut.Builder(unit)
                    .add(sent, sent, PatternFly.colors.green)
                    .add(received, received, PatternFly.colors.blue)
                    .legend(Donut.Legend.RIGHT)
                    .responsive(true)
                    .build();
        }

        public Donut getDonut() {
            return donut;
        }

        public void updateDonut(NamedNode channel) {
            donut.update(ImmutableMap.of(sent, channel.get(sent).asLong(), received, channel.get(received).asLong()));
        }
    }
}
