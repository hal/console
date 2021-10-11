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
package org.jboss.hal.ballroom.form;

import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental2.dom.CSSProperties.MarginLeftUnionType;
import elemental2.dom.CSSProperties.MarginRightUnionType;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.HelpPopover;
import org.jboss.hal.ballroom.form.AbstractFormItem.ExpressionContext;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.i;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.resources.CSS.*;

/**
 * Abstract editing appearance which builds the following DOM tree:
 * <pre>
 * &lt;div class="form-group"&gt;
 *     &lt;label class="control-label hal-form-label"&gt;&lt;/label&gt;
 *     &lt;div class="hal-form-input"&gt;
 *         &lt;input-element/&gt;
 *     &lt;/div&gt;
 * &lt;/div&gt;
 * </pre>
 */
public abstract class EditingAppearance<T> extends AbstractAppearance<T> {

    final HTMLElement root;
    final HTMLElement inputContainer;
    final HTMLInputElement inputElement;
    final String inputType;
    final HTMLElement inputGroup;
    final HTMLElement helpBlock;
    HTMLElement expressionContainer;
    HTMLElement expressionButton;
    HTMLElement suggestContainer;
    HTMLElement suggestButton;
    HTMLElement restrictedMarker;
    HTMLElement hintMarker;
    HTMLElement peekButton;
    HTMLElement peekIcon;
    HTMLElement peekContainer;
    HTMLElement helpElement;
    HTMLElement labelGroup;
    boolean masked;
    boolean attached;
    HandlerRegistration expressionHandler;

    protected EditingAppearance(Set<Decoration> supportedDecorations, HTMLInputElement inputElement) {
        super(supportedDecorations);
        this.inputElement = inputElement;
        this.inputType = inputElement.type;
        this.masked = false;
        this.root = div().css(formGroup)
                .add(labelGroup = div()
                        .css(controlLabel, halFormLabel).element())
                .add(inputContainer = div().css(halFormInput)
                        .add(inputElement).element()).element();
        labelElement = (label().css("label-text").element());
        labelGroup.appendChild(labelElement);

        this.helpElement = a()
                .css("popover-pf-info")
                .attr("role", "button")
                .attr("data-toggle", "popover")
                .attr("tabindex", "-1") // Prevents focusing the button by pressing Tab
                .add(span().css("pficon pficon-info")).
                element();
        this.inputGroup = Appearance.inputGroup();
        this.helpBlock = Appearance.helpBlock();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    /**
     * Sets the {@link #attached} flag to {@code true}. If you override this method, make sure to call {@code
     * super.attach()} first!
     */
    @Override
    public void attach() {
        attached = true;
    }

    boolean hasInputGroup() {
        return inputContainer.contains(inputGroup) && inputGroup.contains(inputElement);
    }

    void wrapInputElement() {
        Elements.failSafeRemove(inputContainer, inputElement);
        inputGroup.insertBefore(inputElement, inputGroup.firstElementChild);
        inputContainer.insertBefore(inputGroup, inputContainer.firstElementChild);
    }

    void unwrapInputElement() {
        if (Elements.failSafeRemove(inputGroup, inputElement)) {
            inputContainer.insertBefore(inputElement, inputContainer.firstElementChild);
        }
    }

    private void mask() {
        inputElement.type = "password";
        inputElement.focus();
        peekButton.title = CONSTANTS.showSensitive();
        peekIcon.classList.add("fa-eye");
        peekIcon.classList.remove("fa-eye-slash");
        masked = true;
    }

    private void unmask() {
        inputElement.type = inputType;
        inputElement.focus();
        peekButton.title = CONSTANTS.hideSensitive();
        peekIcon.classList.add("fa-eye-slash");
        peekIcon.classList.remove("fa-eye");
        masked = false;
    }


    // ------------------------------------------------------ apply decoration

    @Override
    final <C> void safeApply(Decoration decoration, C context) {
        switch (decoration) {
            case DEFAULT:
                applyDefault(String.valueOf(context));
                break;
            case DEPRECATED:
                applyDeprecated((Deprecation) context);
                break;
            case ENABLED:
                applyEnabled();
                break;
            case EXPRESSION:
                applyExpression((ExpressionContext) context);
                break;
            case HELP:
                applyHelp((SafeHtml) context);
                break;
            case HINT:
                applyHint(String.valueOf(context));
                break;
            case INVALID:
                applyInvalid(String.valueOf(context));
                break;
            case REQUIRED:
                applyRequired();
                break;
            case RESTRICTED:
                applyRestricted();
                break;
            case SENSITIVE:
                applySensitive();
                break;
            case SUGGESTIONS:
                applySuggestions((SuggestHandler) context);
                break;
            default:
                break;
        }
    }

    void applyDefault(String defaultValue) {
        inputElement.placeholder = defaultValue;
    }

    void applyDeprecated(Deprecation deprecation) {
        markAsDeprecated(deprecation);
    }

    void applyEnabled() {
        inputElement.disabled = false;
    }

    protected void applyExpression(ExpressionContext expressionContext) {
        if (expressionContainer == null) {
            expressionContainer = span().css(inputGroupBtn)
                    .add(expressionButton = button().css(btn, btnDefault).title(CONSTANTS.resolveExpression())
                            .add(i().css(fontAwesome("link"))).element()).element();
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        if (isApplied(HINT)) {
            inputGroup.insertBefore(expressionContainer, hintMarker);
            expressionButton.style.marginLeft = MarginLeftUnionType.of("-1px"); //NON-NLS
            expressionButton.style.marginRight = MarginRightUnionType.of("-1px"); //NON-NLS
        } else {
            inputGroup.appendChild(expressionContainer);
            expressionButton.style.marginLeft = MarginLeftUnionType.of(0);
            expressionButton.style.marginRight = MarginRightUnionType.of(0);
        }
        expressionHandler = bind(expressionButton, click,
                event -> expressionContext.callback.resolveExpression(inputElement.value));
    }

    void applyHint(String hint) {
        if (hintMarker == null) {
            hintMarker = Appearance.hintMarker();
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        hintMarker.textContent = hint;
        inputElement.setAttribute(UIConstants.ARIA_DESCRIBEDBY, hintMarker.id);
        inputGroup.insertBefore(hintMarker, inputElement.nextElementSibling);
    }

    private void applyHelp(SafeHtml context) {
        HelpPopover helpPopover = new HelpPopover(labelElement.title, (SafeHtml) context, helpElement);
        labelElement.setAttribute("class", labelElement.getAttribute("class") + " help-padding");
        labelGroup.appendChild(helpElement);
    }

    void applyInvalid(String errorMessage) {
        helpBlock.textContent = errorMessage;
        root.classList.add(hasError);
        inputContainer.appendChild(helpBlock);
    }

    void applyRequired() {
        markAsRequired();
    }

    void applyRestricted() {
        if (restrictedMarker == null) {
            restrictedMarker = Appearance.restrictedMarker();
        }

        inputElement.value = CONSTANTS.restricted();
        inputElement.setAttribute(UIConstants.READONLY, UIConstants.TRUE);
        inputElement.classList.add(restricted);

        // Don't ask for hasInputGroup()
        // Always clear the inputContainer and inputGroup, then wrap the input element
        Elements.removeChildrenFrom(inputContainer);
        Elements.removeChildrenFrom(inputGroup);
        wrapInputElement();
        inputGroup.appendChild(restrictedMarker);
    }

    void applySensitive() {
        if (peekIcon == null || peekButton == null) {
            peekContainer = span().css(inputGroupBtn)
                    .add(peekButton = button()
                            .css(btn, btnDefault)
                            .title(CONSTANTS.showSensitive())
                            .on(click, event -> {
                                if (masked) {
                                    unmask();
                                } else {
                                    mask();
                                }
                            })
                            .add(peekIcon = i().css(fontAwesome("eye")).element()).element()).element();
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        inputGroup.appendChild(peekContainer);
        mask();
    }

    void applySuggestions(SuggestHandler suggestHandler) {
        if (suggestContainer == null) {
            suggestContainer = span().css(inputGroupBtn)
                    .add(suggestButton = button()
                            .css(btn, btnDefault)
                            .title(CONSTANTS.showAll())
                            .on(click, event -> suggestHandler.showAll())
                            .add(i().css(fontAwesome("angle-down"))).element()).element();

            if (!hasInputGroup()) {
                wrapInputElement();
            }
            if (isApplied(SENSITIVE)) {
                peekButton.style.marginLeft = MarginLeftUnionType.of("-1px"); //NON-NLS
            }
            inputGroup.appendChild(suggestContainer);
        }
    }


    // ------------------------------------------------------ unapply decoration

    @Override
    final void safeUnapply(Decoration decoration) {
        switch (decoration) {
            case DEFAULT:
                unapplyDefault();
                break;
            case DEPRECATED:
                unapplyDeprecated();
                break;
            case ENABLED:
                unapplyEnabled();
                break;
            case EXPRESSION:
                unapplyExpression();
                break;
            case HINT:
                unapplyHint();
                break;
            case INVALID:
                unapplyInvalid();
                break;
            case REQUIRED:
                unapplyRequired();
                break;
            case RESTRICTED:
                unapplyRestricted();
                break;
            case SENSITIVE:
                unapplySensitive();
                break;
            case SUGGESTIONS:
                unapplySuggestions();
                break;
            default:
                break;
        }
    }

    void unapplyDefault() {
        // noop
    }

    void unapplyDeprecated() {
        clearDeprecation();
    }

    void unapplyEnabled() {
        inputElement.disabled = true;
    }

    void unapplyExpression() {
        if (expressionHandler != null) {
            expressionHandler.removeHandler();
            expressionHandler = null;
        }
        Elements.failSafeRemove(inputGroup, expressionContainer);
        if (!isApplied(HINT) && !isApplied(RESTRICTED) && !isApplied(SENSITIVE)) {
            unwrapInputElement();
        }
    }

    void unapplyHint() {
        inputElement.removeAttribute(UIConstants.ARIA_DESCRIBEDBY);
        Elements.failSafeRemove(inputGroup, hintMarker);
        if (!isApplied(EXPRESSION) && !isApplied(RESTRICTED) && !isApplied(SENSITIVE)) {
            unwrapInputElement();
        }
    }

    void unapplyInvalid() {
        root.classList.remove(hasError);
        Elements.failSafeRemove(inputContainer, helpBlock);
    }

    void unapplyRequired() {
        clearRequired();
    }

    void unapplyRestricted() {
        inputElement.value = "";
        inputElement.removeAttribute(UIConstants.READONLY);
        inputElement.classList.remove(restricted);

        Elements.failSafeRemove(inputGroup, restrictedMarker);
        if (!isApplied(HINT) && !isApplied(EXPRESSION)) {
            unwrapInputElement();
        }
    }

    void unapplySensitive() {
        Elements.failSafeRemove(inputGroup, peekContainer);
        if (!isApplied(EXPRESSION) && !isApplied(HINT) && !isApplied(SUGGESTIONS)) {
            unwrapInputElement();
        }
        unmask();
    }

    void unapplySuggestions() {
        Elements.failSafeRemove(inputGroup, suggestContainer);
        if (!isApplied(EXPRESSION) && !isApplied(HINT) && !isApplied(SENSITIVE)) {
            unwrapInputElement();
        }
    }


    // ------------------------------------------------------ properties & delegates

    @Override
    public void setId(String id) {
        this.id = Ids.build(id, EDITING.name().toLowerCase());
        root.dataset.set(FORM_ITEM_GROUP, this.id);
        inputElement.id = this.id;
        labelElement.htmlFor = this.id;
    }

    @Override
    public void setName(String name) {
        inputElement.name = name;
    }

    @Override
    public int getTabIndex() {
        return (int) inputElement.tabIndex;
    }

    @Override
    public void setAccessKey(char key) {
        inputElement.accessKey = String.valueOf(key);
    }

    @Override
    public void setFocus(boolean focused) {
        if (focused) {
            inputElement.focus();
        } else {
            inputElement.blur();
        }
    }

    @Override
    public void setTabIndex(int index) {
        inputElement.tabIndex = index;
    }
}