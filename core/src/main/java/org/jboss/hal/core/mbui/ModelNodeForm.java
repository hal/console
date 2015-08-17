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

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.DefaultStateMachine;
import org.jboss.hal.ballroom.form.EditOnlyStateMachine;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemFactory;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceDescription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;

/**
 * @author Harald Pehl
 */
public class ModelNodeForm extends AbstractForm<ModelNode> {

    public static class Builder {

        private final String id;
        private final ResourceDescription resourceDescription;
        private final Set<String> includes;
        private final Set<String> excludes;
        private final Map<String, FormItemFactory> factories;
        private final Map<String, SaveOperationStep> saveOperations;
        private boolean createResource;
        private boolean viewOnly;
        private boolean editOnly;
        private boolean includeRuntime;
        private SaveCallback<ModelNode> saveCallback;
        private CancelCallback<ModelNode> cancelCallback;
        private ResetCallback<ModelNode> resetCallback;

        public Builder(final String id, final ResourceDescription resourceDescription) {
            this.id = id;
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

        public ModelNodeForm build() {
            return new ModelNodeForm(this);
        }
    }


    private final Map<String, SaveOperationStep> saveOperations;
    private final ResourceDescriptionReflection reflection;


    ModelNodeForm(Builder builder) {
        super(builder.id, builder.createResource || builder.editOnly ?
                new EditOnlyStateMachine() :
                (builder.viewOnly ? new ViewOnlyStateMachine() : new DefaultStateMachine()));

        if (builder.createResource && builder.viewOnly) {
            throw new IllegalStateException("Illegal flag combination in " + formId() + ": createResource && viewOnly");
        }
        if (builder.viewOnly && builder.editOnly) {
            throw new IllegalStateException("Illegal flag combination in " + formId() + ": viewOnly && editOnly");
        }
        if (builder.createResource &&
                !builder.resourceDescription.hasDefined(REQUEST_PROPERTIES)) {
            throw new IllegalStateException("No request properties found for " + formId() +
                    " using resource description " + builder.resourceDescription);
        }
        if (!builder.createResource &&
                !builder.resourceDescription.hasDefined(ATTRIBUTES)) {
            throw new IllegalStateException("No attributes found for " + formId() +
                    " using resource description " + builder.resourceDescription);
        }

        this.saveOperations = builder.saveOperations;
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.resetCallback = builder.resetCallback;

        this.reflection = new ResourceDescriptionReflection(builder.resourceDescription,
                builder.includes, builder.excludes, builder.includeRuntime, builder.factories);
        for (FormItem formItem : reflection.getFormItems().values()) {
            addFormItem(formItem);
        }
        for (Map.Entry<String, String> entry : reflection.getHelpTexts().entrySet()) {
            addHelp(entry.getKey(), entry.getValue());
        }
    }
}
