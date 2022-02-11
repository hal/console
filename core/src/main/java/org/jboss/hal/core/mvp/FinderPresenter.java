/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mvp;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/** A presenter which contains a finder. Should only be used as base class for the top level categories. */
public abstract class FinderPresenter<V extends FinderView, Proxy_ extends ProxyPlace<?>>
        extends TopLevelPresenter<V, Proxy_>
        implements TopLevelCategory {

    protected final Finder finder;
    protected final Resources resources;
    protected String path;

    public FinderPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final Finder finder, final Resources resources) {
        super(eventBus, view, proxy);
        this.finder = finder;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setFinder(finder);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        path = request.getParameter(FinderContext.PATH_PARAM, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        AsyncCallback<FinderColumn> callback = new AsyncCallback<FinderColumn>() {
            @Override
            public void onFailure(final Throwable caught) {
                MessageEvent
                        .fire(getEventBus(), Message.error(resources.messages().unknownError(), caught.getMessage()));
            }

            @Override
            public void onSuccess(final FinderColumn result) {
                // nop
            }
        };

        String token = getProxy().getNameToken();
        if (Strings.isNullOrEmpty(path)) {
            finder.reset(token, initialColumn(), initialPreview(), callback);
        } else {
            finder.select(token, FinderPath.from(path),
                    () -> finder.reset(token, initialColumn(), initialPreview(), callback));
        }
    }

    protected abstract String initialColumn();

    protected abstract PreviewContent initialPreview();
}
