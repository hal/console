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

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import jsinterop.annotations.JsIgnore;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.processing.SuccessfulMetadataCallback;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Footer;

/**
 * Class to create, read, update and delete complex attributes. This class mirrors some of the methods from {@link
 * CrudOperations}.
 */
public class ComplexAttributeOperations {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final StatementContext statementContext;
    private final CrudOperations crud;
    private final Resources resources;
    private final LabelBuilder labelBuilder;

    @Inject
    public ComplexAttributeOperations(final EventBus eventBus,
            final Dispatcher dispatcher,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final StatementContext statementContext,
            final CrudOperations crud,
            final Resources resources) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.statementContext = statementContext;
        this.crud = crud;
        this.resources = resources;
        this.labelBuilder = new LabelBuilder();
    }


    // ------------------------------------------------------ (u)pdate using address

    /**
     * Write the changed values to the complex attribute. After the complex attribute has been saved a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param resource         the resource name
     * @param complexAttribute the name of the complex attribute
     * @param template         the address template which is resolved against the current statement context and the
     *                         resource name to get the resource address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String resource, String complexAttribute, final AddressTemplate template,
            final Map<String, Object> changedValues, final Callback callback) {
        metadataProcessor.lookup(template, progress.get(), new SuccessfulMetadataCallback(eventBus, resources) {
            @Override
            public void onMetadata(final Metadata metadata) {
                ResourceAddress address = template.resolve(statementContext, resource);
                Metadata caMeta = metadata.forComplexAttribute(complexAttribute);
                save(complexAttribute, address, changedValues, caMeta, callback);
            }
        });
    }


    // ------------------------------------------------------ (u)pdate using address

    /**
     * Write the changed values to the complex attribute. After the complex attribute has been saved a standard success
     * message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param complexAttribute the name of the complex attribute
     * @param address          the fq address for the operation
     * @param changedValues    the changed values / payload for the operation
     * @param metadata         the metadata for the complex attribute
     * @param callback         the callback executed after the resource has been saved
     */
    @JsIgnore
    public void save(String complexAttribute, final ResourceAddress address, final Map<String, Object> changedValues,
            final Metadata metadata, final Callback callback) {
        Composite operations = operationFactory(complexAttribute).fromChangeSet(address, changedValues, metadata);
        crud.save(operations, resources.messages().modifySingleResourceSuccess(labelBuilder.label(complexAttribute)),
                callback);
    }


    // ------------------------------------------------------ helper methods

    private OperationFactory operationFactory(String complexAttribute) {
        return new OperationFactory(name -> complexAttribute + "." + name);
    }
}
