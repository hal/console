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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.List;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class BufferCacheView extends MbuiViewImpl<BufferCachePresenter> implements BufferCachePresenter.MyView {

    // ------------------------------------------------------ initialization

    public static BufferCacheView create(final MbuiContext mbuiContext) {
        return new Mbui_BufferCacheView(mbuiContext);
    }

    @MbuiElement("buffer-cache-table") NamedNodeTable<NamedNode> table;
    @MbuiElement("buffer-cache-form") Form<NamedNode> form;

    BufferCacheView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    // ------------------------------------------------------ scanners

    @Override
    public void update(final List<NamedNode> items) {
        form.clear();
        table.update(items);
    }

}
