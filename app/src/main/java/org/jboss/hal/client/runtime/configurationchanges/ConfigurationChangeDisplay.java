/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.configurationchanges;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLPreElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Ids.CONFIGURATION_CHANGES;

class ConfigurationChangeDisplay implements ItemDisplay<ConfigurationChange> {

    private static final String COLON = ": ";

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
        return builder.element();
    }

    @Override
    public String getTitle() {
        return resources.constants().operationDate() + COLON + Format.mediumDateTime(item.getOperationDate());
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public SafeHtml getDescriptionHtml() {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        if (hideDescriptionWhenLarge()) {
            html.append(SafeHtmlUtils.fromTrustedString("<pre class=\"" + formControlStatic + " " + wrap + "\">"));
        }
        item.changes().forEach(m -> {
            String op = m.get(OPERATION).asString();
            ResourceAddress address = new ResourceAddress(m.get(ADDRESS));
            html.append(SafeHtmlUtils.fromTrustedString(
                    resources.constants().operation() + ": <strong>" + op + "</strong><br/>"));
            html.append(SafeHtmlUtils.fromTrustedString(
                    resources.constants().address() + ": <strong>" + address + "</strong><br/>"));
            HTMLPreElement elem = pre().css(formControlStatic, wrap).element();
            m.asPropertyList().forEach(prop -> {
                boolean allowedProperties = !(prop.getName().equals(OPERATION) || prop.getName()
                        .equals(ADDRESS) || prop.getName().equals(OPERATION_HEADERS));
                if (allowedProperties) {
                    html.append(SafeHtmlUtils.fromTrustedString(
                            "&nbsp;&nbsp;&nbsp;&nbsp;" + prop.getName() + COLON + prop.getValue() + "<br/>"));
                }
            });
        });
        if (hideDescriptionWhenLarge()) {
            html.append(SafeHtmlUtils.fromTrustedString("</pre>"));
        }
        return html.toSafeHtml();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public Iterable<HTMLElement> getAdditionalInfoElements() {
        return collect()
                .add(div().css(halConfChangesAdditionalInfo)
                        .add(p().css(textRight).innerHtml(new SafeHtmlBuilder()
                                .appendEscaped(resources.constants().accessMechanism() + COLON)
                                .appendEscaped(item.getAccessMechanism())
                                .appendHtmlConstant("<br/>")

                                .appendEscaped(resources.constants().remoteAddress() + COLON)
                                .appendEscaped(item.getRemoteAddress())
                                .appendHtmlConstant("<br/>")

                                .appendEscaped(resources.constants().composite() + COLON)
                                .appendEscaped(String.valueOf(item.isComposite()))
                                .toSafeHtml()))).elements();
    }

    @Override
    public int getDescriptionLength() {
        return item.getOperationsLength();
    }

    @Override
    public List<ItemAction<ConfigurationChange>> actions() {
        List<ItemAction<ConfigurationChange>> actions = new ArrayList<>();
        String id = Ids.build(CONFIGURATION_CHANGES, item.getName(), "view");
        actions.add(new ItemAction<>(id, resources.constants().view(), presenter::viewRawChange));
        String cliId = Ids.build(CONFIGURATION_CHANGES, item.getName(), "cli");
        actions.add(new ItemAction<>(cliId, resources.constants().cli(), presenter::viewCli));
        return actions;
    }


}
