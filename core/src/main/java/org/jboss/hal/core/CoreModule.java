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
package org.jboss.hal.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.ColumnRegistry;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroupActions;
import org.jboss.hal.core.runtime.host.HostActions;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.subsystem.Subsystems;
import org.jboss.hal.core.ui.UIRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.GinModule;

@GinModule
public class CoreModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(ColumnRegistry.class).in(Singleton.class);
        bind(ColumnActionFactory.class).in(Singleton.class);
        bind(Finder.class).in(Singleton.class);
        bind(FinderPathFactory.class).in(Singleton.class);
        bind(HostActions.class).in(Singleton.class);
        bind(ItemMonitor.class).in(Singleton.class);
        bind(ItemActionFactory.class).in(Singleton.class);
        bind(MbuiContext.class).in(Singleton.class);
        bind(ModelBrowser.class);
        bind(Places.class).in(Singleton.class);
        bind(ServerActions.class).in(Singleton.class);
        bind(ServerGroupActions.class).in(Singleton.class);
        bind(StatementContext.class).to(CoreStatementContext.class).asEagerSingleton(); // to register the event handler
        bind(Subsystems.class).in(Singleton.class);
        bind(TableButtonFactory.class).in(Singleton.class);
        bind(UIRegistry.class).in(Singleton.class);

        requestStaticInjection(CoreStatementContext.class);
    }

    /**
     * Convenience provider to make the global {@link Progress} implementation in HAL's footer injectable. Please use
     * the qualifier {@code @Footer} for injections.
     */
    @Provides
    @Footer
    public Progress provideProgress(UIRegistry uiRegistry) {
        return uiRegistry.getProgress();
    }
}
