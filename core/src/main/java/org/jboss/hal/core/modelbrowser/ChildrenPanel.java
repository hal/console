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
package org.jboss.hal.core.modelbrowser;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.hal.core.modelbrowser.ModelBrowser.asGenericTemplate;
import static org.jboss.hal.core.modelbrowser.ReadChildren.uniqueId;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/** Panel which holds the children of the selected resource. */
class ChildrenPanel implements Iterable<HTMLElement>, Attachable {

    private static final Logger logger = LoggerFactory.getLogger(ChildrenPanel.class);

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Iterable<HTMLElement> elements;
    private final HTMLElement header;
    private final Table<String> table;
    private Node<Context> parent;

    ChildrenPanel(ModelBrowser modelBrowser, Environment environment, Dispatcher dispatcher,
            MetadataProcessor metadataProcessor, Resources resources) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;

        Options<String> options = new OptionsBuilder<String>()
                .column("resource", Names.RESOURCE, (cell, type, row, meta) -> row)
                .column(new InlineAction<>(resources.constants().view(), row -> modelBrowser.tree.openNode(parent.id,
                        () -> modelBrowser.select(uniqueId(parent, row), false))))
                .button(resources.constants().add(), table -> modelBrowser.add(parent, table.getRows()))
                .button(resources.constants().remove(), table -> {
                            ResourceAddress fq = parent.data.getAddress()
                                    .getParent()
                                    .add(parent.text, table.selectedRow());
                            modelBrowser.remove(fq);
                        }, Scope.SELECTED
                )
                .paging(false)
                .options();

        table = new DataTable<>(Ids.build(Ids.MODEL_BROWSER, "children", Ids.TABLE), options);
        elements = Elements.collect()
                .add(header = h(1).get())
                .add(table)
                .get();
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return elements.iterator();
    }

    @Override
    public void attach() {
        table.attach();
    }

    @SuppressWarnings("HardCodedStringLiteral")
    void update(Node<Context> node, ResourceAddress address) {
        this.parent = node;

        SafeHtmlBuilder safeHtml = new SafeHtmlBuilder();
        if (node.data.hasSingletons()) {
            safeHtml.appendEscaped("Singleton ");
        }
        safeHtml.appendEscaped("Child Resources of ")
                .appendHtmlConstant("<code>")
                .appendEscaped(node.text)
                .appendHtmlConstant("</code>");
        header.innerHTML = safeHtml.toSafeHtml().asString();

        Operation operation = new Operation.Builder(address.getParent(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, node.text)
                .build();
        dispatcher.execute(operation, result -> {
            List<String> names = result.asList().stream().map(ModelNode::asString).collect(toList());
            table.update(names);
            if (node.data.hasSingletons()) {
                logger.debug("Read {} / {} singletons", names.size(), node.data.getSingletons().size());
            } else {
                // enable / disable buttons makes only sense for none-singleton resources!
                AddressTemplate template = asGenericTemplate(node, address);
                metadataProcessor.lookup(template, Progress.NOOP,
                        new MetadataProcessor.MetadataCallback() {
                            @Override
                            public void onMetadata(Metadata metadata) {
                                table.enableButton(0, AuthorisationDecision.from(environment,
                                        constraint -> Optional.of(metadata.getSecurityContext()))
                                        .isAllowed(Constraint.executable(template, ADD)));
                                table.enableButton(1, AuthorisationDecision.from(environment,
                                        constraint -> Optional.of(metadata.getSecurityContext()))
                                        .isAllowed(Constraint.executable(template, REMOVE)));
                            }

                            @Override
                            public void onError(Throwable error) {
                                logger.warn("Unable to enable / disable table buttons for {}", address);
                            }
                        });
            }
        });
    }

    void show() {
        for (HTMLElement element : this) {
            Elements.setVisible(element, true);
        }
        table.show();
    }

    void hide() {
        for (HTMLElement element : this) {
            Elements.setVisible(element, false);
        }
        table.hide();
    }
}
