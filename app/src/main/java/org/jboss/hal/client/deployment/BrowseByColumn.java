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
package org.jboss.hal.client.deployment;

import java.util.Arrays;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static elemental2.dom.DomGlobal.document;

/** Initial column in the deployment top level tab for the domain mode. */
@Column(Ids.DEPLOYMENT_BROWSE_BY)
public class BrowseByColumn extends StaticItemColumn {

    private static class ContentRepositoryPreview extends PreviewContent<StaticItem> {

        ContentRepositoryPreview(Resources resources) {
            super(resources.constants().contentRepository(), resources.previews().deploymentsContentRepository());
        }

        @Override
        public void update(StaticItem item) {
            Elements.setVisible((HTMLElement) document.getElementById(Ids.DRAG_AND_DROP_DEPLOYMENT),
                    JsHelper.supportsAdvancedUpload());
        }
    }

    @Inject
    public BrowseByColumn(Finder finder, Resources resources) {
        super(finder, Ids.DEPLOYMENT_BROWSE_BY, resources.constants().browseBy(),
                Arrays.asList(
                        new StaticItem.Builder(resources.constants().contentRepository())
                                .onPreview(new ContentRepositoryPreview(resources))
                                .nextColumn(Ids.CONTENT)
                                .build(),
                        new StaticItem.Builder(Names.SERVER_GROUPS)
                                .onPreview(new PreviewContent<>(Names.SERVER_GROUPS,
                                        resources.previews().deploymentsServerGroups()))
                                .nextColumn(Ids.DEPLOYMENT_SERVER_GROUP)
                                .build()
                ));
    }
}
