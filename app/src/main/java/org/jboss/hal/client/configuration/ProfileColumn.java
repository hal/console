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
package org.jboss.hal.client.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.configuration.ProfileSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLONE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TO_PROFILE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

@Column(Ids.PROFILE)
@Requires(value = "/profile=*", recursive = false)
public class ProfileColumn extends FinderColumn<NamedNode> {

    private static final AddressTemplate PROFILE_TEMPLATE = AddressTemplate.of("/profile=*");

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;

    @Inject
    public ProfileColumn(Finder finder,
            Dispatcher dispatcher,
            EventBus eventBus,
            PlaceManager placeManager,
            Places places,
            FinderPathFactory finderPathFactory,
            CrudOperations crud,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            StatementContext statementContext,
            Resources resources) {

        super(new Builder<NamedNode>(finder, Ids.PROFILE, Names.PROFILE)
                .itemsProvider(context -> crud.readChildren(ResourceAddress.root(), PROFILE)
                        .then(children -> Promise.resolve(asNamedNodes(children))))

                .onItemSelect(item -> eventBus.fireEvent(new ProfileSelectionEvent(item.getName())))

                .onPreview(item -> new ProfilePreview(dispatcher, finderPathFactory, places, resources, item))

                .onBreadcrumbItem((item, context) -> {
                    PlaceRequest.Builder builder;
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();

                    if (NameTokens.GENERIC_SUBSYSTEM.equals(current.getNameToken())) {
                        // switch profile in address parameter of generic presenter
                        builder = new PlaceRequest.Builder().nameToken(current.getNameToken());
                        String addressParam = current.getParameter(Places.ADDRESS_PARAM, null);
                        if (addressParam != null) {
                            ResourceAddress currentAddress = AddressTemplate.of(addressParam).resolve(statementContext);
                            ResourceAddress newAddress = currentAddress.replaceValue(PROFILE, item.getName());
                            builder.with(Places.ADDRESS_PARAM, newAddress.toString());
                        }

                    } else {
                        // switch profile in place request parameter of specific presenter
                        builder = places.replaceParameter(current, PROFILE, item.getName());
                    }
                    placeManager.revealPlace(builder.build());
                })

                .withFilter()
                .pinnable());
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement element() {
                if (item.hasDefined(INCLUDES) && !item.get(INCLUDES).asList().isEmpty()) {
                    String includes = item.get(INCLUDES).asList().stream()
                            .map(ModelNode::asString)
                            .collect(joining(", "));
                    return ItemDisplay.withSubtitle(item.getName(), includes);
                }
                return null;
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<NamedNode>()
                        .title(resources.constants().clone_())
                        .handler(itm -> cloneProfile(itm.getName()))
                        .constraint(Constraint.executable(PROFILE_TEMPLATE, CLONE))
                        .build());
                actions.add(itemActionFactory.remove(
                        Names.PROFILE, item.getName(), PROFILE_TEMPLATE, ProfileColumn.this));
                return actions;
            }

            @Override
            public String nextColumn() {
                return Ids.CONFIGURATION_SUBSYSTEM;
            }
        });

        addColumnAction(columnActionFactory.add(Ids.PROFILE_ADD, Names.PROFILE,
                PROFILE_TEMPLATE, singletonList(INCLUDES), this::createUniqueValidation));
        addColumnAction(columnActionFactory.refresh(Ids.PROFILE_REFRESH));
    }

    private void cloneProfile(String from) {
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.PROFILE_CLONE, Metadata.empty())
                .unboundFormItem(new NameItem())
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.constants().cloneProfile(), form, (to, model) -> {
            Operation operation = new Operation.Builder(new ResourceAddress().add(PROFILE, from), CLONE)
                    .param(TO_PROFILE, to)
                    .build();
            dispatcher.execute(operation, result -> {
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().cloneProfileSuccess(from, to)));
                refresh(to);
            });
        });
        dialog.show();
    }
}
