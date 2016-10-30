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
package org.jboss.hal.core.mbui;

import java.util.Map;

import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.model.ResourceAddress;

/**
 * Base class for views generated using {@code @MbuiView}.
 *
 * @author Harald Pehl
 */
public abstract class MbuiViewImpl<P extends MbuiPresenter> extends HalViewImpl implements MbuiView<P> {

    protected final MbuiContext mbuiContext;
    protected P presenter;

    protected MbuiViewImpl(final MbuiContext mbuiContext) {
        this.mbuiContext = mbuiContext;
    }

    @Override
    public void setPresenter(final P presenter) {
        this.presenter = presenter;
    }

    protected void saveForm(final String type, final String name, final ResourceAddress address,
            final Map<String, Object> changedValues) {
        mbuiContext.crud().save(type, name, address, changedValues, () -> presenter.reload());
    }

    protected void saveSingletonForm(final String type, final ResourceAddress address,
            final Map<String, Object> changedValues) {
        mbuiContext.crud().saveSingleton(type, address, changedValues, () -> presenter.reload());
    }
}
