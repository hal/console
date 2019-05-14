/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.accesscontrol;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import com.google.gwt.resources.client.ExternalTextResource;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.setVisible;

@AsyncColumn(Ids.ACCESS_CONTROL_BROWSE_BY)
public class BrowseByColumn extends StaticItemColumn {

    private static class TopLevelPreview extends PreviewContent<StaticItem> {

        private final Environment environment;
        private final Alert warning;

        TopLevelPreview(String header, ExternalTextResource resource,
                AccessControl accessControl, Environment environment, Resources resources) {
            super(header);
            this.environment = environment;
            this.warning = new Alert(Icons.WARNING, resources.messages().simpleProviderWarning(),
                    resources.constants().enableRbac(),
                    event -> accessControl.switchProvider());

            previewBuilder().add(warning);
            HTMLElement content = div().get();
            Previews.innerHtml(content, resource);
            previewBuilder().add(content);
        }

        @Override
        public void update(StaticItem item) {
            setVisible(warning.element(), environment.getAccessControlProvider() == AccessControlProvider.SIMPLE);
        }
    }


    @Inject
    public BrowseByColumn(Finder finder, AccessControl accessControl, Environment environment,
            Resources resources) {
        super(finder, Ids.ACCESS_CONTROL_BROWSE_BY, resources.constants().browseBy(),

                // if Keycloak-SSO is enabled, the user management is performed in keycloak server,
                // so we need to disable it in widfly
                // this view is not displayed when user clicks on "access contol" top level menu
                // but is accessible if user types the named token in the url
                environment.isSingleSignOn() ? Collections.emptyList() :
                        Arrays.asList(
                                new StaticItem.Builder(resources.constants().users())
                                        .id(Ids.ACCESS_CONTROL_BROWSE_BY_USERS)
                                        .onPreview(new TopLevelPreview(resources.constants().users(),
                                                resources.previews().rbacUsers(), accessControl, environment,
                                                resources))
                                        .nextColumn(Ids.USER)
                                        .build(),
                                new StaticItem.Builder(resources.constants().groups())
                                        .id(Ids.ACCESS_CONTROL_BROWSE_BY_GROUPS)
                                        .onPreview(new TopLevelPreview(resources.constants().groups(),
                                                resources.previews().rbacGroups(), accessControl, environment,
                                                resources))
                                        .nextColumn(Ids.GROUP)
                                        .build(),
                                new StaticItem.Builder(resources.constants().roles())
                                        .id(Ids.ACCESS_CONTROL_BROWSE_BY_ROLES)
                                        .onPreview(new TopLevelPreview(resources.constants().roles(),
                                                environment.isStandalone() ? resources.previews()
                                                        .rbacRolesStandalone() : resources.previews().rbacRolesDomain(),
                                                accessControl, environment, resources))
                                        .nextColumn(Ids.ROLE)
                                        .build()
                        ));
    }
}
