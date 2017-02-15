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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.form.AbstractFormItem.ExpressionContext;
import org.jboss.hal.dmr.model.Deprecation;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

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
    protected static final String VALUE_ELEMENT = "valueElement";

    final Element valueContainer;
    protected Element valueElement;
    private final Element root;
    private final Element hintElement;
    private final Element defaultValue;
    private final Element expressionLink;
    private final Element restrictedMarker;

    protected ReadOnlyAppearance(Set<Decoration> supportedDecorations) {
        super(supportedDecorations);

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(formGroup)
                .label().css(controlLabel, halFormLabel).rememberAs(LABEL_ELEMENT).end()
                .div().css(halFormInput)
                    .p().css(formControlStatic).rememberAs(VALUE_CONTAINER)
                        .span().rememberAs(VALUE_ELEMENT).end()
                    .end()
                .end()
            .end();
        // @formatter:on

        labelElement = builder.referenceFor(LABEL_ELEMENT);
        valueContainer = builder.referenceFor(VALUE_CONTAINER);
        valueElement = builder.referenceFor(VALUE_ELEMENT);
        root = builder.build();

        hintElement = new Elements.Builder().span().css(hint).end().build();
        defaultValue = new Elements.Builder().span()
                .css(CSS.defaultValue)
                .title(CONSTANTS.defaultValue())
                .end().build();
        expressionLink = new Elements.Builder().span()
                .css(CSS.fontAwesome("link"), clickable)
                .title(CONSTANTS.resolveExpression())
                .end().build();
        restrictedMarker = new Elements.Builder().span()
                .css(fontAwesome("lock"))
                .aria(HIDDEN, TRUE)
                .textContent(CONSTANTS.restricted())
                .end().build();
    }

    @Override
    public Element asElement() {
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
        valueElement.setTextContent(stringValue);
        if (Strings.isNullOrEmpty(stringValue)) {
            valueElement.getClassList().add(empty);
        } else {
            valueElement.getClassList().remove(empty);
        }
    }

    @Override
    public void showExpression(final String expression) {
        valueElement.setTextContent(expression);
        if (Strings.isNullOrEmpty(expression)) {
            valueElement.getClassList().add(empty);
        } else {
            valueElement.getClassList().remove(empty);
        }
    }

    @Override
    public void clearValue() {
        valueElement.setTextContent("");
        valueElement.getClassList().add(empty);
    }


    // ------------------------------------------------------ decorations

    @Override
    protected <C> void safeApply(final Decoration decoration, final C context) {
        switch (decoration) {

            case DEFAULT:
                defaultValue.setTextContent(String.valueOf(context));
                valueContainer.appendChild(defaultValue);
                break;

            case DEPRECATED:
                markAsDeprecated((Deprecation) context);
                break;

            case EXPRESSION:
                ExpressionContext ec = (ExpressionContext) context;
                expressionLink.setOnclick(event -> ec.callback.resolveExpression(valueElement.getTextContent()));
                valueContainer.appendChild(expressionLink);
                break;

            case HINT:
                hintElement.setTextContent(String.valueOf(context));
                valueContainer.appendChild(hintElement);
                break;

            case RESTRICTED:
                valueElement.setTextContent("");
                valueContainer.appendChild(restrictedMarker);
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
                Elements.failSafeRemove(valueContainer, expressionLink);
                break;

            case HINT:
                Elements.failSafeRemove(valueContainer, hintElement);
                break;

            case RESTRICTED:
                Elements.failSafeRemove(valueContainer, restrictedMarker);
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
        root.getDataset().setAt(FORM_ITEM_GROUP, this.id);
        valueElement.setId(this.id);
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
