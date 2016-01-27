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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.PreBootstrapper;
import elemental.client.Browser;
import org.jboss.hal.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("unused")
public class HalPreBootstrapper implements PreBootstrapper {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Logger logger = LoggerFactory.getLogger(HalPreBootstrapper.class);

    @Override
    public void onPreBootstrap() {
        GWT.setUncaughtExceptionHandler(e -> {
            //noinspection HardCodedStringLiteral
            logger.error("Bootstrap error in {}: {}", HalPreBootstrapper.class.getSimpleName(), e.getMessage());
            Browser.getDocument().getBody().appendChild(
                    BootstrapFailed.create(CONSTANTS.bootstrapException(), e.getMessage()).asElement());
        });
    }
}
