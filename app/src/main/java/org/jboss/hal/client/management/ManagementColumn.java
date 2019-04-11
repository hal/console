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
package org.jboss.hal.client.management;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Arrays.asList;

@Column(Ids.MANAGEMENT)
public class ManagementColumn extends StaticItemColumn {

    @Inject
    public ManagementColumn(Finder finder, Resources resources) {

        super(finder, Ids.MANAGEMENT, Names.MANAGEMENT, asList(

                new StaticItem.Builder(Names.EXTENSIONS)
                        .nextColumn(Ids.EXTENSION)
                        .onPreview(new PreviewContent<>(Names.EXTENSIONS, resources.previews().managementExtensions()))
                        .build()
        ));
    }
}
