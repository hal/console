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
package org.jboss.hal.client.runtime.host.configurationchanges;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLPreElement;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.elements;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.pre;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION_CHANGES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.hal.resources.CSS.*;

class ConfigurationChangeDisplay implements ItemDisplay<ConfigurationChange> {

    private final ConfigurationChange item;
    private final ConfigurationChangesPresenter presenter;
    private final Resources resources;

    ConfigurationChangeDisplay(ConfigurationChange item, ConfigurationChangesPresenter presenter, Resources resources) {
        this.item = item;
        this.presenter = presenter;
        this.resources = resources;
    }

    @Override
    public String getId() {
        return Ids.build(CONFIGURATION_CHANGES, String.valueOf(item.getName()));
    }

    @Override
    public HTMLElement getStatusElement() {
        HtmlContentBuilder<HTMLElement> builder = span()
                .css(listHalIconBig)
                .title(resources.constants().outcome() + ":  " + item.getOutcome());
        if (item.isSuccess()) {
            builder.css(pfIcon(ok), listHalIconSuccess);
        } else {
            builder.css(pfIcon(errorCircleO), listHalIconError);
        }
        return builder.asElement();
    }

    @Override
    public String getTitle() {
        return resources.constants().operationDate() + ": " + item.getOperationDate().replace('T', ' ');
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public SafeHtml getDescriptionHtml() {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        item.changes().forEach(m -> {
            String op = m.get(OPERATION).asString();
            ResourceAddress address = new ResourceAddress(m.get(ADDRESS));
            html.append(SafeHtmlUtils.fromTrustedString(resources.constants().operation() + ": <strong>" + op + "</strong>&nbsp;&nbsp;&nbsp;&nbsp;"));
            html.append(SafeHtmlUtils.fromTrustedString(resources.constants().address() + ": <strong>" + address + "</strong><br/>"));
            HTMLPreElement elem = pre().css(formControlStatic, wrap).asElement();
            m.asPropertyList().forEach(prop -> {
                boolean allowedProperties = !(prop.getName().equals(OPERATION) || prop.getName().equals(ADDRESS) || prop.getName().equals(OPERATION_HEADERS));
                if (allowedProperties) {
                    html.append(SafeHtmlUtils.fromTrustedString("&nbsp;&nbsp;&nbsp;&nbsp;" + prop.getName() + ": " + prop.getValue() + "<br/>"));
                }
            });
        });
        return html.toSafeHtml();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public HasElements getAdditionalInfoElements() {
        ElementsBuilder elements = elements();
        elements.add(div().css(halExecutionTime)
                .add(p().css(textRight).innerHtml(new SafeHtmlBuilder()

                        .appendEscaped(resources.constants().accessMechanism() + ": ")
                        .appendEscaped(item.getAccessMechanism())
                        .appendHtmlConstant("<br/>")

                        .appendEscaped(resources.constants().remoteAddress() + ": ")
                        .appendEscaped(item.getRemoteAddress())
                        .appendHtmlConstant("<br/>")

                        .appendEscaped(resources.constants().composite() + ": ")
                        .appendEscaped("" + item.isComposite())
                        .toSafeHtml())));
        return elements;
    }

    @Override
    public List<ItemAction<ConfigurationChange>> actions() {
        List<ItemAction<ConfigurationChange>> actions = new ArrayList<>();
        String id = Ids.build(Ids.CONFIGURATION_CHANGES, item.getName(), "view");
        actions.add(new ItemAction<>(id, resources.constants().view(), presenter::viewRawChange));
        return actions;
    }
}
