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
package org.jboss.hal.core.mbui;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.DefaultStateMachine;
import org.jboss.hal.ballroom.form.EditOnlyStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemFactory;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.ResourceDescription;
import org.jboss.hal.security.SecurityContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class ModelNodeForm extends AbstractForm<ModelNode> {

    public static class Builder {

        final String id;
        final SecurityContext securityContext;
        final ResourceDescription resourceDescription;
        final Set<String> includes;
        final Set<String> excludes;
        final Map<String, FormItemFactory> factories;
        final Map<String, SaveOperationStep> saveOperations;
        boolean createResource;
        boolean viewOnly;
        boolean editOnly;
        boolean includeRuntime;
        SaveCallback<ModelNode> saveCallback;
        CancelCallback<ModelNode> cancelCallback;
        ResetCallback<ModelNode> resetCallback;


        // ------------------------------------------------------ configure required and optional settings

        public Builder(final String id, final SecurityContext securityContext,
                final ResourceDescription resourceDescription) {
            this.id = id;
            this.securityContext = securityContext;
            this.resourceDescription = resourceDescription;
            this.includes = new HashSet<>();
            this.excludes = new HashSet<>();
            this.factories = new HashMap<>();
            this.saveOperations = new HashMap<>();
            this.createResource = false;
            this.viewOnly = false;
            this.editOnly = false;
            this.includeRuntime = false;
        }

        public Builder include(final String first, final String... rest) {
            includes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder exclude(final String first, final String... rest) {
            excludes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder createResource() {
            this.createResource = true;
            return this;
        }

        public Builder viewOnly() {
            this.viewOnly = true;
            return this;
        }

        public Builder editOnly() {
            this.editOnly = true;
            return this;
        }

        public Builder includeRuntime() {
            this.includeRuntime = true;
            return this;
        }

        public Builder customFormItem(final String attribute, final FormItemFactory factory) {
            return customFormItem(attribute, factory, null);
        }

        public Builder customFormItem(final String attribute, final FormItemFactory factory,
                final SaveOperationStep saveOperation) {
            factories.put(attribute, factory);
            if (saveOperation != null) {
                saveOperations.put(attribute, saveOperation);
            }
            return this;
        }

        public Builder onSave(final SaveCallback<ModelNode> saveCallback) {
            this.saveCallback = saveCallback;
            return this;
        }

        public Builder onCancel(final CancelCallback<ModelNode> cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder onReset(final ResetCallback<ModelNode> resetCallback) {
            this.resetCallback = resetCallback;
            return this;
        }

        // ------------------------------------------------------ build

        public ModelNodeForm build() {
            validate();
            return new ModelNodeForm(this);
        }

        void validate() {
            if (viewOnly && editOnly) {
                throw new IllegalStateException("Illegal combination for " + formId() + ": viewOnly && editOnly");
            }
            if (createResource) {
                if (viewOnly) {
                    throw new IllegalStateException("Illegal combination for " + formId() + ": createResource && viewOnly");
                }
                if (!resourceDescription.hasDefined(OPERATIONS) ||
                        !resourceDescription.get(OPERATIONS).hasDefined(ADD) ||
                        !resourceDescription.get(OPERATIONS).get(ADD).hasDefined(REQUEST_PROPERTIES)) {
                    throw new IllegalStateException("No request properties found for " + formId() +
                            " / operation add in resource description " + resourceDescription);
                }
            } else {
                if (!resourceDescription.hasDefined(ATTRIBUTES)) {
                    throw new IllegalStateException("No attributes found for " + formId() +
                            " in resource description " + resourceDescription);
                }
            }
        }

        StateMachine stateMachine() {
            return createResource || editOnly ?
                    new EditOnlyStateMachine() :
                    (viewOnly ? new ViewOnlyStateMachine() : new DefaultStateMachine());
        }

        private String formId() {
            return "form(" + id + ")";
        }
    }


    private final FormItemFactory defaultFormItemFactory;
    private final Map<String, SaveOperationStep> saveOperations;

    ModelNodeForm(final Builder builder) {
        super(builder.id, builder.stateMachine(), builder.securityContext);

        this.defaultFormItemFactory = new DefaultFormItemFactory();
        this.saveOperations = builder.saveOperations;
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.resetCallback = builder.resetCallback;

        ImmutableSortedSet<Property> properties = new PropertyFilter(builder).filter();
        for (Property property : properties) {
            String name = property.getName();
            ModelNode attribute = property.getValue();

            FormItem formItem;
            if (builder.factories.containsKey(name)) {
                formItem = builder.factories.get(name).createFrom(attribute);
            } else {
                formItem = defaultFormItemFactory.createFrom(attribute);
            }
            addFormItem(formItem);
            // TODO addHelp(name, description);
        }
    }
}
