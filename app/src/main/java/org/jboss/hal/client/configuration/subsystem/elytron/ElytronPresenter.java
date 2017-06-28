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

package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.Map;
import java.util.function.Function;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.spi.Callback;

/**
 * Constains the standard methods to be used from the ResourceView class, it must be implemented in the presenter
 * classes.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public interface ElytronPresenter {


    /**
     * Write the changed values to the specified resource. After the resource has been saved a standard success message
     * is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param title         the human readable resource type used in the success message
     * @param name          the resource name
     * @param changedValues the changed values / payload for the operation
     * @param metadata      the metadata of the attributes in the change set
     */
    void saveForm(final String title, final String name, final Map<String, Object> changedValues,
            final Metadata metadata);

    /**
     * Write the changed values to the specified resource in the complex attribute. After the resource has been saved a
     * standard success message is fired and the specified callback is executed.
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param title                the human readable resource type used in the success message
     * @param name                 the resource name
     * @param complexAttributeName the complex attribute name
     * @param changedValues        the changed values / payload for the operation
     * @param metadata             the metadata of the attributes in the change set
     */
    void saveComplexForm(final String title, final String name, String complexAttributeName,
            final Map<String, Object> changedValues, final Metadata metadata);

    /**
     * Write the changed values to the specified index of the complex attribute of type LIST. After the resource has
     * been saved a standard success message is fired and the specified callback is executed.
     * <p>
     * It mimics write-attribute(complex-attribute[index].attribute, value=some value)
     * <p>
     * If the change set is empty, a warning message is fired and the specified callback is executed.
     *
     * @param resource          the resource name
     * @param listAttributeName The complex attribute name of type LIST
     * @param metadata          the metadata of the attributes in the change set
     * @param payload           The model representing the loaded form, is used only to retrieve the index number
     * @param changedValues     the changed values / payload for the operation
     */
    void saveFormPage(String resource, String listAttributeName, Metadata metadata, NamedNode payload,
            Map<String, Object> changedValues);


    /**
     * Shows a confirmation dialog and removes the object item from the complex attribute of type LIST, represented by
     * its index. After the item has been removed a success message is fired and the specified callback is executed.
     *
     * @param title                 the human readable resource type used in the success message
     * @param resourceName                 the resource name
     * @param complexAttributeName the complex attribute name
     * @param index                the list index to remove the item
     * @param template  The template of the specified resource
     */
    void listRemove(String title, String resourceName, String complexAttributeName, int index,
            AddressTemplate template);

    /**
     * Undefines the complex attribute of the mentioned resource. After the resource has been undefined a standard
     * success message is fired and the specified callback is executed.
     * <p>
     * If the set contains only required attributes, a warning message is fired and the specified callback is executed.
     *
     * @param type      The user friendly resource name
     * @param name      The resource name
     * @param attribute The complex attribute name
     * @param metadata  The resource's metadata
     * @param callback
     */
    void resetComplexAttribute(String type, String name, String attribute, Metadata metadata, Callback callback);

    /**
     * Reload the all resources related to this view.
     */
    void reload();

    /**
     * Constructs an AddDialog for the specified metadata description, it is used to add new objects to a list of
     * a complex object of type LIST.
     *
     * @param resourceNameFunction  The function to return the resource name
     * @param complexAttributeName
     * @param metadata
     * @param title
     */
    void launchAddDialog(Function<String, String> resourceNameFunction, String complexAttributeName, Metadata metadata,
            String title);
}
