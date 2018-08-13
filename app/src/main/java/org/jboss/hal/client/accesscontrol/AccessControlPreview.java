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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.section;

class AccessControlPreview extends PreviewContent<Void> {

    private final Environment environment;
    private final Alert warning;
    private final Alert warningSso;

    AccessControlPreview(AccessControl accessControl, Environment environment, Resources resources) {
        super(Names.ACCESS_CONTROL);
        this.environment = environment;
        this.warning = new Alert(Icons.WARNING, resources.messages().simpleProviderWarning(),
                resources.constants().enableRbac(),
                event -> accessControl.switchProvider());

        this.warningSso = new Alert(Icons.WARNING, resources.messages().ssoAccessControlWarning());

        HTMLElement content;
        previewBuilder().add(warning);
        previewBuilder().add(warningSso);
        previewBuilder().add(content = section().asElement());
        Previews.innerHtml(content, resources.previews().rbacOverview());
        update(null);
    }

    @Override
    public void update(final Void item) {
        Elements.setVisible(warning.asElement(),
                environment.getAccessControlProvider() == AccessControlProvider.SIMPLE);
        Elements.setVisible(warningSso.asElement(), environment.isSingleSignOn());
    }
}
