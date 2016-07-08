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
package org.jboss.hal.core.subsystem;

import com.google.gwt.resources.client.ExternalTextResource;
import org.jboss.hal.core.finder.PreviewCallback;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.ResourceDescriptionPreview;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;

/**
 * @author Harald Pehl
 */
public class SubsystemPreviewCallback implements PreviewCallback<SubsystemMetadata> {

    private final AddressTemplate template;
    private final Resources resources;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;

    public SubsystemPreviewCallback(final Dispatcher dispatcher, final StatementContext statementContext,
            final AddressTemplate template, final Resources resources) {
        this.template = template;
        this.resources = resources;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
    }

    @Override
    public PreviewContent<SubsystemMetadata> onPreview(final SubsystemMetadata item) {
        if (item.getPreviewContent() != null) {
            //noinspection unchecked
            return item.getPreviewContent();
        } else {
            String camelCase = LOWER_HYPHEN.to(LOWER_CAMEL, item.getName());
            ExternalTextResource resource = resources.preview(camelCase);
            if (resource != null) {
                return new PreviewContent<>(item.getTitle(), resource);

            } else {
                ResourceAddress address = template.resolve(statementContext, item.getName());
                Operation operation = new Operation.Builder(READ_RESOURCE_DESCRIPTION_OPERATION, address).build();
                return new ResourceDescriptionPreview(item.getTitle(), dispatcher, operation);
            }
        }
    }
}
