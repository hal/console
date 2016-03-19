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
package org.jboss.hal.ballroom.wizard;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static elemental.css.CSSStyleDeclaration.Unit.PCT;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.jboss.hal.resources.CSS.*;

/**
 * General purpose wizard relying on a context for the common data and an enum representing the states of the different
 * steps.
 * <p>
 * Concrete wizards must inherit from this class and
 * <ol>
 * <li>add steps in the constructor using {@link #addStep(Enum, WizardStep)}</li>
 * <li>provide the initial and last step(s) by overriding {@link #initialState()} and {@link #lastStates()}</li>
 * <li>decide how to move back and forth by overriding {@link #back(Enum)} and {@link #next(Enum)}</li>
 * </ol>
 *
 * @param <C> The context
 * @param <S> The state enum
 *
 * @author Harald Pehl
 */
public abstract class Wizard<C, S extends Enum<S>> {

    @FunctionalInterface
    public interface FinishCallback<C> {

        void onFinish(C context);
    }


    @FunctionalInterface
    public interface CancelCallback<C> {

        void onCancel(C context);
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String INDICATOR_ELEMENT = "indicatorElement";
    private static final String HEADER_ELEMENT = "headerElement";
    private static final String STEPS_CONTAINER = "stepsContainer";

    private final String id;
    private final LinkedHashMap<S, WizardStep<C, S>> steps;
    private final Element header;
    private final Element indicator;
    private final Element stepsContainer;
    private final Dialog dialog;
    private final C context;
    private final FinishCallback<C> finishCallback;
    private final CancelCallback<C> cancelCallback;
    private S state;

    protected Wizard(final String id, final String title, final C context) {
        this(id, title, context, null, null);
    }

    protected Wizard(final String id, final String title, final C context,
            FinishCallback<C> finishCallback) {
        this(id, title, context, finishCallback, null);
    }

    protected Wizard(final String id, final String title, final C context,
            FinishCallback<C> finishCallback, CancelCallback<C> cancelCallback) {
        this.id = id;
        this.context = context;
        this.finishCallback = finishCallback;
        this.cancelCallback = cancelCallback;
        this.steps = new LinkedHashMap<>();

        // @formatter:off
        Elements.Builder body = new Elements.Builder()
            .header().css(wizardHeader)
                .h(1).css(wizardHeader).rememberAs(HEADER_ELEMENT).end()
                .div().css(wizardProgress)
                    .span().css(CSS.indicator).rememberAs(INDICATOR_ELEMENT).end()
                .end()
            .end()
            .section().css(wizardStep).rememberAs(STEPS_CONTAINER).end();
        // @formatter:on

        this.header = body.referenceFor(HEADER_ELEMENT);
        this.indicator = body.referenceFor(INDICATOR_ELEMENT);
        this.stepsContainer = body.referenceFor(STEPS_CONTAINER);
        this.dialog = new Dialog.Builder(title)
                .closeOnEsc(true)
                .size(Size.MEDIUM)
                .add(body.elements())
                .secondary(-100, CONSTANTS.cancel(), this::onCancel)
                .secondary(CONSTANTS.back(), this::onBack)
                .primary(CONSTANTS.next(), this::onNext)
                .build();
    }

    private void initSteps() {
        for (Map.Entry<S, WizardStep<C, S>> entry : steps.entrySet()) {
            WizardStep<C, S> step = entry.getValue();
            Element element = step.asElement();
            Elements.setVisible(element, false);
            stepsContainer.appendChild(element);
        }
    }

    protected void addStep(final S state, final WizardStep<C, S> step) {
        steps.put(state, step);
    }


    // ------------------------------------------------------ public API

    /**
     * Opens the wizard and reset the state, context and UI. If you override this method please make sure to call
     * {@code super.show()} <em>before</em> you access or modify the context.
     */
    public void show() {
        assertSteps();
        if (stepsContainer.getChildElementCount() == 0) {
            initSteps();
        }

        resetContext();
        for (WizardStep<C, S> step : steps.values()) {
            step.reset(context);
        }
        state = initialState();

        dialog.show();
        pushState(state);
    }

    public C getContext() {
        return context;
    }


    // ------------------------------------------------------ workflow

    private boolean onCancel() {
        if (currentStep().onCancel(context)) {
            cancel();
            return true;
        }
        return false;
    }

    private boolean onBack() {
        if (currentStep().onBack(context)) {
            final S previousState = back(state);
            if (previousState != null) {
                pushState(previousState);
            }
        }
        return false;
    }

    private boolean onNext() {
        if (currentStep().onNext(context)) {
            final S nextState = next(state);
            if (nextState != null) {
                pushState(nextState);
                return false;
            } else {
                finish();
                return true;
            }
        } else {
            return false;
        }
    }


    /**
     * Method which is called when the wizard is finished.
     */
    private void finish() {
        if (finishCallback != null) {
            finishCallback.onFinish(context);
        }
    }

    /**
     * Method which is called when the wizard is canceled.
     */
    private void cancel() {
        if (cancelCallback != null) {
            cancelCallback.onCancel(context);
        }
    }

    /**
     * Sets the current state to the specified state and updates the UI to reflect the current state.
     *
     * @param state the next state
     */
    private void pushState(final S state) {
        this.state = state;

        int index = 0;
        int current = 0;
        for (Map.Entry<S, WizardStep<C, S>> entry : steps.entrySet()) {
            if (entry.getKey() == state) {
                current = index;
            }
            Elements.setVisible(entry.getValue().asElement(), entry.getKey() == state);
            index++;
        }
        current++;
        double width = min(round(((double) current / (double) steps.size()) * 100.0), 100.0);
        setTitle(currentStep().title);
        indicator.getStyle().setWidth(width, PCT);
        currentStep().onShow(context);
        for (Attachable attachable : currentStep().attachables) {
            attachable.attach();
        }
        ButtonElement back = dialog.getButton(Dialog.SECONDARY_POSITION);
        back.setDisabled(state == initialState());
        ButtonElement next = dialog.getButton(Dialog.PRIMARY_POSITION);
        next.setInnerHTML(lastStates().contains(state) ? CONSTANTS.finish() : CONSTANTS.next());
    }

    void setTitle(String title) {
        header.setInnerText(title);
    }

    /**
     * @return the initial state which is the state of the first added step by default.
     */
    protected S initialState() {
        assertSteps();
        return steps.keySet().iterator().next();
    }

    /**
     * @return the last state(s) which is the state of the last added step by default.
     */
    protected EnumSet<S> lastStates() {
        assertSteps();
        return EnumSet.of(Iterables.getLast(steps.keySet()));
    }

    /**
     * Subclasses must provide the previous state for {@code state} or {@code null} if there's no previous state.
     */
    protected abstract S back(final S state);

    /**
     * Subclasses must provide the next state for {@code state} or {@code null} if there's no next state (signals the
     * 'finished' state)
     */
    protected abstract S next(final S state);

    /**
     * Subclasses can override this method to reset the context. This method is called just before the
     * wizard is opened. You don't need to reset the state or the UI though, the {@link #show()} method will take
     * care of this.
     */
    protected void resetContext() {

    }


    // ------------------------------------------------------ helper methods

    private WizardStep<C, S> currentStep() {
        assertSteps();
        return steps.get(state);
    }

    /**
     * @return the unique id of this wizard.
     */
    protected String id() {
        return id;
    }

    private void assertSteps() {
        if (steps.isEmpty()) {
            throw new IllegalStateException("No steps found for wizard " + getClass()
                    .getName() + ". Please add steps in the constructor before using this wizard");
        }
    }
}
