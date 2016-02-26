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
package org.jboss.hal.ballroom.wizard;

import com.google.common.base.CharMatcher;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A step in a wizard. The UI for the step should <string>not</string> contain a header. The header is part of the
 * wizard and will show this step's title.
 *
 * @param <C> The context
 * @param <S> The state enum
 *
 * @author Harald Pehl
 */
public abstract class WizardStep<C, S extends Enum<S>> implements IsElement {

    final List<Attachable> attachables;
    protected final Wizard<C, S> wizard;
    protected String title;

    protected WizardStep(final Wizard<C, S> wizard, final String title) {
        this.wizard = wizard;
        this.title = title;
        this.attachables = new ArrayList<>();
    }

    /**
     * Subclasses should reset their state using this method. This method is called just before the
     * wizard is opened. Opposed to {@link #onShow(Object)} this method should be used to implement one-time
     * initialization.
     */
    public void reset(C context) {

    }

    /**
     * Called every time the step is shown.
     *
     * @param context the current context
     */
    protected void onShow(C context) {}

    /**
     * Called when this step is canceled.
     *
     * @param context the current context
     *
     * @return {@code true} if we can cancel this step, {@code false} otherwise.
     */
    protected boolean onCancel(C context) { return true; }

    /**
     * Called before the previous step is shown. The method is called no matter if there's a previous step!
     *
     * @param context the current context
     *
     * @return {@code true} if we can navigate to the previous step, {@code false} otherwise.
     */
    protected boolean onBack(C context) { return true; }

    /**
     * Called before the next step is shown. The method is called no matter if there's a next step!
     *
     * @param context the current context
     *
     * @return {@code true} if we can navigate to the next step, {@code false} otherwise.
     */
    protected boolean onNext(C context) { return true; }

    /**
     * @return an unique id for this step based on {@link Wizard#id()}. Can be used as base ID for widgets of this step.
     */
    protected String id() {
        return wizard.id() + "_" + CharMatcher.WHITESPACE.removeFrom(title);
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }
}
