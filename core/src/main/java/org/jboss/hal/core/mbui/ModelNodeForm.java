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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceDescription;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.ballroom.form.Form.State.EDIT;
import static org.jboss.hal.ballroom.form.Form.State.VIEW;

/**
 * @author Harald Pehl
 */
public class ModelNodeForm extends AbstractForm<ModelNode> {

    public static class Builder {

        private final String id;
        private final ResourceDescription resourceDescription;
        private final EnumSet<Form.State> supportedStates;
        private final Set<String> includes;
        private final Set<String> excludes;
        private final Map<String, ModelNodeFormItemFactory> factories;
        private final Map<String, SaveOperationProvider> saveOperations;
        private SaveCallback<ModelNode> saveCallback;
        private CancelCallback<ModelNode> cancelCallback;
        private UndefineCallback<ModelNode> undefineCallback;

        public Builder(final String id, final ResourceDescription resourceDescription) {
            this.id = id;
            this.resourceDescription = resourceDescription;
            this.supportedStates = EnumSet.of(VIEW, EDIT);
            this.includes = new HashSet<>();
            this.excludes = new HashSet<>();
            this.factories = new HashMap<>();
            this.saveOperations = new HashMap<>();
        }

        public Builder support(final State first, final State... rest) {
            supportedStates.clear();
            supportedStates.addAll(EnumSet.of(first, rest));
            return this;
        }

        public Builder include(final String first, final String... rest) {
            includes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder exclude(final String first, final String... rest) {
            excludes.addAll(Lists.asList(first, rest));
            return this;
        }

        public Builder customFormItem(final String attribute, final ModelNodeFormItemFactory factory) {
            factories.put(attribute, factory);
            return this;
        }

        public Builder onSave(final String attribute, final SaveOperationProvider saveOperation) {
            saveOperations.put(attribute, saveOperation);
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

        public Builder onUndefine(final UndefineCallback<ModelNode> undefineCallback) {
            this.undefineCallback = undefineCallback;
            return this;
        }

        public ModelNodeForm build() {
            return new ModelNodeForm(this);
        }
    }


    private final Map<String, ModelNodeFormItemFactory> factories;
    private final Map<String, SaveOperationProvider> saveOperations;
    private final ResourceDescriptionReflection reflection;


    ModelNodeForm(Builder builder) {
        super(builder.id, builder.supportedStates);

        this.factories = builder.factories;
        this.saveOperations = builder.saveOperations;
        this.saveCallback = builder.saveCallback;
        this.cancelCallback = builder.cancelCallback;
        this.undefineCallback = builder.undefineCallback;

        this.reflection = new ResourceDescriptionReflection(builder.resourceDescription,
                builder.includes, builder.excludes, factories);
        for (FormItem formItem : reflection.getFormItems()) {
            addFormItem(formItem);
        }
        for (Map.Entry<String, String> entry : reflection.getHelpTexts().entrySet()) {
            addHelp(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void updateModel(final Map<String, Object> changedValues) {
        ModelNode model = getModel();
    }

    @Override
    public ModelNode newModel() {
        return new ModelNode();
    }

    @Override
    public Map<String, Object> getChangedValues() {
        return new HashMap<>();
    }
}
