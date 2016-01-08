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
package org.jboss.hal.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.ui.UIRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.GinModule;

@GinModule
public class CoreModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(StatementContext.class).to(CoreStatementContext.class).asEagerSingleton(); // to register the event
        bind(UIRegistry.class).in(Singleton.class);
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
