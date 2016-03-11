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
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import javax.inject.Inject;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.DATA_SOURCE_TYPE_COLUMN)
public class DataSourceTypeColumn extends StaticItemColumn {

    static final String NON_XA = "non-xa";
    static final String XA = "xa";

    @Inject
    public DataSourceTypeColumn(final Finder finder,
            final Resources resources) {
        super(finder, Ids.DATA_SOURCE_TYPE_COLUMN, resources.constants().type(), asList(

                new StaticItem.Builder("Non-XA") //NON-NLS
                        .id(NON_XA)
                        .nextColumn(ModelDescriptionConstants.DATA_SOURCE)
                        .onPreview(new PreviewContent("Non-XA Datasource", resources.previews().nonXa())) //NON-NLS
                        .build(),

                new StaticItem.Builder("XA") //NON-NLS
                        .id(XA)
                        .nextColumn(ModelDescriptionConstants.DATA_SOURCE) // re-use the same column
                        .onPreview(new PreviewContent("XA Datasource", resources.previews().xa())) //NON-NLS
                        .build()));
    }
}
