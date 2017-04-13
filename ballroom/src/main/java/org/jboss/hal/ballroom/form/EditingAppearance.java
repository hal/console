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

import java.util.Set;

import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.AbstractFormItem.ExpressionContext;
import org.jboss.hal.dmr.model.Deprecation;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static elemental.css.CSSStyleDeclaration.Unit.PX;
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
 *
 * @author Harald Pehl
 */
@SuppressWarnings("WeakerAccess")
public abstract class EditingAppearance<T> extends AbstractAppearance<T> {

    private static final String EXPRESSION_BUTTON = "expressionButton";
    private static final String SUGGEST_BUTTON = "suggestButton";
    private static final String PEEK_BUTTON = "peekButton";
    private static final String PEEK_ICON = "peekIcon";

    final Element root;
    final Element inputContainer;
    final InputElement inputElement;
    final String inputType;
    final Element inputGroup;
    final Element helpBlock;
    Element expressionContainer;
    Element expressionButton;
    Element suggestContainer;
    Element suggestButton;
    Element restrictedMarker;
    Element hintMarker;
    Element peekButton;
    Element peekIcon;
    Element peekContainer;
    boolean masked;
    boolean attached;

    protected EditingAppearance(Set<Decoration> supportedDecorations, elemental.html.InputElement inputElement) {
        super(supportedDecorations);
        this.inputElement = inputElement;
        this.inputType = inputElement.getType();
        this.masked = false;

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(formGroup)
                .label().css(controlLabel, halFormLabel).rememberAs(LABEL_ELEMENT).end()
                .div().css(halFormInput).rememberAs(INPUT_CONTAINER)
                    .add(inputElement)
                .end()
            .end();
        // @formatter:on

        inputGroup = Appearance.inputGroup();
        helpBlock = Appearance.helpBlock();

        labelElement = builder.referenceFor(LABEL_ELEMENT);
        inputContainer = builder.referenceFor(INPUT_CONTAINER);
        root = builder.build();
    }

    @Override
    public Element asElement() {
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
        inputGroup.insertBefore(inputElement, inputGroup.getFirstElementChild());
        inputContainer.insertBefore(inputGroup, inputContainer.getFirstElementChild());
    }

    void unwrapInputElement() {
        if (Elements.failSafeRemove(inputGroup, inputElement)) {
            inputContainer.insertBefore(inputElement, inputContainer.getFirstElementChild());
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void mask() {
        inputElement.setType("password");
        inputElement.focus();
        peekButton.setTitle(CONSTANTS.showSensitive());
        peekIcon.getClassList().add("fa-eye");
        peekIcon.getClassList().remove("fa-eye-slash");
        masked = true;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void unmask() {
        inputElement.setType(inputType);
        inputElement.focus();
        peekButton.setTitle(CONSTANTS.hideSensitive());
        peekIcon.getClassList().add("fa-eye-slash");
        peekIcon.getClassList().remove("fa-eye");
        masked = false;
    }


    // ------------------------------------------------------ apply decoration

    @Override
    final <C> void safeApply(final Decoration decoration, final C context) {
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
        }
    }

    void applyDefault(final String defaultValue) {
        inputElement.setPlaceholder(defaultValue);
    }

    void applyDeprecated(final Deprecation deprecation) {
        markAsDeprecated(deprecation);
    }

    void applyEnabled() {
        inputElement.setDisabled(false);
    }

    protected void applyExpression(final ExpressionContext expressionContext) {
        if (expressionContainer == null) {
            // @formatter:off
            Elements.Builder expressionBuilder = new Elements.Builder()
                .span().css(inputGroupBtn)
                    .button().css(btn, btnDefault).title(CONSTANTS.resolveExpression()).rememberAs(EXPRESSION_BUTTON)
                        .start("i").css(fontAwesome("link")).end()
                    .end()
                .end();
            expressionButton = expressionBuilder.referenceFor(EXPRESSION_BUTTON);
            expressionContainer = expressionBuilder.build();
            // @formatter:on
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        if (isApplied(HINT)) {
            inputGroup.insertBefore(expressionContainer, hintMarker);
            expressionButton.getStyle().setMarginLeft(-1, PX);
            expressionButton.getStyle().setMarginRight(-1, PX);
        } else {
            inputGroup.appendChild(expressionContainer);
            expressionButton.getStyle().clearMarginLeft();
            expressionButton.getStyle().clearMarginRight();
        }
        expressionButton.setOnclick(event -> expressionContext.callback.resolveExpression(inputElement.getValue()));
    }

    void applyHint(final String hint) {
        if (hintMarker == null) {
            hintMarker = Appearance.hintMarker();
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        hintMarker.setTextContent(hint);
        inputElement.setAttribute(UIConstants.ARIA_DESCRIBEDBY, hintMarker.getId());
        inputGroup.insertBefore(hintMarker, inputElement.getNextElementSibling());
    }

    void applyInvalid(final String errorMessage) {
        helpBlock.setTextContent(errorMessage);
        root.getClassList().add(hasError);
        inputContainer.appendChild(helpBlock);
    }

    void applyRequired() {
        markAsRequired();
    }

    void applyRestricted() {
        if (restrictedMarker == null) {
            restrictedMarker = Appearance.restrictedMarker();
        }

        inputElement.setValue(CONSTANTS.restricted());
        inputElement.setAttribute(UIConstants.READONLY, UIConstants.TRUE);
        inputElement.getClassList().add(restricted);

        // Don't ask for hasInputGroup()
        // Always clear the inputContainer and inputGroup, then wrap the input element
        Elements.removeChildrenFrom(inputContainer);
        Elements.removeChildrenFrom(inputGroup);
        wrapInputElement();
        inputGroup.appendChild(restrictedMarker);
    }

    void applySensitive() {
        if (peekIcon == null || peekButton == null) {
            // @formatter:off
            Elements.Builder builder = new Elements.Builder()
                .span().css(inputGroupBtn)
                    .button().css(btn, btnDefault)
                             .rememberAs(PEEK_BUTTON)
                             .title(CONSTANTS.showSensitive())
                             .on(click, event -> {
                                 if (masked) {
                                     unmask();
                                 } else {
                                     mask();
                                 }
                             })
                        .start("i").css(fontAwesome("eye")).rememberAs(PEEK_ICON).end()
                    .end()
                .end();
            // @formatter:on
            peekButton = builder.referenceFor(PEEK_BUTTON);
            peekIcon = builder.referenceFor(PEEK_ICON);
            peekContainer = builder.build();
        }

        if (!hasInputGroup()) {
            wrapInputElement();
        }
        inputGroup.appendChild(peekContainer);
        mask();
    }

    void applySuggestions(final SuggestHandler suggestHandler) {
        if (suggestContainer == null) {
            // @formatter:off
            Elements.Builder suggestBuilder = new Elements.Builder()
                .span().css(inputGroupBtn)
                    .button().css(btn, btnDefault).title(CONSTANTS.showAll()).rememberAs(SUGGEST_BUTTON)
                        .start("i").css(fontAwesome("angle-down")).end()
                    .end()
                .end();
            suggestButton = suggestBuilder.referenceFor(SUGGEST_BUTTON);
            suggestContainer = suggestBuilder.build();
            // @formatter:on

            if (!hasInputGroup()) {
                wrapInputElement();
            }
            if (isApplied(SENSITIVE)) {
                peekButton.getStyle().setMarginLeft(-1, PX);
            }
            inputGroup.appendChild(suggestContainer);
            suggestButton.setOnclick(event -> suggestHandler.showAll());
        }
    }


    // ------------------------------------------------------ unapply decoration

    @Override
    final void safeUnapply(final Decoration decoration) {
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
        }
    }

    void unapplyDefault() {
        // noop
    }

    void unapplyDeprecated() {
        clearDeprecation();
    }

    void unapplyEnabled() {
        inputElement.setDisabled(true);
    }

    void unapplyExpression() {
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
        root.getClassList().remove(hasError);
        Elements.failSafeRemove(inputContainer, helpBlock);
    }

    void unapplyRequired() {
        clearRequired();
    }

    void unapplyRestricted() {
        inputElement.setValue("");
        inputElement.removeAttribute(UIConstants.READONLY);
        inputElement.getClassList().remove(restricted);

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
    public void setId(final String id) {
        this.id = Ids.build(id, EDITING.name().toLowerCase());
        root.getDataset().setAt(FORM_ITEM_GROUP, this.id);
        inputElement.setId(this.id);
        labelElement.setHtmlFor(this.id);
    }

    @Override
    public void setName(final String name) {
        inputElement.setName(name);
    }

    @Override
    public int getTabIndex() {
        return inputElement.getTabIndex();
    }

    @Override
    public void setAccessKey(final char key) {
        inputElement.setAccessKey(String.valueOf(key));
    }

    @Override
    public void setFocus(final boolean focused) {
        if (focused) {
            inputElement.focus();
        } else {
            inputElement.blur();
        }
    }

    @Override
    public void setTabIndex(final int index) {
        inputElement.setTabIndex(index);
    }
}