/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;

/**
 * A step in a wizard. The UI for the step should <string>not</string> contain a header. The header is part of the
 * wizard and will show this step's title.
 *
 * @param <C> The context
 * @param <S> The state enum
 */
public abstract class WizardStep<C, S extends Enum<S>> implements IsElement {

    final List<Attachable> attachables;
    private Wizard<C, S> wizard;
    protected String title;

    protected WizardStep(String title) {
        this.title = title;
        this.attachables = new ArrayList<>();
    }

    void init(Wizard<C, S> wizard) {
        this.wizard = wizard;
    }

    /**
     * Valid only <strong>after</strong> the wizard has been created. Must not be used in the step constructor!
     *
     * @return the wizard instance this step is part of.
     */
    protected Wizard<C, S> wizard() {
        return wizard;
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
    protected void onShow(C context) {
    }

    /**
     * Called when this step is canceled.
     *
     * @param context the current context
     *
     * @return {@code true} if we can cancel this step, {@code false} otherwise.
     */
    protected boolean onCancel(C context) {
        return true;
    }

    /**
     * Called before the previous step is shown. The method is called no matter if there's a previous step!
     *
     * @param context the current context
     *
     * @return {@code true} if we can navigate to the previous step, {@code false} otherwise.
     */
    protected boolean onBack(C context) {
        return true;
    }

    /**
     * Called before the next step is shown. The method is called no matter if there's a next step!
     *
     * @param context the current context
     *
     * @return {@code true} if we can navigate to the next step, {@code false} otherwise.
     */
    protected boolean onNext(C context) {
        return true;
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }
}
