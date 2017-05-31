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
package org.jboss.hal.ballroom.form;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import elemental2.dom.HTMLLabelElement;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.resources.CSS.deprecated;

/**
 * Abstract appearance with a set of supported decorations. Puts guards around {@link #apply(Decoration, Object)} and
 * {@link #unapply(Decoration)}. The guards check if the decoration is supported and not already applied resp. has been
 * applied.
 *
 * @author Harald Pehl
 */
abstract class AbstractAppearance<T> implements Appearance<T> {

    static final String LABEL_ELEMENT = "labelElement";
    static final String INPUT_CONTAINER = "inputContainer";
    static final Messages MESSAGES = GWT.create(Messages.class);
    protected static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Set<Decoration> supportedDecorations;
    private final Set<Decoration> appliedDecorations;
    String id;
    String label;
    HTMLLabelElement labelElement;

    AbstractAppearance(final Set<Decoration> supportedDecorations) {
        this.supportedDecorations = new HashSet<>(supportedDecorations);
        this.appliedDecorations = new HashSet<>();
        applyDefaults(appliedDecorations);
    }

    protected void applyDefaults(final Set<Decoration> appliedDecorations) {
        // all appearances are enabled by default
        appliedDecorations.add(Decoration.ENABLED);
    }

    /**
     * Calls {@link #safeApply(Decoration, Object)} if the decoration is supported and has not been applied yet.
     */
    @Override
    public final <C> void apply(final Decoration decoration, final C context) {
        if (supportedDecorations.contains(decoration) && !appliedDecorations.contains(decoration)) {
            safeApply(decoration, context);
            appliedDecorations.add(decoration);
        }
    }

    /**
     * Calls {@link #safeUnapply(Decoration)} if the decoration is supported and has been applied.
     */
    @Override
    public final void unapply(final Decoration decoration) {
        if (supportedDecorations.contains(decoration) && appliedDecorations.contains(decoration)) {
            safeUnapply(decoration);
            appliedDecorations.remove(decoration);
        }
    }

    boolean isApplied(Decoration decoration) {
        return appliedDecorations.contains(decoration);
    }

    /**
     * Safely applies a decoration. Only called if the appearance supports the decoration and the decoration has not
     * been applied to the appearance.
     */
    abstract <C> void safeApply(Decoration decoration, C context);

    /**
     * Safely unapplies a decoration. Only called if the appearance supports the decoration and the decoration has been
     * applied to the appearance.
     */
    abstract void safeUnapply(Decoration decoration);

    @NonNls
    protected abstract String name();


    // ------------------------------------------------------ common behaviour

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setLabel(final String label) {
        this.label = label;
        labelElement.title = label;
        labelElement.textContent = label;
    }

    void markAsDeprecated(Deprecation deprecation) {
        labelElement.title = MESSAGES.deprecated(deprecation.getSince(), deprecation.getReason());
        labelElement.classList.add(deprecated);
    }

    void clearDeprecation() {
        labelElement.title = label;
        labelElement.classList.remove(deprecated);
    }

    void markAsRequired() {
        labelElement.innerHTML = label + " " + MESSAGES.requiredMarker().asString();
    }

    void clearRequired() {
        labelElement.innerHTML = label;
    }
}
