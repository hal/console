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
import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.CSS;

import static org.jboss.hal.resources.CSS.key;
import static org.jboss.hal.resources.CSS.listGroup;
import static org.jboss.hal.resources.CSS.listGroupItem;

/**
 * Element to show the basic attributes of a resource inside the preview pane.
 *
 * @author Harald Pehl
 */
public class PreviewAttributes<T extends ModelNode> implements HasElements {

    @FunctionalInterface
    public interface PreviewAttributeFunction<T> {

        String[] labelValue(T model);
    }


    private final T model;
    private final LabelBuilder labelBuilder;
    private final Elements.Builder builder;

    public PreviewAttributes(final T model, final String header) {
        this(model, header, Collections.emptyList());
    }

    public PreviewAttributes(final T model, final String header, final List<String> attributes) {
        this.model = model;
        this.labelBuilder = new LabelBuilder();
        this.builder = new Elements.Builder().h(2).textContent(header).end();

        builder.ul().css(listGroup);
        for (String attribute : attributes) {
            append(attribute);
        }
    }

    public PreviewAttributes<T> append(final String attribute) {
        String label = labelBuilder.label(attribute);
        String value = model.get(attribute).asString();
        append(label, value);
        return this;
    }

    public PreviewAttributes<T> append(final PreviewAttributeFunction<T> function) {
        String[] labelValue = function.labelValue(model);
        append(labelValue[0], labelValue[1]);
        return this;
    }

    private void append(String label, String value) {
        builder.li().css(listGroupItem)
                .span().css(key).textContent(label).end()
                .span().css(CSS.value).textContent(value);
        if (value.length() > 15) {
            builder.title(value);
        }
        builder.end().end();
    }

    public PreviewAttributes<T> end() {
        builder.end();
        return this;
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }
}
