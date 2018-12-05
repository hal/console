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
package org.jboss.hal.client.runtime.subsystem.microprofile.health;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLUListElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.client.runtime.subsystem.microprofile.health.AddressTemplates.MICROPROFILE_HEALTH_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.key;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listGroupItem;
import static org.jboss.hal.resources.Names.STATE;

public class MicroprofileHealthPreview extends PreviewContent<SubsystemMetadata> {

    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private Resources resources;
    private Alert outcomeUp;
    private Alert outcomeDown;

    private HTMLHeadingElement header;
    private final List<HTMLElement> elements = new ArrayList<>();
    private HTMLElement section = section().id(Ids.uniqueId()).get();

    public MicroprofileHealthPreview(Dispatcher dispatcher, StatementContext statementContext, Resources resources) {
        super(Names.MICROPROFILE_HEALTH);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.outcomeUp = new Alert(Icons.OK, resources.messages().microprofileHealthOutcome(UP));
        this.outcomeDown = new Alert(Icons.ERROR, resources.messages().microprofileHealthOutcome(DOWN));

        header = h(1).add(span().textContent(Names.MICROPROFILE_HEALTH)).asElement();
        header.appendChild(refreshLink(() -> update(null)));

        update(null);
    }

    @Override
    public void update(SubsystemMetadata item) {
        ResourceAddress addressWeb = MICROPROFILE_HEALTH_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(addressWeb, CHECK).build();

        // clear the previous state and remove the DOM children to populate with new values
        elements.clear();
        Elements.removeChildrenFrom(section);

        dispatcher.execute(operation, result -> {
            String outcome = result.get(OUTCOME).asString();
            if (UP.equals(outcome)) {
                section.appendChild(outcomeUp.element());
            } else {
                section.appendChild(outcomeDown.element());
            }
            section.appendChild(p().textContent(resources.messages().microprofileHealthPreviewDescription()).asElement());

            List<ModelNode> checks = new ArrayList<>();
            ModelNode modelChecks = result.get(CHECKS);
            int max = 10;
            if (modelChecks.isDefined()) {
                checks = modelChecks.asList();
                if (checks.size() < 10) {
                    max = checks.size();
                }
            }
            // checks may return an empty list
            if (!checks.isEmpty()) {
                // show the first 10 checks in the preview pane
                for (int i = 0; i < max; i++) {
                    ModelNode check = checks.get(i);
                    String name = check.get(NAME).asString();
                    String state = check.get("state").asString();
                    section.appendChild(h(2, name).asElement());

                    Map<String, String> dataMap = new HashMap<>();
                    if (check.hasDefined("data")) {
                        check.get("data").asPropertyList().forEach(data -> {
                            String key = data.getName();
                            String val = data.getValue().asString();
                            dataMap.put(key, val);
                        });
                    }
                    HTMLElement checkElement = checkElement(state, dataMap);
                    section.appendChild(checkElement);
                }
            } else {
                section.appendChild(p().textContent(resources.messages().microprofileHealthNoChecks()).asElement());
            }
            elements.add(section);
        });
    }

    private HTMLElement checkElement(String state, Map<String, String> data) {

        HTMLLIElement liState = li().css(listGroupItem)
                .add(span().css(key).textContent(STATE))
                .add(span().css(CSS.value).textContent(state).asElement())
                .asElement();

        HtmlContentBuilder<HTMLUListElement> ulBuilder = ul().css(listGroup)
                .add(liState);

        if (!data.isEmpty()) {
            HTMLElement dataValue;
            HTMLLIElement liData = li().css(listGroupItem)
                    .add(span().css(key).textContent("Data"))
                    .add(dataValue = span().css(CSS.value).asElement())
                    .asElement();
            dataValue.style.whiteSpace = "pre";

            StringBuilder dataString = new StringBuilder();
            data.forEach((key, value) -> dataString.append(key).append(" \u21D2 ").append(value).append("\n"));
            dataValue.textContent = dataString.toString();

            ulBuilder.add(liData);
        }
        return ulBuilder.asElement();
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        List<HTMLElement> coll = new ArrayList<>();
        coll.add(header);
        coll.addAll(elements);
        return coll.iterator();
    }
}
