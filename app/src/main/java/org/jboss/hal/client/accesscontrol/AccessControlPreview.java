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
package org.jboss.hal.client.accesscontrol;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
class AccessControlPreview extends PreviewContent<Void> {

    private static final String CONTENT_ELEMENT = "contentElement";

    static Alert simpleProviderWarning(final AccessControl accessControl, final Environment environment,
            final Resources resources) {
        Alert warning = new Alert(Icons.WARNING, resources.messages().simpleProviderWarning(),
                resources.constants().enableRbac(),
                event -> accessControl.switchProvider());
        Elements.setVisible(warning.asElement(), environment.getAccessControlProvider() == AccessControlProvider.SIMPLE);
        return warning;
    }

    AccessControlPreview(AccessControl accessControl, Environment environment, Resources resources) {
        super(Names.ACCESS_CONTROL);

        previewBuilder().add(simpleProviderWarning(accessControl, environment, resources));
        previewBuilder().section().rememberAs(CONTENT_ELEMENT).end();
        Element content = previewBuilder().referenceFor(CONTENT_ELEMENT);
        Previews.innerHtml(content, resources.previews().rbacOverview());
    }
}
