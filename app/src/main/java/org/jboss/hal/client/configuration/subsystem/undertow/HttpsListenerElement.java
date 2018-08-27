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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental2.dom.Element;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
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

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.Listener.HTTPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;
import static org.jboss.hal.resources.CSS.halTableButtons;
import static org.jboss.hal.resources.Ids.DISABLE_SSL;
import static org.jboss.hal.resources.Ids.ENABLE_SSL;
import static org.jboss.hal.resources.UIConstants.CONSTRAINT;

class HttpsListenerElement extends ListenerElement {

    private HTMLButtonElement enableSslButton;
    private HTMLButtonElement disableSslButton;
    private String selectedHttps;

    @SuppressWarnings("ConstantConditions")
    HttpsListenerElement(Resources resources, MetadataRegistry metadataRegistry, TableButtonFactory tableButtonFactory) {

        AddressTemplate template = SERVER_TEMPLATE.append(HTTPS.resource + "=*");
        Constraint constraint = Constraint.writable(template, SSL_CONTEXT);

        enableSslButton = button().id(ENABLE_SSL)
                .textContent(resources.constants().enableSSL())
                .css(org.jboss.hal.ballroom.Button.DEFAULT_CSS)
                .data(CONSTRAINT, constraint.data())
                .asElement();
        bind(enableSslButton, click, ev -> presenter.enableSsl(selectedHttps));

        disableSslButton = button().id(DISABLE_SSL)
                .textContent(resources.constants().disableSSL())
                .css(org.jboss.hal.ballroom.Button.DEFAULT_CSS)
                .data(CONSTRAINT, constraint.data())
                .asElement();
        bind(disableSslButton, click, ev -> presenter.disableSsl(selectedHttps));

        Metadata metadata = metadataRegistry.lookup(template);
        table = new ModelNodeTable.Builder<NamedNode>(Ids.build(HTTPS.baseId, Ids.TABLE), metadata)
                .button(tableButtonFactory.add(template, table -> presenter.addListener(HTTPS)))
                .button(tableButtonFactory.remove(template,
                        table -> presenter.removeListener(HTTPS, table.selectedRow().getName())))
                .column(NAME, (cell, type, row, meta) -> SafeHtmlUtils.fromString(row.getName()).asString())
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
                .add(form)
                .asElement();
    }

    @Override
    public void attach() {
        super.attach();

        NodeList<Element> elems = table.asElement().getElementsByClassName(halTableButtons);
        if (elems.length > 0) {
            Element tableButtonsElement = elems.item(0);
            tableButtonsElement.appendChild(enableSslButton);
            tableButtonsElement.appendChild(disableSslButton);
            Elements.setVisible(enableSslButton, false);
            Elements.setVisible(disableSslButton, false);
        }

        table.onSelectionChange(table1 -> {
            if (table1.hasSelection()) {
                selectedHttps = table1.selectedRow().getName();
                boolean sslContextExists = table1.selectedRow().asModelNode().hasDefined(SSL_CONTEXT);
                Elements.setVisible(enableSslButton, !sslContextExists);
                Elements.setVisible(disableSslButton, sslContextExists);
            } else {
                Elements.setVisible(enableSslButton, false);
                Elements.setVisible(disableSslButton, false);
            }
        });

    }
}
