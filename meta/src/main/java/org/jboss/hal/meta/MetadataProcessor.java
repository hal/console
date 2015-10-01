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
package org.jboss.hal.meta;

import com.google.common.collect.FluentIterable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.meta.functions.MetadataContext;
import org.jboss.hal.meta.functions.MetadataFunctions;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.spi.Footer;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Harald Pehl
 */
public class MetadataProcessor {

    private final RequiredResources rrr;
    private final MetadataFunctions functions;
    private final Progress progress;

    @Inject
    public MetadataProcessor(final RequiredResources rrr, final MetadataFunctions functions,
            @Footer Progress progress) {
        this.rrr = rrr;
        this.functions = functions;
        this.progress = progress;
    }

    public void process(String token, final AsyncCallback<Void> callback) {
        Set<String> resources = rrr.getResources(token);
        if (resources.isEmpty()) {
            callback.onSuccess(null);

        } else {
            Set<AddressTemplate> templates = FluentIterable.from(resources).transform(AddressTemplate::of).toSet();
            MetadataContext metadataContext = new MetadataContext(templates, rrr.isRecursive(token));
            Outcome<MetadataContext> outcome = new Outcome<MetadataContext>() {
                @Override
                public void onFailure(final MetadataContext context) {
                    callback.onFailure(context.getError());
                }

                @Override
                public void onSuccess(final MetadataContext context) {
                    callback.onSuccess(null);
                }
            };
            new Async<MetadataContext>(progress).waterfall(metadataContext, outcome, functions.functions());
        }
    }
}
