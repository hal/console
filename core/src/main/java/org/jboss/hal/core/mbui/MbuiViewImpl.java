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
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

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

    protected void saveSingletonForm(final Map<String, Object> changedValues, final ResourceAddress address,
            final String type) {
        Composite composite = mbuiContext.operationFactory().fromChangeSet(address, changedValues);
        mbuiContext.dispatcher().execute(composite, (CompositeResult result) -> {
            presenter.reload();
            MessageEvent.fire(mbuiContext.eventBus(),
                    Message.success(mbuiContext.resources().messages().modifySingleResourceSuccess(type)));
        });
    }

    protected void saveForm(final Map<String, Object> changedValues, final ResourceAddress address,
            final String type, final String name) {
        Composite composite = mbuiContext.operationFactory().fromChangeSet(address, changedValues);
        mbuiContext.dispatcher().execute(composite, (CompositeResult result) -> {
            presenter.reload();
            MessageEvent.fire(mbuiContext.eventBus(),
                    Message.success(mbuiContext.resources().messages().modifyResourceSuccess(type, name)));
        });
    }
}
