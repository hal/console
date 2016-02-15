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
package org.jboss.hal.client.configuration;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.HasFinder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.FinderPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class ConfigurationPresenter
        extends FinderPresenter<ConfigurationPresenter.MyView, ConfigurationPresenter.MyProxy> {

    // @formatter:off
    @ProxyStandard
    @NameToken(NameTokens.CONFIGURATION)
    public interface MyProxy extends ProxyPlace<ConfigurationPresenter> {}

    public interface MyView extends PatternFlyView, HasFinder {}
    // @formatter:on


    private final Environment environment;

    @Inject
    public ConfigurationPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Resources resources,
            final Environment environment) {
        super(eventBus, view, proxy, finder, resources);
        this.environment = environment;
    }

    @Override
    protected String initialColumn() {
        return Ids.CONFIGURATION_COLUMN;
    }

    @Override
    protected PreviewContent initialPreview() {
        return new PreviewContent(Names.CONFIGURATION,
                environment.isStandalone() ?
                        resources.previews().configurationStandalone() :
                        resources.previews().configurationDomain());
    }
}
