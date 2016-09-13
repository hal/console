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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TABINDEX;

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


    // ------------------------------------------------------ wizard singleton

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String BACK_ELEMENT = "prev";
    private static final String CANCEL_ELEMENT = "cancel";
    private static final String CLOSE_ICON_ELEMENT = "closeIcon";
    private static final String LABEL = "label";
    private static final String MAIN_CONTAINER = "mainContainer";
    private static final String NEXT_ELEMENT = "next";
    private static final String SELECTOR_ID = "#" + Ids.HAL_WIZARD;
    private static final String STEPS_LIST = "steps";
    private static final String TITLE_ELEMENT = "title";

    private static final Element root;
    private static final Element closeIcon;
    private static final Element title;
    private static final Element stepsList;
    private static final Element main;
    private static final ButtonElement cancel;
    private static final ButtonElement back;
    private static final ButtonElement next;

    private static boolean open;


    static {
        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().id(Ids.HAL_WIZARD).css(modal)
                    .attr(ROLE, "wizard") //NON-NLS
                    .attr(TABINDEX, "-1")
                    .aria("labeledby", Ids.HAL_WIZARD_TITLE)
                .div().css(modalDialog, modalLarge, wizardPf)
                    .div().css(modalContent)
                        .div().css(modalHeader)
                            .button().css(close).aria(LABEL, CONSTANTS.close()).rememberAs(CLOSE_ICON_ELEMENT)
                                .span().css(pfIcon("close")).aria(HIDDEN, String.valueOf(true)).end()
                            .end()
                            .h(4).css(modalTitle).id(Ids.HAL_WIZARD_TITLE).rememberAs(TITLE_ELEMENT).end()
                        .end()
                        .div().css(modalBody, wizardPfBody, clearfix)
                            .div().css(wizardPfSteps)
                                .ul().css(wizardPfStepsIndicator).rememberAs(STEPS_LIST)
                                .end()
                            .end()
                            .div().css(wizardPfMain).rememberAs(MAIN_CONTAINER)
                            .end()
                        .end()
                        .div().css(modalFooter, wizardPfFooter)
                            .button().css(btn, btnDefault, btnCancel).rememberAs(CANCEL_ELEMENT)
                                .textContent(CONSTANTS.cancel())
                                .end()
                            .button().css(btn, btnDefault).rememberAs(BACK_ELEMENT)
                                .span().css(fontAwesome("angle-left")).end()
                                .span().textContent(CONSTANTS.back()).end()
                            .end()
                            .button().css(btn, btnPrimary).rememberAs(NEXT_ELEMENT)
                                .span().textContent(CONSTANTS.next()).end()
                                .span().css(fontAwesome("angle-right")).end()
                            .end()
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        root = rootBuilder.build();
        closeIcon = rootBuilder.referenceFor(CLOSE_ICON_ELEMENT);
        title = rootBuilder.referenceFor(TITLE_ELEMENT);
        stepsList = rootBuilder.referenceFor(STEPS_LIST);
        main = rootBuilder.referenceFor(MAIN_CONTAINER);
        cancel = rootBuilder.referenceFor(CANCEL_ELEMENT);
        back = rootBuilder.referenceFor(BACK_ELEMENT);
        next = rootBuilder.referenceFor(NEXT_ELEMENT);

        Browser.getDocument().getBody().appendChild(root);
        initEventHandler();
    }

    private static void initEventHandler() {
        $(SELECTOR_ID).on(UIConstants.SHOWN_MODAL, () -> Wizard.open = true);
        $(SELECTOR_ID).on(UIConstants.HIDDEN_MODAL, () -> Wizard.open = false);
    }

    private static void reset() {
        Elements.removeChildrenFrom(stepsList);
        Elements.removeChildrenFrom(main);
    }


    // ------------------------------------------------------ wizard instance

    private final String id;
    private final LinkedHashMap<S, WizardStep<C, S>> steps;
    private final Map<S, Element> stepIndicators;
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
        this.stepIndicators = new HashMap<>();

        reset();
        Wizard.title.setTextContent(title);
        closeIcon.setOnclick(event -> onCancel());
        cancel.setOnclick(event -> onCancel());
        back.setOnclick(event -> onBack());
        next.setOnclick(event -> onNext());
    }

    private void initSteps() {
        int index = 1;
        for (Map.Entry<S, WizardStep<C, S>> entry : steps.entrySet()) {
            WizardStep<C, S> step = entry.getValue();

            // @formatter:off
            Element li = new Elements.Builder()
                .li()
                    .a()
                        .span().css(wizardPfStepNumber).textContent(String.valueOf(index)).end()
                        .span().css(wizardPfStepTitle).textContent(step.title).end()
                    .end()
                .end()
            .build();
            // @formatter:on

            stepIndicators.put(entry.getKey(), li);
            stepsList.appendChild(li);

            Element stepElement = step.asElement();
            main.appendChild(stepElement);
            Elements.setVisible(stepElement, false);

            index++;
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
        if (stepsList.getChildElementCount() == 0) {
            initSteps();
        }

        resetContext();
        for (WizardStep<C, S> step : steps.values()) {
            step.reset(context);
        }
        state = initialState();

        if (Wizard.open) {
            throw new IllegalStateException(
                    "Another wizard is still open. Only one wizard can be open at a time. Please close the other wizard!");
        }
        $(SELECTOR_ID).modal(ModalOptions.create(true));
        $(SELECTOR_ID).modal("show");
        pushState(state);
    }

    public C getContext() {
        return context;
    }

    private void close() {
        $(SELECTOR_ID).modal("hide");
    }


    // ------------------------------------------------------ workflow

    private void onCancel() {
        if (currentStep().onCancel(context)) {
            cancel();
            close();
        }
    }

    private void onBack() {
        if (currentStep().onBack(context)) {
            final S previousState = back(state);
            if (previousState != null) {
                pushState(previousState);
            }
        }
    }

    private void onNext() {
        if (currentStep().onNext(context)) {
            final S nextState = next(state);
            if (nextState != null) {
                pushState(nextState);
            } else {
                finish();
            }
        }
    }


    /**
     * Method which is called when the wizard is finished.
     */
    private void finish() {
        if (finishCallback != null) {
            finishCallback.onFinish(context);
        }
        close();
    }

    /**
     * Method which is called when the wizard is canceled.
     */
    private void cancel() {
        if (cancelCallback != null) {
            cancelCallback.onCancel(context);
        }
        close();
    }

    /**
     * Sets the current state to the specified state and updates the UI to reflect the current state.
     *
     * @param state the next state
     */
    private void pushState(final S state) {
        this.state = state;

        stepIndicators.forEach((s, element) -> {
            if (s == state) {
                element.getClassList().add(active);
            } else {
                element.getClassList().remove(active);
            }
        });
        steps.forEach((s, step) -> Elements.setVisible(step.asElement(), s == state));
        currentStep().onShow(context);
        for (Attachable attachable : currentStep().attachables) {
            attachable.attach();
        }

        back.setDisabled(state == initialState());
        next.setInnerHTML(lastStates().contains(state) ? CONSTANTS.finish() : CONSTANTS.next());
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
