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

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.AbstractFormItem.ExpressionContext;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.EXPRESSION;
import static org.jboss.hal.ballroom.form.Decoration.HINT;
import static org.jboss.hal.ballroom.form.Decoration.SENSITIVE;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.HIDDEN;
import static org.jboss.hal.resources.UIConstants.TRUE;

/**
 * Abstract read-only appearance which builds the following DOM tree:
 * <pre>
 * &lt;div class="form-group"&gt;
 *     &lt;label class="control-label hal-form-label"&gt;&lt;/label&gt;
 *     &lt;div class="hal-form-input"&gt;
 *         &lt;value-element class="form-control-static"&gt;&lt;/p&gt;
 *     &lt;/div&gt;
 * &lt;/div&gt;
 * </pre>
 * <p>
 * Unless you override {@link #safeApply(Decoration, Object)} or {@link #safeUnapply(Decoration)} the following
 * decorations are not supported by this read-only appearance:
 * <ul>
 * <li>{@link Decoration#ENABLED}</li>
 * <li>{@link Decoration#INVALID}</li>
 * <li>{@link Decoration#REQUIRED}</li>
 * <li>{@link Decoration#SUGGESTIONS}</li>
 * </ul>
 *
 * @author Harald Pehl
 */
public abstract class ReadOnlyAppearance<T> extends AbstractAppearance<T> {

    private static final String VALUE_CONTAINER = "valueContainer";
    private static final String VALUE_ELEMENT = "valueElement";

    final HTMLElement valueContainer;
    HTMLElement valueElement;
    private final HTMLElement root;
    private final HTMLElement hintElement;
    private final HTMLElement defaultValue;
    private final HTMLElement expressionLink;
    private final HTMLElement restrictedMarker;
    private HTMLElement peekLink;
    private boolean masked;
    private String backupValue;
    private HandlerRegistration expressionHandler;

    protected ReadOnlyAppearance(Set<Decoration> supportedDecorations) {
        super(supportedDecorations);
        masked = false;

        root = div().css(formGroup)
                .add(labelElement = label().css(controlLabel, halFormLabel).asElement())
                .add(div().css(halFormInput)
                        .add(valueContainer = p().css(formControlStatic)
                                .add(valueElement = span().asElement())
                                .asElement()))
                .asElement();

        hintElement = span().css(hint).asElement();
        defaultValue = span()
                .css(CSS.defaultValue)
                .title(CONSTANTS.defaultValue())
                .asElement();
        expressionLink = span()
                .css(CSS.fontAwesome("link"), clickable)
                .title(CONSTANTS.resolveExpression())
                .asElement();
        restrictedMarker = span()
                .add(span().css(fontAwesome("lock"), marginRight5).aria(HIDDEN, TRUE))
                .add(span().textContent(CONSTANTS.restricted()))
                .asElement();
        peekLink = span()
                .css(fontAwesome("eye"), clickable)
                .title(CONSTANTS.showSensitive())
                .on(click, event -> {
                    if (masked) {
                        unmask();
                    } else {
                        mask();
                    }
                })
                .asElement();
        Elements.setVisible(peekLink, false);
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void attach() {
        // noop
    }


    // ------------------------------------------------------ value

    @Override
    public void showValue(final T value) {
        String stringValue = asString(value);
        valueElement.textContent = stringValue;
        if (Strings.isNullOrEmpty(stringValue)) {
            valueElement.classList.add(empty);
        } else {
            valueElement.classList.remove(empty);
        }

        if (isApplied(SENSITIVE)) {
            if (masked) {
                mask();
            } else {
                unmask();
            }
        }
        Elements.setVisible(peekLink, isApplied(SENSITIVE) && !Strings.isNullOrEmpty(stringValue));
    }

    @Override
    public void clearValue() {
        valueElement.textContent = "";
        valueElement.classList.add(empty);
        if (isApplied(SENSITIVE)) {
            Elements.setVisible(peekLink, false);
        }
    }

    @Override
    public void showExpression(final String expression) {
        valueElement.textContent = expression;
        if (Strings.isNullOrEmpty(expression)) {
            valueElement.classList.add(empty);
        } else {
            valueElement.classList.remove(empty);
        }

        if (isApplied(SENSITIVE)) {
            if (masked) {
                mask();
            } else {
                unmask();
            }
        }
        Elements.setVisible(peekLink, isApplied(SENSITIVE) && !Strings.isNullOrEmpty(expression));
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void mask() {
        backupValue = valueElement.textContent;
        valueElement.textContent = backupValue.replaceAll(".", "\u25CF");
        peekLink.title = CONSTANTS.showSensitive();
        peekLink.classList.add("fa-eye");
        peekLink.classList.remove("fa-eye-slash");
        masked = true;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void unmask() {
        valueElement.textContent = Strings.nullToEmpty(backupValue);
        peekLink.title = CONSTANTS.hideSensitive();
        peekLink.classList.add("fa-eye-slash");
        peekLink.classList.remove("fa-eye");
        masked = false;
    }


    // ------------------------------------------------------ decorations

    @Override
    protected <C> void safeApply(final Decoration decoration, final C context) {
        switch (decoration) {

            case DEFAULT:
                defaultValue.textContent = String.valueOf(context);
                if (isApplied(HINT)) {
                    valueContainer.insertBefore(defaultValue, hintElement);
                } else {
                    valueContainer.appendChild(defaultValue);
                }
                break;

            case DEPRECATED:
                markAsDeprecated((Deprecation) context);
                break;

            case EXPRESSION:
                ExpressionContext ec = (ExpressionContext) context;
                expressionHandler = bind(expressionLink, click,
                        event -> ec.callback.resolveExpression(masked ? backupValue : valueElement.textContent));
                if (isApplied(HINT)) {
                    valueContainer.insertBefore(expressionLink, hintElement);
                } else {
                    valueContainer.appendChild(expressionLink);
                }
                break;

            case HINT:
                hintElement.textContent = String.valueOf(context);
                valueContainer.appendChild(hintElement);
                break;

            case RESTRICTED:
                valueElement.textContent = "";
                Elements.removeChildrenFrom(valueContainer);
                valueContainer.appendChild(restrictedMarker);
                break;

            case SENSITIVE:
                if (isApplied(EXPRESSION)) {
                    valueContainer.insertBefore(peekLink, expressionLink);
                } else {
                    valueContainer.appendChild(peekLink);
                }
                mask();
                break;

            // not supported
            case ENABLED:
            case INVALID:
            case REQUIRED:
            case SUGGESTIONS:
                break;
        }
    }

    @Override
    protected void safeUnapply(final Decoration decoration) {
        switch (decoration) {

            case DEFAULT:
                Elements.failSafeRemove(valueContainer, defaultValue);
                break;

            case DEPRECATED:
                clearDeprecation();
                break;

            case EXPRESSION:
                if (expressionHandler != null) {
                    expressionHandler.removeHandler();
                    expressionHandler = null;
                }
                Elements.failSafeRemove(valueContainer, expressionLink);
                break;

            case HINT:
                Elements.failSafeRemove(valueContainer, hintElement);
                break;

            case RESTRICTED:
                Elements.failSafeRemove(valueContainer, restrictedMarker);
                break;

            case SENSITIVE:
                Elements.failSafeRemove(valueContainer, peekLink);
                unmask();
                break;

            // not supported
            case ENABLED:
            case INVALID:
            case REQUIRED:
            case SUGGESTIONS:
                break;
        }
    }


    // ------------------------------------------------------ properties & delegates

    @Override
    public void setId(final String id) {
        this.id = Ids.build(id, READONLY.name().toLowerCase());
        root.dataset.set(FORM_ITEM_GROUP, this.id);
        valueElement.id = this.id;
    }

    @Override
    public void setName(final String name) {
        // noop
    }

    @Override
    public int getTabIndex() {
        return -1;
    }

    @Override
    public void setAccessKey(final char key) {
        // noop
    }

    @Override
    public void setFocus(final boolean focused) {
        // noop
    }

    @Override
    public void setTabIndex(final int index) {
        // noop
    }
}
