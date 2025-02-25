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
package org.jboss.hal.client.runtime.subsystem.resourceadapter;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.MissingMetadataException;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.resources.CSS.clearfix;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.pullRight;

public abstract class BaseView<P extends BasePresenter<?, ?>> extends HalViewImpl
        implements BasePresenter.MyView<P> {

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private P presenter;
    private HTMLElement header;
    private Form<ModelNode> poolForm;
    private Form<ModelNode> extendedForm;

    @Inject
    public BaseView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
    }

    @Override
    public void setup() {
        // The metadata for the "statistic" resources is only available for existing resource-adapters.
        // That's why we cannot set up the UI in the constructor like in other views and
        // using wildcards in the address templates. As a workaround we defer the UI setup
        // until the RA name is known and replace the wildcards with the RA name.
        Metadata poolMeta;
        Metadata extendedMeta = null;
        try {
            extendedMeta = metadataRegistry.lookup(presenter.getExtendedStatsAddress());
        } catch (MissingMetadataException mme) {
            // "extended" statistics are only present if the underlying class implements them
        }

        boolean isConnDef = presenter.getType() == StatisticsResource.ResourceType.CONNECTION_DEFINITION;
        HTMLElement extendedElement;
        HTMLElement statsElement;
        if (extendedMeta != null) {
            extendedForm = new ModelNodeForm.Builder<>(Ids.build(Ids.RESOURCE_ADAPTER_RUNTIME, "extended", Ids.FORM),
                    extendedMeta)
                    .readOnly()
                    .includeRuntime()
                    .exclude(STATISTICS_ENABLED)
                    .build();
            extendedElement = extendedForm.element();

            registerAttachable(extendedForm);
        } else {
            extendedElement = new Alert(Icons.INFO, resources.messages().noStatistics()).element();
        }

        if (!isConnDef) {
            statsElement = extendedElement;
        } else {
            poolMeta = metadataRegistry.lookup(presenter.getPoolStatsAddress());
            poolForm = new ModelNodeForm.Builder<>(Ids.build(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME, Ids.POOL, Ids.FORM), poolMeta)
                    .readOnly()
                    .includeRuntime()
                    .exclude(STATISTICS_ENABLED)
                    .build();

            Tabs tabs = new Tabs(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME_TAB_CONTAINER);
            tabs.add(Ids.build(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME, "pool", Ids.TAB), Names.POOL, poolForm.element());
            tabs.add(Ids.build(Ids.RESOURCE_ADAPTER_CHILD_RUNTIME, "extended", Ids.TAB), "Extended", extendedElement);

            statsElement = tabs.element();

            registerAttachable(poolForm);
        }

        HTMLElement root = row()
                .add(column()
                        .add(header = h(1).textContent("RA resource").element())
                        .add(extendedMeta != null ? p().css(clearfix)
                                .add(a().css(clickable, pullRight).on(click, event -> refresh())
                                        .add(span().css(fontAwesome("refresh"), marginRight5))
                                        .add(span().textContent(resources.constants().refresh())))
                                : null)
                        .add(statsElement))
                .element();

        initElement(root);
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(StatisticsResource resource) {
        header.textContent = resource.getName();

        if (resource.hasExtendedStats()) {
            extendedForm.view(resource.getExtendedStats());
        }
        if (presenter.getType() == StatisticsResource.ResourceType.CONNECTION_DEFINITION) {
            poolForm.view(resource.getPoolStats());
        }
    }

    private void refresh() {
        if (presenter != null) {
            presenter.reload();
        }
    }
}
