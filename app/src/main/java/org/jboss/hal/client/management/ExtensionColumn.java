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
package org.jboss.hal.client.management;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.extension.Extension;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.ExtensionStorage;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental2.dom.DomGlobal.window;
import static java.util.Collections.singletonList;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.core.extension.Extension.Point.CUSTOM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BUNDLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXTENSION_POINT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STANDALONE;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.EXTENSION)
public class ExtensionColumn extends FinderColumn<InstalledExtension> {

    private final EventBus eventBus;
    private final ExtensionRegistry extensionRegistry;
    private final ExtensionStorage extensionStorage;
    private final Resources resources;

    @Inject
    public ExtensionColumn(final Finder finder,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory,
            final ExtensionRegistry extensionRegistry,
            final ExtensionStorage extensionStorage,
            final Resources resources) {

        super(new Builder<InstalledExtension>(finder, Ids.EXTENSION, Names.EXTENSION)

                .itemsProvider((context, callback) -> {
                    // TODO Load bundled extensions from /core-service=management/console-extension=*
                    // TODO and mark them as STANDALONE = false
                    List<InstalledExtension> standaloneExtensions = extensionStorage.list();
                    for (NamedNode extension : standaloneExtensions) {
                        extension.get(STANDALONE).set(true);
                    }
                    callback.onSuccess(standaloneExtensions);
                })
                .onPreview(item -> new ExtensionPreview(item, extensionRegistry, resources))
                .showCount()
                .withFilter()
        );

        this.eventBus = eventBus;
        this.extensionRegistry = extensionRegistry;
        this.extensionStorage = extensionStorage;
        this.resources = resources;

        addColumnAction(new ColumnAction.Builder<InstalledExtension>(Ids.EXTENSION_ADD)
                .element(columnActionFactory.addButton(Names.EXTENSION))
                .handler(column -> add())
                .build());

        setItemRenderer(item -> new ItemDisplay<InstalledExtension>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                Extension.Point point = ModelNodeHelper.asEnumValue(item, EXTENSION_POINT,
                        Extension.Point::valueOf, CUSTOM);
                return ItemDisplay.withSubtitle(item.getName(), point.title());
            }

            @Override
            public String getFilterData() {
                Extension.Point point = ModelNodeHelper.asEnumValue(item, EXTENSION_POINT,
                        Extension.Point::valueOf, CUSTOM);
                String deployment = ModelNodeHelper.failSafeBoolean(item, STANDALONE) ? STANDALONE : BUNDLED;
                return String.join(" ", item.getName(), point.title(), deployment);
            }

            @Override
            public String getTooltip() {
                if (ModelNodeHelper.failSafeBoolean(item, STANDALONE)) {
                    return Names.STANDALONE_EXTENSION;
                } else {
                    return Names.BUNDLED_EXTENSION;
                }
            }

            @Override
            public HTMLElement getIcon() {
                return ModelNodeHelper.failSafeBoolean(item, STANDALONE)
                        ? span().css(fontAwesome("puzzle-piece")).asElement()
                        : span().css(fontAwesome("archive")).asElement();
            }

            @Override
            public List<ItemAction<InstalledExtension>> actions() {
                if (ModelNodeHelper.failSafeBoolean(item, STANDALONE)) {
                    return singletonList(new ItemAction.Builder<InstalledExtension>()
                            .title(resources.constants().remove())
                            .handler(itm -> remove(itm))
                            .build());
                }
                return Collections.emptyList();
            }
        });
    }

    private void add() {
        new AddExtensionWizard(this, eventBus, extensionRegistry, extensionStorage, resources).show();
    }

    private void remove(InstalledExtension extension) {
        DialogFactory.showConfirmation(resources.messages().removeConfirmationTitle(Names.EXTENSION),
                resources.messages().removeExtensionQuestion(), () -> {
                    extensionStorage.remove(extension);
                    Message message = Message.success(
                            resources.messages().removeExtensionSuccess(),
                            resources.constants().reload(),
                            () -> window.location.reload(), true);
                    MessageEvent.fire(eventBus, message);
                    refresh(RefreshMode.CLEAR_SELECTION);
                });
    }
}
