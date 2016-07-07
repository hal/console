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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.client.GenericSubsystemPresenter;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;

/**
 * @author Harald Pehl
 */
@Column(Ids.CONFIGURATION_SUBSYSTEM_COLUMN)
public class SubsystemColumn extends FinderColumn<SubsystemMetadata> {

    private static class ResourceDescriptionPreview extends PreviewContent<SubsystemMetadata> {

        ResourceDescriptionPreview(final String header, final Dispatcher dispatcher, final Operation operation) {
            super(header);
            previewBuilder().section().rememberAs(CONTENT_ELEMENT).end();
            Element content = previewBuilder().referenceFor(CONTENT_ELEMENT);
            dispatcher.execute(operation, result -> {
                if (result.hasDefined(DESCRIPTION)) {
                    SafeHtml html = SafeHtmlUtils.fromSafeConstant(result.get(DESCRIPTION).asString());
                    content.setInnerHTML(html.asString());
                }
            });
        }
    }


    private static final AddressTemplate SUBSYSTEM_TEMPLATE = AddressTemplate.of("{selected.profile}/subsystem=*");

    private static PlaceRequest subsystemPlaceRequest(Places places, StatementContext statementContext,
            SubsystemMetadata metadata) {
        PlaceRequest placeRequest = null;
        if (metadata.hasCustomImplementation() && metadata.getToken() != null) {
            placeRequest = places.selectedProfile(metadata.getToken()).build();

        } else if (!metadata.hasCustomImplementation()) {
            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, metadata.getName());
            placeRequest = new PlaceRequest.Builder()
                    .nameToken(NameTokens.GENERIC_SUBSYSTEM)
                    .with(GenericSubsystemPresenter.ADDRESS_PARAM, address.toString())
                    .build();
        }
        return placeRequest;
    }

    @Inject
    public SubsystemColumn(final Finder finder,
            final Dispatcher dispatcher,
            final PlaceManager placeManager,
            final Places places,
            final StatementContext statementContext,
            final Subsystems subsystems,
            final Resources resources) {

        super(new Builder<SubsystemMetadata>(finder, Ids.CONFIGURATION_SUBSYSTEM_COLUMN, Names.SUBSYSTEM)

                .itemRenderer(item -> new ItemDisplay<SubsystemMetadata>() {
                    @Override
                    public String getId() {
                        return item.getName();
                    }

                    @Override
                    public Element asElement() {
                        return item.getSubtitle() != null ? ItemDisplay
                                .withSubtitle(item.getTitle(), item.getSubtitle()) : null;
                    }

                    @Override
                    public String getTitle() {
                        return item.getTitle();
                    }

                    @Override
                    public String getFilterData() {
                        return item.getSubtitle() != null
                                ? item.getTitle() + " " + item.getSubtitle()
                                : item.getTitle();
                    }

                    @Override
                    public String nextColumn() {
                        return item.getNextColumn();
                    }

                    @Override
                    public List<ItemAction<SubsystemMetadata>> actions() {
                        final PlaceRequest placeRequest = subsystemPlaceRequest(places, statementContext, item);
                        if (placeRequest != null) {
                            return singletonList(new ItemAction<>(resources.constants().view(),
                                    item -> placeManager.revealPlace(placeRequest)));
                        } else {
                            return ItemDisplay.super.actions();
                        }
                    }
                })
                .showCount()
                .withFilter()
                .pinnable()
                .useFirstActionAsBreadcrumbHandler()

                .onPreview(item -> {
                    if (item.getPreviewContent() != null) {
                        return item.getPreviewContent();
                    } else {
                        String camelCase = LOWER_HYPHEN.to(LOWER_CAMEL, item.getName());
                        ExternalTextResource resource = resources.preview(camelCase);
                        if (resource != null) {
                            return new PreviewContent(item.getTitle(), resource);

                        } else {
                            ResourceAddress address = SUBSYSTEM_TEMPLATE.resolve(statementContext, item.getName());
                            Operation operation = new Operation.Builder(READ_RESOURCE_DESCRIPTION_OPERATION, address)
                                    .build();
                            return new ResourceDescriptionPreview(item.getTitle(), dispatcher, operation);
                        }
                    }
                }));

        ItemsProvider<SubsystemMetadata> itemsProvider = (context, callback) -> {
            ResourceAddress address = AddressTemplate.of("/{selected.profile}").resolve(statementContext);
            Operation subsystemOp = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                    .param(CHILD_TYPE, ModelDescriptionConstants.SUBSYSTEM).build();
            dispatcher.execute(subsystemOp, result -> {

                List<SubsystemMetadata> combined = new ArrayList<>();
                for (ModelNode modelNode : result.asList()) {
                    String name = modelNode.asString();
                    if (subsystems.containsConfiguration(name)) {
                        combined.add(subsystems.getConfigurationSubsystem(name));

                    } else {
                        String title = new LabelBuilder().label(name);
                        SubsystemMetadata subsystem = new SubsystemMetadata(name, title, null, null, null,
                                false);
                        combined.add(subsystem);
                    }
                }
                callback.onSuccess(combined);
            });
        };
        setItemsProvider(itemsProvider);

        // reuse the items provider to filter breadcrumb items
        setBreadcrumbItemsProvider((context, callback) ->
                itemsProvider.get(context, new AsyncCallback<List<SubsystemMetadata>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(final List<SubsystemMetadata> result) {
                        // only subsystems w/o next columns will show up in the breadcrumb dropdown
                        List<SubsystemMetadata> subsystemsWithTokens = result.stream()
                                .filter(metadata -> metadata.getNextColumn() == null)
                                .collect(toList());
                        callback.onSuccess(subsystemsWithTokens);
                    }
                }));
    }
}
