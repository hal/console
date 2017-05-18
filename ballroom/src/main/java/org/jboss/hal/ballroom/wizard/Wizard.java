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
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.html.ButtonElement;
import elemental.html.DivElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TABINDEX;
import static org.jboss.hal.resources.UIConstants.TRUE;

/**
 * General purpose wizard relying on a context for the common data and an enum representing the states of the different
 * steps.
 *
 * @param <C> The context
 * @param <S> The state enum
 *
 * @author Harald Pehl
 */
public class Wizard<C, S extends Enum<S>> {

    @FunctionalInterface
    public interface BackFunction<C, S extends Enum<S>> {

        S back(C context, S currentState);
    }


    @FunctionalInterface
    public interface NextFunction<C, S extends Enum<S>> {

        S next(C context, S currentState);
    }


    /**
     * An action executed when the user clicks on the success button of the success page.
     */
    @FunctionalInterface
    public interface SuccessAction<C> {

        void execute(C context);
    }


    /**
     * A callback executed when the user finishes last step.
     *
     * @param <C>
     */
    @FunctionalInterface
    public interface FinishCallback<C, S extends Enum<S>> {

        void onFinish(Wizard<C, S> wizard, C context);
    }


    /**
     * A callback executed whenever the user cancels the wizard.
     *
     * @param <C>
     */
    @FunctionalInterface
    public interface CancelCallback<C> {

        void onCancel(C context);
    }


    // ------------------------------------------------------ wizard builder


    public static class Builder<C, S extends Enum<S>> {

        private final String title;
        private final C context;
        private final LinkedHashMap<S, WizardStep<C, S>> steps;
        private S initialState;
        private BackFunction<C, S> back;
        private NextFunction<C, S> next;
        private EnumSet<S> lastStates;
        private FinishCallback<C, S> finishCallback;
        private CancelCallback<C> cancelCallback;
        private boolean stayOpenAfterFinish;

        public Builder(final String title, final C context) {
            this.title = title;
            this.context = context;
            this.steps = new LinkedHashMap<>();
            this.initialState = null;
            this.back = null;
            this.next = null;
            this.lastStates = null;
            this.finishCallback = null;
            this.cancelCallback = null;
            this.stayOpenAfterFinish = false;
        }

        public Builder<C, S> addStep(S state, WizardStep<C, S> step) {
            steps.put(state, step);
            return this;
        }

        public Builder<C, S> onBack(BackFunction<C, S> back) {
            this.back = back;
            return this;
        }

        public Builder<C, S> onNext(NextFunction<C, S> next) {
            this.next = next;
            return this;
        }

        public Builder<C, S> onFinish(FinishCallback<C, S> finishCallback) {
            this.finishCallback = finishCallback;
            return this;
        }

        public Builder<C, S> onCancel(CancelCallback<C> cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public Builder<C, S> stayOpenAfterFinish() {
            this.stayOpenAfterFinish = true;
            return this;
        }

        public Wizard<C, S> build() {
            if (steps.isEmpty()) {
                throw new IllegalStateException("No steps found for wizard '" + title + "'");
            }
            if (back == null) {
                throw new IllegalStateException("No back function defined for wizard '" + title + "'");
            }
            if (next == null) {
                throw new IllegalStateException("No next function defined for wizard '" + title + "'");
            }
            return new Wizard<>(this);
        }
    }


    // ------------------------------------------------------ wizard singleton

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String BACK_BUTTON = "backButton";
    private static final String BLANK_SLATE = "blankSlate";
    private static final String CANCEL_BUTTON = "cancelButton";
    private static final String CLOSE_ICON = "closeIcon";
    private static final String MAIN_CONTAINER = "mainContainer";
    private static final String NEXT_BUTTON = "nextButton";
    private static final String NEXT_TEXT = "nextText";
    private static final String NEXT_ICON = "nextIcon";
    private static final String SELECTOR_ID = "#" + Ids.HAL_WIZARD;
    private static final String STEP_NAMES = "stepNames";
    private static final String TITLE_ELEMENT = "titleElement";

    private static final Element root;
    private static final Element titleElement;
    private static final Element closeIcon;
    private static final Element stepsNames;
    private static final Element mainContainer;
    private static final Element blankSlate;
    private static final ButtonElement cancelButton;
    private static final ButtonElement backButton;
    private static final ButtonElement nextButton;
    private static final Element nextText;
    private static final Element nextIcon;

    private static boolean open;


    static {
        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().id(Ids.HAL_WIZARD).css(modal)
                    .attr(ROLE, "wizard") //NON-NLS
                    .attr(TABINDEX, "-1")
                    .aria("labeledby", Ids.HAL_WIZARD_TITLE)
                .div().css(modalDialog, modalLg, wizardPf)
                    .div().css(modalContent)
                        .div().css(modalHeader)
                            .button().css(close).aria("label", CONSTANTS.close()).rememberAs(CLOSE_ICON)
                                .span().css(pfIcon("close")).aria(HIDDEN, TRUE).end()
                            .end()
                            .h(4).css(modalTitle).id(Ids.HAL_WIZARD_TITLE).rememberAs(TITLE_ELEMENT).end()
                        .end()
                        .div().css(modalBody, wizardPfBody, clearfix)
                            .div().css(wizardPfSteps)
                                .ul().css(wizardPfStepsIndicator).rememberAs(STEP_NAMES)
                                .end()
                            .end()
                            .div().css(wizardPfRow)
                                .div().css(wizardPfMain, wizardHalNoSidebar).rememberAs(MAIN_CONTAINER)
                                    .div().css(blankSlatePf).rememberAs(BLANK_SLATE).end()
                                .end()
                            .end()
                        .end()
                        .div().css(modalFooter, wizardPfFooter)
                            .button().css(btn, btnDefault, btnCancel).rememberAs(CANCEL_BUTTON)
                                .textContent(CONSTANTS.cancel())
                                .end()
                            .button().css(btn, btnDefault).rememberAs(BACK_BUTTON)
                                .span().css(fontAwesome("angle-left")).end()
                                .span().textContent(CONSTANTS.back()).end()
                            .end()
                            .button().css(btn, btnPrimary).rememberAs(NEXT_BUTTON)
                                .span().textContent(CONSTANTS.next()).rememberAs(NEXT_TEXT).end()
                                .span().css(fontAwesome("angle-right")).rememberAs(NEXT_ICON).end()
                            .end()
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        root = rootBuilder.build();
        titleElement = rootBuilder.referenceFor(TITLE_ELEMENT);
        closeIcon = rootBuilder.referenceFor(CLOSE_ICON);
        stepsNames = rootBuilder.referenceFor(STEP_NAMES);
        mainContainer = rootBuilder.referenceFor(MAIN_CONTAINER);
        blankSlate = rootBuilder.referenceFor(BLANK_SLATE);
        cancelButton = rootBuilder.referenceFor(CANCEL_BUTTON);
        backButton = rootBuilder.referenceFor(BACK_BUTTON);
        nextButton = rootBuilder.referenceFor(NEXT_BUTTON);
        nextText = rootBuilder.referenceFor(NEXT_TEXT);
        nextIcon = rootBuilder.referenceFor(NEXT_ICON);

        Browser.getDocument().getBody().appendChild(root);
        initEventHandler();
    }

    public static boolean isOpen() {
        return open;
    }

    private static void initEventHandler() {
        $(SELECTOR_ID).on(UIConstants.SHOWN_MODAL, () -> Wizard.open = true);
        $(SELECTOR_ID).on(UIConstants.HIDDEN_MODAL, () -> Wizard.open = false);
    }

    private static void reset() {
        Elements.removeChildrenFrom(stepsNames);
        NodeList contents = mainContainer.querySelectorAll("." + wizardPfContents);
        Elements.stream(contents).forEach(mainContainer::removeChild);
        Elements.setVisible(blankSlate, false);
    }


    // ------------------------------------------------------ wizard instance

    private final C context;
    private final LinkedHashMap<S, WizardStep<C, S>> steps;
    private final LinkedHashMap<S, Element> stepElements;
    private final Map<S, Element> stepIndicators;
    private S initialState;
    private BackFunction<C, S> back;
    private NextFunction<C, S> next;
    private EnumSet<S> lastStates;
    private FinishCallback<C, S> finishCallback;
    private CancelCallback<C> cancelCallback;
    private boolean showsError;
    private boolean stayOpenAfterFinish;
    private boolean finishCanClose;
    private S state;

    private Wizard(final Builder<C, S> builder) {
        this.context = builder.context;
        this.steps = new LinkedHashMap<>(builder.steps);
        this.steps.values().forEach(step -> step.init(this));
        this.stepElements = new LinkedHashMap<>();
        this.stepIndicators = new HashMap<>();
        this.initialState = builder.initialState == null ? steps.keySet().iterator().next() : builder.initialState;
        this.back = builder.back;
        this.next = builder.next;
        this.lastStates = builder.lastStates == null ? EnumSet
                .of(Iterables.getLast(steps.keySet())) : builder.lastStates;
        this.finishCallback = builder.finishCallback;
        this.cancelCallback = builder.cancelCallback;
        this.showsError = false;
        this.stayOpenAfterFinish = builder.stayOpenAfterFinish;
        this.finishCanClose = false;

        reset();
        Wizard.titleElement.setTextContent(builder.title);
        closeIcon.setOnclick(event -> onCancel());
        cancelButton.setOnclick(event -> onCancel());
        backButton.setOnclick(event -> onBack());
        nextButton.setOnclick(event -> onNext());
    }


    // ------------------------------------------------------ public API

    /**
     * Opens the wizard and reset the state, context and UI. If you override this method please make sure to call
     * {@code super.show()} <em>before</em> you access or modify the context.
     */
    public void show() {
        if (stepsNames.getChildElementCount() == 0) {
            initSteps();
        }

        for (WizardStep<C, S> step : steps.values()) {
            step.reset(context);
        }
        state = initialState;

        if (Wizard.open) {
            throw new IllegalStateException(
                    "Another wizard is still open. Only one wizard can be open at a time. Please close the other wizard!");
        }
        $(SELECTOR_ID).modal(ModalOptions.create(true));
        $(SELECTOR_ID).modal("show");
        PatternFly.initComponents(SELECTOR_ID);
        pushState(state);
    }

    public void showProgress(final String title, final SafeHtml text) {
        Elements.Builder builder = new Elements.Builder()
                .div().css(spinner, spinnerLg, blankSlatePfIcon).end()
                .h(3).css(blankSlatePfMainAction).textContent(title).end()
                .p().css(blankSlatePfSecondaryAction).innerHtml(text).end();

        blankSlate.getClassList().remove(wizardPfComplete);
        blankSlate.getClassList().add(wizardPfProcess);
        Elements.removeChildrenFrom(blankSlate);
        builder.elements().forEach(blankSlate::appendChild);

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        backButton.setDisabled(true);
        nextButton.setDisabled(true);
    }

    public void showSuccess(final String title, final SafeHtml text) {
        showSuccess(title, text, null, null, true);
    }

    public void showSuccess(final String title, final SafeHtml text, final boolean lastStep) {
        showSuccess(title, text, null, null, lastStep);
    }

    public void showSuccess(final String title, final SafeHtml text,
            final String successButton, SuccessAction<C> successAction) {
        showSuccess(title, text, successButton, successAction, true);
    }

    public void showSuccess(final String title, final SafeHtml text, final String successButton,
            SuccessAction<C> successAction, final boolean lastStep) {
        Elements.Builder builder = new Elements.Builder()
                .div().css(wizardPfSuccessIcon)
                .span().css(glyphicon("ok-circle")).end()
                .end()
                .h(3).css(blankSlatePfMainAction).textContent(title).end()
                .p().css(blankSlatePfSecondaryAction).innerHtml(text).end();
        if (successButton != null && successAction != null) {
            builder.button().css(btn, btnLg, btnPrimary).textContent(successButton)
                    .on(click, event -> {
                        successAction.execute(context);
                        close();
                    })
                    .end();
        }

        blankSlate.getClassList().remove(wizardPfProcess);
        blankSlate.getClassList().add(wizardPfComplete);
        Elements.removeChildrenFrom(blankSlate);
        builder.elements().forEach(blankSlate::appendChild);

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        cancelButton.setDisabled(lastStep);
        backButton.setDisabled(lastStep);
        nextButton.setDisabled(false);
        if (lastStep) {
            nextText.setTextContent(CONSTANTS.close());
            Elements.setVisible(nextIcon, false);
        }
        finishCanClose = lastStep;
    }

    public void showError(final String title, final SafeHtml text) {
        showError(title, text, null, true);
    }

    public void showError(final String title, final SafeHtml text, final boolean lastStep) {
        showError(title, text, null, lastStep);
    }

    public void showError(final String title, final SafeHtml text, final String error) {
        showError(title, text, error, true);
    }

    public void showError(final String title, final SafeHtml text, final String error, final boolean lastStep) {
        Elements.Builder builder = new Elements.Builder()
                .div().css(wizardPfErrorIcon)
                .span().css(glyphicon("remove-circle")).end()
                .end()
                .h(3).css(blankSlatePfMainAction).textContent(title).end()
                .p().css(blankSlatePfSecondaryAction).innerHtml(text);
        if (error != null) {
            String id = Ids.uniqueId();
            builder.a("#" + id).css(marginLeft5)
                    .data(UIConstants.TOGGLE, UIConstants.COLLAPSE)
                    .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                    .aria(UIConstants.CONTROLS, id)
                    .textContent(CONSTANTS.details())
                    .end();
            builder.end(); // </p>
            builder.div().css(collapse).id(id).aria(UIConstants.EXPANDED, UIConstants.FALSE)
                    .start("pre").css(wizardHalErrorText).textContent(error).end()
                    .end();
        } else {
            builder.end(); // </p>
        }

        blankSlate.getClassList().remove(wizardPfProcess);
        blankSlate.getClassList().add(wizardPfComplete);
        Elements.removeChildrenFrom(blankSlate);
        builder.elements().forEach(blankSlate::appendChild);

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        cancelButton.setDisabled(lastStep);
        backButton.setDisabled(lastStep);
        nextButton.setDisabled(!lastStep);
        if (lastStep) {
            nextText.setTextContent(CONSTANTS.close());
            Elements.setVisible(nextIcon, false);
        }
        finishCanClose = lastStep;
        showsError = true;
    }

    public C getContext() {
        return context;
    }


    // ------------------------------------------------------ workflow

    @SuppressWarnings("unchecked")
    private void onCancel() {
        if (currentStep() instanceof AsyncStep) {
            ((AsyncStep<C>) currentStep()).onCancel(context, this::cancel);
        } else {
            if (currentStep().onCancel(context)) {
                cancel();
            }
        }
    }

    private void cancel() {
        if (cancelCallback != null) {
            cancelCallback.onCancel(context);
        }
        close();
    }

    @SuppressWarnings("unchecked")
    private void onBack() {
        if (currentStep() instanceof AsyncStep) {
            ((AsyncStep<C>) currentStep()).onBack(context, this::back);
        } else {
            if (currentStep().onBack(context)) {
                back();
            }
        }
        finishCanClose = false;
    }

    private void back() {
        if (showsError) {
            pushState(state);
        } else {
            final S previousState = back.back(context, state);
            if (previousState != null) {
                pushState(previousState);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void onNext() {
        if (finishCanClose) {
            // we're on the last step and have either seen a success or error message
            close();
        } else {
            if (currentStep() instanceof AsyncStep) {
                ((AsyncStep<C>) currentStep()).onNext(context, this::next);
            } else {
                if (currentStep().onNext(context)) {
                    next();
                }
            }
        }
    }

    private void next() {
        final S nextState = next.next(context, state);
        if (nextState != null) {
            pushState(nextState);
        } else {
            finish();
        }
    }

    private void finish() {
        if (finishCallback != null) {
            finishCallback.onFinish(this, context);
        }
        if (!stayOpenAfterFinish) {
            close();
        }
    }

    /**
     * Sets the current state to the specified state and updates the UI to reflect the current state.
     *
     * @param state the next state
     */
    private void pushState(final S state) {
        this.state = state;
        this.showsError = false;

        stepIndicators.forEach((s, element) -> {
            if (s == state) {
                element.getClassList().add(active);
            } else {
                element.getClassList().remove(active);
            }
        });
        Elements.setVisible(blankSlate, false);
        stepElements.forEach((s, element) -> Elements.setVisible(element, s == state));
        currentStep().onShow(context);

        cancelButton.setDisabled(false);
        backButton.setDisabled(state == initialState);
        nextButton.setDisabled(false);
        nextText.setTextContent(lastStates.contains(state) ? CONSTANTS.finish() : CONSTANTS.next());
        Elements.setVisible(nextIcon, !lastStates.contains(state));
    }

    private WizardStep<C, S> currentStep() {
        return steps.get(state);
    }


    // ------------------------------------------------------ private methods

    private void initSteps() {
        int index = 1;
        for (Map.Entry<S, WizardStep<C, S>> entry : steps.entrySet()) {
            S status = entry.getKey();
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

            stepIndicators.put(status, li);
            stepsNames.appendChild(li);

            DivElement wrapper = Browser.getDocument().createDivElement();
            wrapper.getClassList().add(wizardPfContents);
            wrapper.appendChild(step.asElement());
            step.attachables.forEach(Attachable::attach);
            Elements.setVisible(wrapper, false);
            mainContainer.appendChild(wrapper);
            stepElements.put(status, wrapper);

            index++;
        }
    }

    private void close() {
        steps.values().forEach(step -> step.attachables.forEach(Attachable::detach));
        $(SELECTOR_ID).modal("hide");
    }
}
