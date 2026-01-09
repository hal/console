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

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.Element;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLCollection;

import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;
import static org.jboss.hal.resources.CSS.halTableButtons;
import static org.jboss.hal.resources.Ids.ENABLE_SSL;
import static org.jboss.hal.resources.UIConstants.CONSTRAINT;

class HttpsListenerElement extends ListenerElement {

    private final HTMLButtonElement setupSslButton;
    private String selectedHttps;

    HttpsListenerElement(Resources resources, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory) {

        AddressTemplate template = SERVER_TEMPLATE.append(HTTPS.resource + "=*");
        Constraint constraint = Constraint.writable(template, SSL_CONTEXT);

        setupSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().setupSSL())
                .css(Button.DEFAULT_CSS)
                .data(CONSTRAINT, constraint.data()).element();
        bind(setupSslButton, click, ev -> presenter.setupSsl(selectedHttps));

        Metadata metadata = metadataRegistry.lookup(template);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(HTTPS.baseId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(template, table -> presenter.addListener(HTTPS)))
                .button(tableButtonFactory.remove(template,
                        table -> presenter.removeListener(HTTPS, table.selectedRow().getName())))
                .nameColumn()
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(HTTPS.baseId, Ids.FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveListener(HTTPS, form.getModel().getName(),
                        changedValues))
                .prepareReset(form -> presenter.resetListener(HTTPS, form.getModel().getName(), form))
                .build();

        root = section()
                .add(h(1).textContent(HTTPS.type))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();
    }

    @Override
    public void attach() {
        super.attach();

        HTMLCollection<Element> elems = table.element().getElementsByClassName(halTableButtons);
        if (elems.length > 0) {
            Element tableButtonsElement = elems.item(0);
            tableButtonsElement.appendChild(setupSslButton);
            Elements.setVisible(setupSslButton, false);
        }

        table.onSelectionChange(table1 -> {
            if (table1.hasSelection()) {
                selectedHttps = table1.selectedRow().getName();
                Elements.setVisible(setupSslButton, true);
            } else {
                Elements.setVisible(setupSslButton, false);
            }
        });

    }
}
