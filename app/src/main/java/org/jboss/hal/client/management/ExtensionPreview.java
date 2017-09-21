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

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.extension.ExtensionRegistry;
import org.jboss.hal.core.extension.InstalledExtension;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class ExtensionPreview extends PreviewContent<InstalledExtension> {

    ExtensionPreview(final InstalledExtension extension,
            final ExtensionRegistry extensionRegistry,
            final Resources resources) {

        super(Names.EXTENSION);

        Alert scriptOk = new Alert(Icons.OK, resources.messages().extensionOk());
        Alert scriptError = new Alert(Icons.ERROR, resources.messages().extensionScriptError());

        PreviewAttributes<InstalledExtension> attributes = new PreviewAttributes<>(extension,
                asList(NAME, VERSION, DESCRIPTION))
                .append(model -> new PreviewAttribute(Names.URL, model.get(URL).asString(), model.get(URL).asString(),
                        Ids.build(model.getName(), URL)))
                .append(SCRIPT)
                .append(STYLESHEETS)
                .append(EXTENSION_POINT)
                .append(AUTHOR)
                .append(AUTHOR)
                .append(model -> new PreviewAttribute(new LabelBuilder().label(HOMEPAGE),
                        model.get(HOMEPAGE).asString(), model.get(HOMEPAGE).asString(),
                        Ids.build(model.getName(), Ids.HOMEPAGE)))
                .append(LICENSE);

        previewBuilder()
                .add(scriptOk)
                .add(scriptError)
                .addAll(attributes);

        boolean injected = extensionRegistry.verifyScript(extension.getFqScript());
        Elements.setVisible(scriptOk.asElement(), injected);
        Elements.setVisible(scriptError.asElement(), !injected);
    }
}
