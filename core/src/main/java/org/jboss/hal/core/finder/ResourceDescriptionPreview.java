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
package org.jboss.hal.core.finder;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;

/**
 * Preview which shows the description of a {@code :read-resource-description} operation.
 *
 * @author Harald Pehl
 */
public class ResourceDescriptionPreview extends PreviewContent<SubsystemMetadata> {

    private static final String CONTENT_ELEMENT = "contentElement";

    public ResourceDescriptionPreview(final String header, final Dispatcher dispatcher, final Operation rrd) {
        super(header);
        previewBuilder().section().rememberAs(CONTENT_ELEMENT).end();
        Element content = previewBuilder().referenceFor(CONTENT_ELEMENT);
        dispatcher.execute(rrd, result -> {
            if (result.hasDefined(DESCRIPTION)) {
                SafeHtml html = SafeHtmlUtils.fromSafeConstant(result.get(DESCRIPTION).asString());
                content.setInnerHTML(html.asString());
            }
        });
    }
}
