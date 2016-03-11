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
package org.jboss.hal.client.configuration;

import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Column;

import javax.inject.Inject;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
@Column(ModelDescriptionConstants.PROFILE)
public class ProfileColumn extends FinderColumn<Property> {

    private static final AddressTemplate PROFILE_TEMPLATE = AddressTemplate.of("/profile=*");

    @Inject
    public ProfileColumn(final Finder finder,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final ColumnActionFactory columnActionFactory) {

        super(new Builder<Property>(finder, ModelDescriptionConstants.PROFILE, Names.PROFILES)
                .columnAction(columnActionFactory.add(
                        IdBuilder.build(ModelDescriptionConstants.PROFILE, "add"),
                        Names.PROFILE,
                        PROFILE_TEMPLATE))
                .columnAction(columnActionFactory.refresh(IdBuilder.build(ModelDescriptionConstants.PROFILE, "refresh")))

                .itemRenderer(property -> new ItemDisplay<Property>() {
                    @Override
                    public String getTitle() {
                        return property.getName();
                    }

                    @Override
                    public String nextColumn() {
                        return ModelDescriptionConstants.SUBSYSTEM;
                    }
                })

                .onItemSelect(property -> eventBus.fireEvent(new ProfileSelectionEvent(property.getName())))

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, PROFILE)
                            .build();
                    dispatcher.execute(operation, result -> callback.onSuccess(result.asPropertyList()));
                })

                .onPreview(item -> new PreviewContent(item.getName())));
    }
}
