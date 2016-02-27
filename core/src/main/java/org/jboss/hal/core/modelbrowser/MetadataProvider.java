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
package org.jboss.hal.core.modelbrowser;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.resources.Ids;

import javax.inject.Provider;
import java.util.Iterator;

import static java.util.Collections.singleton;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.meta.StatementContext.Key.ANY_GROUP;
import static org.jboss.hal.meta.StatementContext.Key.ANY_PROFILE;

/**
 * @author Harald Pehl
 */
class MetadataProvider {

    interface MetadataCallback {

        void onMetadata(SecurityContext securityContext, ResourceDescription description);

        void onError(Throwable error);
    }


    private final MetadataProcessor metadataProcessor;
    private final SecurityFramework securityFramework;
    private final ResourceDescriptions resourceDescriptions;
    private final Provider<Progress> progress;

    MetadataProvider(final MetadataProcessor metadataProcessor,
            final SecurityFramework securityFramework,
            final ResourceDescriptions resourceDescriptions,
            final Provider<Progress> progress) {
        this.metadataProcessor = metadataProcessor;
        this.securityFramework = securityFramework;
        this.resourceDescriptions = resourceDescriptions;
        this.progress = progress;
    }

    void getMetadata(Node<Context> node, ResourceAddress address, MetadataCallback callback) {
        AddressTemplate template = asGenericTemplate(node, address);
        metadataProcessor.process(Ids.MODEL_BROWSER, singleton(template), progress,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(final Throwable throwable) {
                        callback.onError(throwable);
                    }

                    @Override
                    public void onSuccess(final Void aVoid) {
                        SecurityContext securityContext = securityFramework.lookup(template);
                        ResourceDescription description = resourceDescriptions.lookup(template);
                        callback.onMetadata(securityContext, description);
                    }
                });
    }

    private AddressTemplate asGenericTemplate(Node<Context> node, ResourceAddress address) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<Property> iterator = address.asPropertyList().iterator(); iterator.hasNext(); ) {
            Property property = iterator.next();
            String name = property.getName();

            if (PROFILE.equals(name)) {
                builder.append(ANY_PROFILE.variable());
            } else if (SERVER_GROUP.equals(name)) {
                builder.append(ANY_GROUP.variable());
            } else {
                builder.append(name).append("=");
                if (!iterator.hasNext() && node != null && node.data != null && !node.data.hasSingletons()) {
                    builder.append("*");
                } else {
                    builder.append(property.getValue().asString());
                }
            }
            if (iterator.hasNext()) {
                builder.append("/");
            }
        }
        return AddressTemplate.of(builder.toString());
    }
}
