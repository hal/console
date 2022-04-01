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
package org.jboss.hal.core.finder;

import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;

/** Preview which shows the description of a {@code :read-resource-description} operation. */
public class ResourceDescriptionPreview extends PreviewContent<SubsystemMetadata> {

    public ResourceDescriptionPreview(String header, Dispatcher dispatcher, Operation rrd) {
        super(header);
        HTMLElement content = section().element();
        previewBuilder().add(content);
        dispatcher.execute(rrd, result -> {
            if (result.hasDefined(DESCRIPTION)) {
                SafeHtml html = SafeHtmlUtils.fromSafeConstant(result.get(DESCRIPTION).asString());
                content.innerHTML = html.asString();
            }
        });
    }
}
