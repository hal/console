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
package org.jboss.hal.meta.processing;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.resource.RequiredResources;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.spi.Footer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Harald Pehl
 */
public class MetadataProcessor {

    /**
     * Number of r-r-d operations part of one composite operation.
     */
    final static int BATCH_SIZE = 3;

    private final RequiredResources requiredResources;
    private final Lookup lookup;
    private final CreateRrdOps rrdOps;
    private final Progress progress;

    @Inject
    public MetadataProcessor(final StatementContext statementContext,
            final RequiredResources requiredResources,
            final ResourceDescriptions descriptionRegistry,
            final SecurityFramework securityFramework,
            final @Footer Progress progress) {
        this.requiredResources = requiredResources;
        this.lookup = new Lookup(descriptionRegistry, securityFramework);
        this.rrdOps = new CreateRrdOps(statementContext);
        this.progress = progress;
    }

    public void process(String token, final AsyncCallback<Void> callback) {
        Set<String> resources = requiredResources.getResources(token);
        if (resources.isEmpty()) {
            callback.onSuccess(null);

        } else {
            Set<AddressTemplate> templates = FluentIterable.from(resources).transform(AddressTemplate::of).toSet();
            LookupResult lookupResult = lookup.check(templates, requiredResources.isRecursive(token));
            List<Operation> operations = rrdOps.create(lookupResult);
            List<List<Operation>> piles = Lists.partition(operations, BATCH_SIZE);
            List<Composite> composites = Lists.transform(piles, Composite::new);
            List<Function> functions = new ArrayList<>();

            Outcome<LookupResult> outcome = new Outcome<LookupResult>() {
                @Override
                public void onFailure(final LookupResult context) {
                    callback.onFailure(context.getError());
                }

                @Override
                public void onSuccess(final LookupResult context) {
                    callback.onSuccess(null);
                }
            };
            new Async<LookupResult>(progress).waterfall(lookupResult, outcome, functions.toArray(new Function[0]));
        }
    }
}
