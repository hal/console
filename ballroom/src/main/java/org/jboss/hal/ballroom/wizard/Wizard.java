/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.wizard;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.HandlerRegistrations;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLParagraphElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.blankSlatePf;
import static org.jboss.hal.resources.CSS.blankSlatePfIcon;
import static org.jboss.hal.resources.CSS.blankSlatePfMainAction;
import static org.jboss.hal.resources.CSS.blankSlatePfSecondaryAction;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnCancel;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnLg;
import static org.jboss.hal.resources.CSS.btnPrimary;
import static org.jboss.hal.resources.CSS.clearfix;
import static org.jboss.hal.resources.CSS.close;
import static org.jboss.hal.resources.CSS.collapse;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.glyphicon;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.modal;
import static org.jboss.hal.resources.CSS.modalBody;
import static org.jboss.hal.resources.CSS.modalContent;
import static org.jboss.hal.resources.CSS.modalDialog;
import static org.jboss.hal.resources.CSS.modalFooter;
import static org.jboss.hal.resources.CSS.modalHeader;
import static org.jboss.hal.resources.CSS.modalLg;
import static org.jboss.hal.resources.CSS.modalTitle;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.spinner;
import static org.jboss.hal.resources.CSS.spinnerLg;
import static org.jboss.hal.resources.CSS.wizardHalErrorText;
import static org.jboss.hal.resources.CSS.wizardHalNoSidebar;
import static org.jboss.hal.resources.CSS.wizardPf;
import static org.jboss.hal.resources.CSS.wizardPfBody;
import static org.jboss.hal.resources.CSS.wizardPfComplete;
import static org.jboss.hal.resources.CSS.wizardPfContents;
import static org.jboss.hal.resources.CSS.wizardPfErrorIcon;
import static org.jboss.hal.resources.CSS.wizardPfFooter;
import static org.jboss.hal.resources.CSS.wizardPfMain;
import static org.jboss.hal.resources.CSS.wizardPfProcess;
import static org.jboss.hal.resources.CSS.wizardPfRow;
import static org.jboss.hal.resources.CSS.wizardPfStep;
import static org.jboss.hal.resources.CSS.wizardPfStepNumber;
import static org.jboss.hal.resources.CSS.wizardPfStepTitle;
import static org.jboss.hal.resources.CSS.wizardPfSteps;
import static org.jboss.hal.resources.CSS.wizardPfStepsIndicator;
import static org.jboss.hal.resources.CSS.wizardPfSuccessIcon;
import static org.jboss.hal.resources.CSS.wizardPfWarningIcon;
import static org.jboss.hal.resources.UIConstants.COLLAPSE;
import static org.jboss.hal.resources.UIConstants.CONTROLS;
import static org.jboss.hal.resources.UIConstants.EXPANDED;
import static org.jboss.hal.resources.UIConstants.FALSE;
import static org.jboss.hal.resources.UIConstants.HASH;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.LABEL;
import static org.jboss.hal.resources.UIConstants.ROLE;
import static org.jboss.hal.resources.UIConstants.TABINDEX;
import static org.jboss.hal.resources.UIConstants.TOGGLE;
import static org.jboss.hal.resources.UIConstants.TRUE;

/**
 * General purpose wizard relying on a context for the common data and an enum representing the states of the different steps.
 *
 * @param <C> The context
 * @param <S> The state enum
 */
public class Wizard<C, S extends Enum<S>> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String SELECTOR_ID = HASH + Ids.HAL_WIZARD;

    private static final HTMLElement root;
    private static final HTMLElement titleElement;
    private static final HTMLElement closeIcon;
    private static final HTMLElement stepsNames;
    private static final HTMLElement mainContainer;
    private static final HTMLElement blankSlate;
    private static final HTMLButtonElement cancelButton;
    private static final HTMLButtonElement backButton;
    private static final HTMLButtonElement nextButton;
    private static final HTMLElement nextText;
    private static final HTMLElement nextIcon;

    private static boolean open;

    static {
        root = div().css(modal)
                .id(Ids.HAL_WIZARD)
                .attr(ROLE, "wizard") // NON-NLS
                .attr(TABINDEX, "-1")
                .aria("labeledby", Ids.HAL_WIZARD_TITLE)
                .add(div().css(modalDialog, modalLg, wizardPf)
                        .add(div().css(modalContent)
                                .add(div().css(modalHeader)
                                        .add(closeIcon = button().css(close)
                                                .aria(LABEL, CONSTANTS.close())
                                                .add(span().css(pfIcon("close")).aria(HIDDEN, TRUE)).element())
                                        .add(titleElement = h(4).css(modalTitle)
                                                .id(Ids.HAL_WIZARD_TITLE).element()))
                                .add(div().css(modalBody, wizardPfBody, clearfix)
                                        .add(div().css(wizardPfSteps)
                                                .add(stepsNames = ul().css(wizardPfStepsIndicator).element()))
                                        .add(div().css(wizardPfRow)
                                                .add(mainContainer = div().css(wizardPfMain, wizardHalNoSidebar)
                                                        .add(blankSlate = div().css(blankSlatePf).element())
                                                        .element())))
                                .add(div().css(modalFooter, wizardPfFooter)
                                        .add(cancelButton = button().css(btn, btnDefault, btnCancel)
                                                .textContent(CONSTANTS.cancel()).element())
                                        .add(backButton = button().css(btn, btnDefault)
                                                .add(span().css(fontAwesome("angle-left")))
                                                .add(span().textContent(CONSTANTS.back())).element())
                                        .add(nextButton = button().css(btn, btnPrimary)
                                                .add(nextText = span().textContent(CONSTANTS.next()).element())
                                                .add(nextIcon = span().css(fontAwesome("angle-right")).element())
                                                .element()))))
                .element();

        document.body.appendChild(root);
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
        elemental2.dom.NodeList<elemental2.dom.Element> contents = mainContainer.querySelectorAll(
                "." + wizardPfContents);
        Elements.stream(contents).forEach(mainContainer::removeChild);
        Elements.setVisible(blankSlate, false);
    }

    // ------------------------------------------------------ wizard instance

    private final C context;
    private final LinkedHashMap<S, WizardStep<C, S>> steps;
    private final LinkedHashMap<S, HTMLElement> stepElements;
    private final Map<S, HTMLElement> stepIndicators;
    private final HandlerRegistration handlerRegistration;
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

    private Wizard(Builder<C, S> builder) {
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
        Wizard.titleElement.textContent = builder.title;
        handlerRegistration = HandlerRegistrations.compose(
                bind(closeIcon, click, event -> onCancel()),
                bind(cancelButton, click, event -> onCancel()),
                bind(backButton, click, event -> onBack()),
                bind(nextButton, click, event -> onNext()));
    }

    // ------------------------------------------------------ public API

    /**
     * Opens the wizard and reset the state, context and UI. If you override this method please make sure to call
     * {@code super.show()} <em>before</em> you access or modify the context.
     */
    public void show() {
        if (stepsNames.childElementCount == 0) {
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

    public void showProgress(String title, SafeHtml text) {
        blankSlate.classList.remove(wizardPfComplete);
        blankSlate.classList.add(wizardPfProcess);
        Elements.removeChildrenFrom(blankSlate);

        blankSlate.appendChild(div().css(spinner, spinnerLg, blankSlatePfIcon).element());
        blankSlate.appendChild(h(3).css(blankSlatePfMainAction).textContent(title).element());
        blankSlate.appendChild(p().css(blankSlatePfSecondaryAction).innerHtml(text).element());

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        backButton.disabled = true;
        nextButton.disabled = true;
    }

    public void showSuccess(String title, SafeHtml text) {
        showSuccess(title, text, null, null, true);
    }

    public void showSuccess(String title, SafeHtml text, boolean lastStep) {
        showSuccess(title, text, null, null, lastStep);
    }

    public void showSuccess(String title, SafeHtml text, CloseAction<C> closeAction) {
        showSuccess(title, text, null, null, closeAction, true);
    }

    public void showSuccess(String title, SafeHtml text, CloseAction<C> closeAction, boolean lastStep) {
        showSuccess(title, text, null, null, closeAction, lastStep);
    }

    public void showSuccess(String title, SafeHtml text, String successButton, SuccessAction<C> successAction) {
        showSuccess(title, text, successButton, successAction, true);
    }

    public void showSuccess(String title, SafeHtml text, String successButton, SuccessAction<C> successAction,
            boolean lastStep) {
        showSuccess(title, text, successButton, successAction, null, lastStep);
    }

    public void showSuccess(String title, SafeHtml text, String successButton, SuccessAction<C> successAction,
            CloseAction<C> closeAction, boolean lastStep) {
        blankSlate.classList.remove(wizardPfProcess);
        blankSlate.classList.add(wizardPfComplete);
        Elements.removeChildrenFrom(blankSlate);

        blankSlate.appendChild(div().css(wizardPfSuccessIcon)
                .add(span().css(glyphicon("ok-circle"))).element());
        blankSlate.appendChild(h(3).css(blankSlatePfMainAction).textContent(title).element());
        blankSlate.appendChild(p().css(blankSlatePfSecondaryAction).innerHtml(text).element());

        if (successButton != null && successAction != null) {
            blankSlate.appendChild(button().css(btn, btnLg, btnPrimary)
                    .textContent(successButton)
                    .on(click, event -> {
                        successAction.execute(context);
                        close();
                    }).element());
        }

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        cancelButton.disabled = lastStep;
        backButton.disabled = lastStep;
        nextButton.disabled = false;
        if (lastStep) {
            nextText.textContent = CONSTANTS.close();
            Elements.setVisible(nextIcon, false);
        }
        if (closeAction != null) {
            nextButton.onclick = event -> {
                closeAction.execute(context);
                return null;
            };
        } else {
            nextButton.onclick = null;
        }
        finishCanClose = lastStep;
    }

    public void showWarning(String title, SafeHtml text, String okButton, SuccessAction<C> okAction, boolean lastStep) {
        blankSlate.classList.remove(wizardPfProcess);
        blankSlate.classList.add(wizardPfComplete);
        Elements.removeChildrenFrom(blankSlate);

        blankSlate.appendChild(div().css(wizardPfWarningIcon)
                .add(span().css(glyphicon("exclamation-sign"))).element());
        blankSlate.appendChild(h(3).css(blankSlatePfMainAction).textContent(title).element());
        blankSlate.appendChild(p().css(blankSlatePfSecondaryAction).innerHtml(text).element());

        if (okButton != null && okAction != null) {
            blankSlate.appendChild(button().css(btn, btnLg, btnPrimary)
                    .textContent(okButton)
                    .on(click, event -> okAction.execute(context))
                    .element());
        }

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        cancelButton.disabled = lastStep;
        backButton.disabled = lastStep;
        nextButton.disabled = false;
        if (lastStep) {
            nextText.textContent = CONSTANTS.close();
            Elements.setVisible(nextIcon, false);
        }
        nextButton.onclick = null;
        finishCanClose = lastStep;
    }

    public void showError(String title, SafeHtml text) {
        showError(title, text, null, true);
    }

    public void showError(String title, SafeHtml text, boolean lastStep) {
        showError(title, text, null, lastStep);
    }

    public void showError(String title, SafeHtml text, String error) {
        showError(title, text, error, true);
    }

    public void showError(String title, SafeHtml text, String error, boolean lastStep) {
        blankSlate.classList.remove(wizardPfProcess);
        blankSlate.classList.add(wizardPfComplete);
        Elements.removeChildrenFrom(blankSlate);

        blankSlate.appendChild(div().css(wizardPfErrorIcon)
                .add(span().css(glyphicon("remove-circle"))).element());
        blankSlate.appendChild(h(3).css(blankSlatePfMainAction).textContent(title).element());
        HTMLParagraphElement p = p().css(blankSlatePfSecondaryAction)
                .innerHtml(text).element();
        blankSlate.appendChild(p);
        if (error != null) {
            String id = Ids.uniqueId();
            p.appendChild(a(HASH + id).css(marginLeft5)
                    .data(TOGGLE, COLLAPSE)
                    .aria(EXPANDED, FALSE)
                    .aria(CONTROLS, id)
                    .textContent(CONSTANTS.details()).element());
            p.appendChild(div().css(collapse).id(id).aria(EXPANDED, FALSE)
                    .add(pre().css(wizardHalErrorText).textContent(error)).element());
        }

        stepElements.values().forEach(element -> Elements.setVisible(element, false));
        Elements.setVisible(blankSlate, true);

        cancelButton.disabled = lastStep;
        backButton.disabled = lastStep;
        nextButton.disabled = !lastStep;
        if (lastStep) {
            nextText.textContent = CONSTANTS.close();
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
            S previousState = back.back(context, state);
            if (previousState != null) {
                pushState(previousState);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void onNext() {
        if (finishCanClose) {
            // we're on the last step and have either seen a success or error message
            if (finishCallback != null) {
                finishCallback.onFinish(this, context);
            }
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
        S nextState = next.next(context, state);
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
    private void pushState(S state) {
        this.state = state;
        this.showsError = false;

        stepIndicators.forEach((s, element) -> {
            if (s == state) {
                element.classList.add(active);
            } else {
                element.classList.remove(active);
            }
        });
        Elements.setVisible(blankSlate, false);
        stepElements.forEach((s, element) -> Elements.setVisible(element, s == state));

        if (currentStep().asyncShow) {
            currentStep().onShowAndWait(context).finally_(() -> {
                cancelButton.disabled = false;
                backButton.disabled = state == initialState;
                nextButton.disabled = false;
                nextText.textContent = lastStates.contains(state) ? CONSTANTS.finish() : CONSTANTS.next();
                Elements.setVisible(nextIcon, !lastStates.contains(state));
            });
        } else {
            currentStep().onShow(context);
            cancelButton.disabled = false;
            backButton.disabled = state == initialState;
            nextButton.disabled = false;
            nextText.textContent = lastStates.contains(state) ? CONSTANTS.finish() : CONSTANTS.next();
            Elements.setVisible(nextIcon, !lastStates.contains(state));
        }
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

            HTMLLIElement li = li().css(wizardPfStep)
                    .add(a()
                            .add(span().css(wizardPfStepNumber).textContent(String.valueOf(index)))
                            .add(span().css(wizardPfStepTitle).textContent(step.title)))
                    .element();
            stepIndicators.put(status, li);
            stepsNames.appendChild(li);

            HTMLDivElement wrapper = div().css(wizardPfContents).add(step).element();
            Elements.setVisible(wrapper, false);
            mainContainer.appendChild(wrapper);
            step.attachables.forEach(Attachable::attach); // attach *after* elements have been appended!
            stepElements.put(status, wrapper);

            index++;
        }
    }

    private void close() {
        handlerRegistration.removeHandler();
        steps.values().forEach(step -> step.attachables.forEach(Attachable::detach));
        $(SELECTOR_ID).modal("hide");
    }

    // ------------------------------------------------------ inner classes

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
     * An action executed when the user clicks on the close button of the success page.
     */
    @FunctionalInterface
    public interface CloseAction<C> {

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

        public Builder(String title, C context) {
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

        public Builder<C, S> setInitialState(S initialState) {
            this.initialState = initialState;
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
}
