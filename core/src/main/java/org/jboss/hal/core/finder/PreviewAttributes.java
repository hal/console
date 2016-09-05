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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.resources.CSS.key;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listGroupItem;

/**
 * Element to show the basic attributes of a resource inside the preview pane.
 *
 * @author Harald Pehl
 */
public class PreviewAttributes<T extends ModelNode> implements HasElements {

    public static class PreviewAttribute {

        final String label;
        final String value;
        final String href;

        public PreviewAttribute(final String label, final String value) {
            this(label, value, null);
        }

        public PreviewAttribute(final String label, final String value, final String href) {
            this.label = label;
            this.value = value;
            this.href = href;
        }
    }


    @FunctionalInterface
    public interface PreviewAttributeFunction<T> {

        PreviewAttribute labelValue(T model);
    }


    private static final String LABEL = "label";
    private static final String VALUE = "value";
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final T model;
    private final LabelBuilder labelBuilder;
    private final Elements.Builder builder;
    private final Map<String, PreviewAttributeFunction<T>> functions;
    private final Map<String, Element> attributeElements;
    private Element lastAttributeGroupItem;

    public PreviewAttributes(final T model) {
        this(model, CONSTANTS.mainAttributes(), Collections.emptyList());
    }

    public PreviewAttributes(final T model, final String header) {
        this(model, header, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final List<String> attributes) {
        this(model, CONSTANTS.mainAttributes(), attributes);
    }

    public PreviewAttributes(final T model, final String header, final List<String> attributes) {
        this.model = model;
        this.labelBuilder = new LabelBuilder();
        this.builder = new Elements.Builder().h(2).textContent(header).end();
        this.functions = new HashMap<>();
        this.attributeElements = new HashMap<>();

        builder.ul().css(listGroup);
        attributes.forEach(this::append);
    }

    public PreviewAttributes<T> append(final String attribute) {
        append(model -> new PreviewAttribute(labelBuilder.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : ""));
        attributeElements.put(attribute, lastAttributeGroupItem);
        return this;
    }

    public PreviewAttributes<T> append(final String attribute, String href) {
        append(model -> new PreviewAttribute(labelBuilder.label(attribute),
                model.hasDefined(attribute) ? model.get(attribute).asString() : "",
                href));
        attributeElements.put(attribute, lastAttributeGroupItem);
        return this;
    }

    public PreviewAttributes<T> append(final PreviewAttributeFunction<T> function) {
        String id = Ids.uniqueId();
        String labelId = Ids.build(id, LABEL);
        String valueId = Ids.build(id, VALUE);
        functions.put(id, function);

        PreviewAttribute previewAttribute = function.labelValue(model);
        // @formatter:off
        builder.li().rememberAs(id).css(listGroupItem)
            .span().rememberAs(labelId).css(key).textContent(previewAttribute.label).end();
            if (previewAttribute.href != null) {
                builder.a(previewAttribute.href);
            }
            builder.span().rememberAs(valueId).css(CSS.value).textContent(previewAttribute.value);
            if (previewAttribute.value.length() > 15) {
                builder.title(previewAttribute.value);
            }
            builder.end(); // </span>
            if (previewAttribute.href != null) {
                builder.end(); // </a>
            }
        builder.end(); // </li>
        // @formatter:on

        lastAttributeGroupItem = builder.referenceFor(id);
        return this;
    }

    public PreviewAttributes<T> end() {
        builder.end();
        return this;
    }

    public void refresh(T model) {
        for (Map.Entry<String, PreviewAttributeFunction<T>> entry : functions.entrySet()) {
            String id = entry.getKey();
            String labelId = Ids.build(id, LABEL);
            String valueId = Ids.build(id, VALUE);

            PreviewAttributeFunction<T> function = entry.getValue();
            PreviewAttribute previewAttribute = function.labelValue(model);

            builder.referenceFor(labelId).setTextContent(previewAttribute.label);
            builder.referenceFor(valueId).setTextContent(previewAttribute.value);
        }
    }

    public void setVisible(String attribute, boolean visible) {
        Elements.setVisible(attributeElements.get(attribute), visible);
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }
}
