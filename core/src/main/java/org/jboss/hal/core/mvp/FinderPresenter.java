/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.mvp;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.HasFinder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
public abstract class FinderPresenter<V extends PatternFlyView, Proxy_ extends ProxyPlace<?>>
        extends PatternFlyPresenter<V, Proxy_>
        implements TopLevelCategory {

    protected final Finder finder;
    protected final Resources resources;
    protected String path;

    public FinderPresenter(final EventBus eventBus, final V view, final Proxy_ proxy,
            final Finder finder, final Resources resources) {
        super(eventBus, view, proxy, Slots.MAIN);
        this.finder = finder;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        if (getView() instanceof HasFinder) { ((HasFinder) getView()).setFinder(finder); }
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        path = request.getParameter("path", null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        String token = getProxy().getNameToken();
        if (path != null) {
            finder.select(token, FinderPath.from(path),
                    () -> finder.reset(token, initialColumn(), initialPreview()));
        } else {
            finder.reset(token, initialColumn(), initialPreview());
        }
    }

    protected abstract String initialColumn();

    protected abstract PreviewContent initialPreview();
}
