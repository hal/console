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
package org.jboss.hal.resources;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ExternalTextResource;

/**
 * @author Harald Pehl
 */
public interface Previews extends ClientBundleWithLookup {

    @Source("previews/configuration-domain.html")
    ExternalTextResource configurationDomain();

    @Source("previews/configuration-standalone.html")
    ExternalTextResource configurationStandalone();

    @Source("previews/subsystems/datasources.html")
    ExternalTextResource datasources();

    @Source("previews/deployments.html")
    ExternalTextResource deployments();

    @Source("previews/interfaces.html")
    ExternalTextResource interfaces();

    @Source("previews/subsystems/non-xa.html")
    ExternalTextResource nonXa();

    @Source("previews/paths.html")
    ExternalTextResource paths();

    @Source("previews/profiles.html")
    ExternalTextResource profiles();

    @Source("previews/runtime-domain.html")
    ExternalTextResource runtimeDomain();

    @Source("previews/runtime-standalone.html")
    ExternalTextResource runtimeStandalone();

    @Source("previews/socket-bindings.html")
    ExternalTextResource socketBindings();

    @Source("previews/subsystems.html")
    ExternalTextResource subsystems();

    @Source("previews/system-properties.html")
    ExternalTextResource systemProperties();

    @Source("previews/subsystems/xa.html")
    ExternalTextResource xa();
}
