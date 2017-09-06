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
package org.jboss.hal.core.finder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;
import elemental2.dom.HTMLUListElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent;
import org.jboss.hal.core.Core;
import org.jboss.hal.core.expression.Expression;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/** Element to show the basic attributes of a resource inside the preview pane. */
public class PreviewAttributes<T extends ModelNode> implements HasElements {

    public static class PreviewAttribute {

        final String label;
        final String value;
        final SafeHtml htmlValue;
        final String href;
        final HTMLElement element;
        final boolean targetBlank;
        final Iterable<HTMLElement> elements;

        public PreviewAttribute(final String label, final String value) {
            this(label, value, null, null, false, null, null);
        }

        public PreviewAttribute(final String label, final String value, final String href) {
            this(label, value, null, href, false, null, null);
        }

        public PreviewAttribute(final String label, final String value, final String href, final boolean targetBlank) {
            this(label, value, null, href, targetBlank, null, null);
        }

        public PreviewAttribute(final String label, final SafeHtml value) {
            this(label, null, value, null, false, null, null);
        }

        public PreviewAttribute(final String label, final SafeHtml value, final String href, final boolean targetBlank) {
            this(label, null, value, href, targetBlank, null, null);
        }

        public PreviewAttribute(final String label, final Iterable<HTMLElement> elements) {
            this(label, null, null, null, false, null, elements);
        }

        public PreviewAttribute(final String label, final HTMLElement element) {
            this(label, null, null, null, false, element, null);
        }

        private PreviewAttribute(final String label, final String value, final SafeHtml htmlValue, final String href,
                final boolean targetBlank, final HTMLElement element, final Iterable<HTMLElement> elements) {
            this.label = label;
            this.value = value;
            this.htmlValue = htmlValue;
            this.href = href;
            this.element = element;
            this.elements = elements;
            this.targetBlank = targetBlank;
        }

        private boolean isUndefined() {
            return value == null && htmlValue == null;
        }

        private boolean isExpression() {
            return Expression.isExpression(value);
        }
    }


    @FunctionalInterface
    public interface PreviewAttributeFunction<T> {

        PreviewAttribute labelValue(T model);
    }


    private static final String LABEL = "label";
    private static final String VALUE = "value";
    private static final String VALUE_MARKER = "valueMarker";
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final T model;
    private final HTMLElement description;
    private final HTMLUListElement ul;
    private final LabelBuilder lb;
    private final ElementsBuilder eb;
    private final Map<String, HTMLLIElement> listItems;
    private final Map<String, PreviewAttributeFunction<T>> functions;

    public PreviewAttributes(final T model) {
        this(model, CONSTANTS.mainAttributes(), null, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final String header) {
        this(model, header, null, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final List<String> attributes) {
        this(model, CONSTANTS.mainAttributes(), null, attributes);
    }

    public PreviewAttributes(final T model, final String header, final List<String> attributes) {
        this(model, header, null, attributes);
    }

    public PreviewAttributes(final T model, final String header, final String description,
            final List<String> attributes) {
        this.model = model;
        this.functions = new HashMap<>();
        this.listItems = new HashMap<>();
        this.lb = new LabelBuilder();
        this.eb = new ElementsBuilder()
                .add(h(2, header))
                .add(this.description = p().asElement());

        if (description != null) {
            this.description.textContent = description;
        } else {
            Elements.setVisible(this.description, false);
        }

        eb.add(this.ul = ul().css(listGroup).asElement());
        attributes.forEach(this::append);
    }

    public PreviewAttributes<T> append(final String attribute) {
        append(model -> new PreviewAttribute(lb.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : ""));
        return this;
    }

    public PreviewAttributes<T> append(final String attribute, String href) {
        append(model -> new PreviewAttribute(lb.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : "",
                href));
        return this;
    }

    public PreviewAttributes<T> append(final PreviewAttributeFunction<T> function) {
        String id = Ids.uniqueId();
        String labelId = Ids.build(id, LABEL);
        String valueId = Ids.build(id, VALUE);

        HTMLElement valueContainer;
        PreviewAttribute previewAttribute = function.labelValue(model);

        HTMLLIElement li = li().id(id).css(listGroupItem)
                .add(span().id(labelId).css(key).textContent(previewAttribute.label))
                .add(valueContainer = span().id(valueId).css(CSS.value).asElement())
                .asElement();

        if (previewAttribute.elements != null || previewAttribute.element != null) {
            if (previewAttribute.elements != null) {
                previewAttribute.elements.forEach(valueContainer::appendChild);
            } else {
                valueContainer.appendChild(previewAttribute.element);
            }
        } else {
            if (previewAttribute.href != null) {
                HTMLAnchorElement anchorElement = a(previewAttribute.href).asElement();
                if (previewAttribute.targetBlank) {
                    anchorElement.target = "_blank";
                }
                valueContainer.appendChild(valueContainer = anchorElement);
            }
            if (previewAttribute.isUndefined()) {
                valueContainer.textContent = Names.NOT_AVAILABLE;
            } else if (previewAttribute.htmlValue != null) {
                valueContainer.innerHTML = previewAttribute.htmlValue.asString();
            } else {
                if (previewAttribute.isExpression()) {
                    HTMLElement resolveExpression = span().css(fontAwesome("link"), clickable, marginLeft5)
                            .title(CONSTANTS.resolveExpression())
                            .on(click, event -> Core.INSTANCE.eventBus()
                                    .fireEvent(new ResolveExpressionEvent(previewAttribute.value)))
                            .asElement();
                    HTMLElement nextValueContainer = span().asElement();
                    valueContainer.appendChild(nextValueContainer);
                    valueContainer.appendChild(resolveExpression);
                    valueContainer = nextValueContainer;
                }
                valueContainer.textContent = previewAttribute.value;
                if (previewAttribute.value.length() > 15) {
                    valueContainer.title = previewAttribute.value;
                }
            }
        }

        valueContainer.dataset.set(VALUE_MARKER, "");
        listItems.put(previewAttribute.label, li);
        functions.put(id, function);
        ul.appendChild(li);
        return this;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public void refresh(T model) {
        for (Map.Entry<String, PreviewAttributeFunction<T>> entry : functions.entrySet()) {
            String id = entry.getKey();
            String labelId = Ids.build(id, LABEL);
            String valueId = Ids.build(id, VALUE);

            PreviewAttributeFunction<T> function = entry.getValue();
            PreviewAttribute previewAttribute = function.labelValue(model);

            Element label = document.getElementById(labelId);
            if (label != null) {
                label.textContent = previewAttribute.label;
            }

            HTMLElement valueContainer = (HTMLElement) document.getElementById(valueId);
            if (valueContainer != null) {
                if (previewAttribute.elements != null) {
                    Elements.removeChildrenFrom(valueContainer);
                    previewAttribute.elements.forEach(valueContainer::appendChild);
                } else if (previewAttribute.element != null) {
                    Elements.removeChildrenFrom(valueContainer);
                    valueContainer.appendChild(previewAttribute.element);
                } else if (previewAttribute.htmlValue != null || previewAttribute.value != null) {
                    if (previewAttribute.href != null) {
                        Elements.stream(valueContainer.getElementsByTagName("a"))
                                .findFirst()
                                .ifPresent(a -> ((HTMLAnchorElement) a).href = previewAttribute.href);
                    }
                    Element valueElement;
                    if (valueContainer.dataset.has("valueMarker")) {
                        valueElement = valueContainer;
                    } else {
                        valueElement = valueContainer.querySelector("[data-value-marker]");
                    }
                    if (valueElement != null) {
                        if (previewAttribute.htmlValue != null) {
                            valueElement.innerHTML = previewAttribute.htmlValue.asString();
                        } else {
                            valueElement.textContent = previewAttribute.value;
                        }
                    }
                }
            }
        }
    }

    public void setVisible(String attribute, boolean visible) {
        Elements.setVisible(listItems.get(lb.label(attribute)), visible);
    }

    public void setDescription(String description) {
        this.description.textContent = description;
        Elements.setVisible(this.description, true);
    }

    public void setDescription(SafeHtml description) {
        this.description.innerHTML = description.asString();
        Elements.setVisible(this.description, true);
    }

    public void setDescription(HTMLElement description) {
        Elements.removeChildrenFrom(description);
        this.description.appendChild(description);
        Elements.setVisible(this.description, true);
    }

    public void hideDescription() {
        Elements.setVisible(this.description, false);
    }

    @Override
    public Iterable<HTMLElement> asElements() {
        return eb.asElements();
    }
}
