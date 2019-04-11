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
package org.jboss.hal.client.runtime.subsystem.undertow;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Column(Ids.UNDERTOW_RUNTIME)
public class UndertowColumn extends FinderColumn<StaticItem> {

    @Inject
    public UndertowColumn(Finder finder, Resources resources) {

        super(new Builder<StaticItem>(finder, Ids.UNDERTOW_RUNTIME, Names.WEB)

                .itemRenderer(StaticItemColumn.StaticItemDisplay::new)
                .onPreview(StaticItem::getPreviewContent)
                .useFirstActionAsBreadcrumbHandler());

        List<StaticItem> items = asList(
                new StaticItem.Builder(Names.APPLICATION_SECURITY_DOMAIN)
                        .onPreview(new PreviewContent<>(Names.APPLICATION_SECURITY_DOMAIN,
                                resources.previews().runtimeApplicationSecurity()))
                        .nextColumn(Ids.UNDERTOW_RUNTIME_APP_SEC_DOMAIN)
                        .build(),
                new StaticItem.Builder(Names.DEPLOYMENT)
                        .nextColumn(Ids.UNDERTOW_RUNTIME_DEPLOYMENT)
                        .onPreview(new PreviewContent<>(Names.DEPLOYMENT, resources.previews().runtimeDeployment()))
                        .build(),
                new StaticItem.Builder(Names.MODCLUSTER)
                        .nextColumn(Ids.UNDERTOW_RUNTIME_MODCLUSTER)
                        .onPreview(new PreviewContent<>(Names.MODCLUSTER, resources.previews().runtimeModCluster()))
                        .build(),
                new StaticItem.Builder(Names.SERVER)
                        .nextColumn(Ids.UNDERTOW_RUNTIME_SERVER)
                        .onPreview(new PreviewContent<>(Names.SERVER, resources.previews().runtimeUndertowServer()))
                        .build()

        );
        setItemsProvider((context, callback) -> callback.onSuccess(items));
        setBreadcrumbItemsProvider(
                (context, callback) -> callback.onSuccess(
                        items.stream().filter(item -> item.getNextColumn() == null).collect(toList())));

    }
}
