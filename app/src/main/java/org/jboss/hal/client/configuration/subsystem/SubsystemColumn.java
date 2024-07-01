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
package org.jboss.hal.client.configuration.subsystem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.StabilityLevel;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.ResourceDescriptionPreview;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.i;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.hal.meta.StatementContext.Expression.SELECTED_PROFILE;

@Column(Ids.CONFIGURATION_SUBSYSTEM)
// this has a negative impact on the performance (for the first selection),
// but is necessary to get the stability of a subsystem
@Requires(value = "/{selected.profile}/subsystem=*", recursive = false)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate.of(SELECTED_PROFILE, "subsystem=*");
    private static final boolean DISABLE_EMPTY_SUBSYSTEM = true;
    private static final List<String> EMPTY_SUBSYSTEMS = asList("bean-validation", "ee-security", "jdr",
            "jsr77", "microprofile-opentracing-smallrye", "pojo", "sar");

    @Inject
    public SubsystemColumn(Finder finder,
            Dispatcher dispatcher,
            Environment environment,
            Places places,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            ItemActionFactory itemActionFactory,
            Subsystems subsystems,
            Resources resources) {

        super(new Builder<SubsystemMetadata>(finder, Ids.CONFIGURATION_SUBSYSTEM, Names.SUBSYSTEM)

                .itemRenderer(item -> new ItemDisplay<SubsystemMetadata>() {
                    @Override
                    public String getId() {
                        return item.getName();
                    }

                    @Override
                    public HTMLElement element() {
                        return item.getSubtitle() != null ? ItemDisplay
                                .withSubtitle(item.getTitle(), item.getSubtitle()) : null;
                    }

                    @Override
                    public String getTitle() {
                        return item.getTitle();
                    }

                    @Override
                    public String getFilterData() {
                        return item.getSubtitle() != null ? item.getTitle() + " " + item.getSubtitle() : item.getTitle();
                    }

                    @Override
                    public HTMLElement getIcon() {
                        StabilityLevel stabilityLevel = item.getStabilityLevel();
                        if (environment.highlightStabilityLevel(stabilityLevel)) {
                            return i().css(stabilityLevel.icon)
                                    .title(resources.constants().stabilityLevel() + ": " + stabilityLevel.name().toLowerCase())
                                    .style("color:var(--stability-" + stabilityLevel.name().toLowerCase() + "-color)")
                                    .element();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public String nextColumn() {
                        return item.getNextColumn();
                    }

                    @Override
                    public List<ItemAction<SubsystemMetadata>> actions() {
                        PlaceRequest placeRequest = null;
                        if (item.isGeneric()) {
                            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                            placeRequest = places.genericSubsystem(address);
                        } else if (item.getToken() != null) {
                            placeRequest = places.selectedProfile(item.getToken()).build();
                        }

                        if (placeRequest == null) {
                            return ItemDisplay.super.actions();
                        } else {
                            return singletonList(itemActionFactory.view(placeRequest));
                        }
                    }
                })

                .onPreview(item -> {
                    if (item.getExternalTextResource() != null) {
                        return new PreviewContent<>(item.getTitle(), item.getExternalTextResource());
                    } else {
                        ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                        Operation operation = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                                .build();
                        return new ResourceDescriptionPreview(item.getTitle(), dispatcher, operation);
                    }
                })

                .useFirstActionAsBreadcrumbHandler()
                .showCount()
                .withFilter()
                .pinnable()
                .filterDescription(resources.messages().susbsystemFilterDescription()));

        ItemsProvider<SubsystemMetadata> itemsProvider = (context) -> new Promise<>((resolve, reject) -> {
            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext).getParent();
            Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, SUBSYSTEM).build();
            dispatcher.execute(operation, result -> {
                List<SubsystemMetadata> combined = new ArrayList<>();
                for (ModelNode modelNode : result.asList()) {
                    String name = modelNode.asString();
                    boolean emptySubsystem = DISABLE_EMPTY_SUBSYSTEM && EMPTY_SUBSYSTEMS.contains(name);
                    if (emptySubsystem) {
                        continue;
                    }
                    SubsystemMetadata subsystemMetadata;
                    if (subsystems.containsConfiguration(name)) {
                        subsystemMetadata = subsystems.getConfiguration(name);
                    } else {
                        String title = new LabelBuilder().label(name);
                        subsystemMetadata = new SubsystemMetadata.Builder(name, title).generic().build();
                    }
                    AddressTemplate template = AddressTemplate.of("/{selected.profile}/subsystem=" + name);
                    Metadata metadata = metadataRegistry.lookup(template);
                    subsystemMetadata.setStabilityLevel(metadata.getDescription().getStability());
                    combined.add(subsystemMetadata);
                }
                combined.sort(comparing(SubsystemMetadata::getTitle));
                resolve.onInvoke(combined);
            });
        });
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider(context -> itemsProvider.items(context).then(result -> {
            List<SubsystemMetadata> subsystemsWithTokens = result.stream()
                    .filter(metadata -> metadata.getToken() != null || metadata.isGeneric())
                    .collect(toList());
            return Promise.resolve(subsystemsWithTokens);
        }));
    }
}
