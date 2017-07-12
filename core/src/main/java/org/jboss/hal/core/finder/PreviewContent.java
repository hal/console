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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.core.Strings;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Previews;

import static org.jboss.gwt.elemento.core.Elements.elements;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.resources.CSS.finderPreview;

/**
 * Wrapper for the preview content which consists of a header (mandatory) and one or more optional elements.
 *
 * @author Harald Pehl
 */
public class PreviewContent<T> implements HasElements, Attachable {

    private static final int MAX_HEADER_LENGTH = 30;

    private final List<Attachable> attachables;
    private final ElementsBuilder builder;

    /**
     * Empty preview w/o content
     */
    public PreviewContent(final String header) {
        this(header, (String) null);
    }

    public PreviewContent(final String header, final String lead) {
        attachables = new ArrayList<>();
        builder = elements().add(header(header));
        if (lead != null) {
            builder.add(p().css(CSS.lead).textContent(lead));
        }
    }

    public PreviewContent(final String header, final SafeHtml html) {
        this(header, null, html);
    }

    public PreviewContent(final String header, final String lead, final SafeHtml html) {
        attachables = new ArrayList<>();
        builder = elements().add(header(header));
        if (lead != null) {
            builder.add(p().css(CSS.lead).textContent(lead));
        }

        builder.add(section().innerHtml(html));
    }

    public PreviewContent(final String header, final HTMLElement first, final HTMLElement... rest) {
        this(header, null, first, rest);
    }

    public PreviewContent(final String header, final String lead, final HTMLElement first, final HTMLElement... rest) {
        attachables = new ArrayList<>();
        builder = elements().add(header(header));
        if (lead != null) {
            builder.add(p().css(CSS.lead).textContent(lead));
        }

        HtmlContentBuilder<HTMLElement> section;
        builder.add(section = section().add(first));
        if (rest != null) {
            for (HTMLElement element : rest) {
                section.add(element);
            }
        }
    }

    public PreviewContent(final String header, final HasElements elements) {
        this(header, null, elements);
    }

    public PreviewContent(final String header, final String lead, HasElements elements) {
        attachables = new ArrayList<>();
        builder = elements().add(header(header));
        if (lead != null) {
            builder.add(p().css(CSS.lead).textContent(lead));
        }
        builder.add(section().addAll(elements));
    }

    public PreviewContent(final String header, final ExternalTextResource resource) {
        this(header, null, resource);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public PreviewContent(final String header, final String lead, final ExternalTextResource resource) {
        attachables = new ArrayList<>();
        builder = elements().add(header(header));
        if (lead != null) {
            builder.add(p().css(CSS.lead).textContent(lead));
        }

        HTMLElement section;
        builder.add(section = section().asElement());
        Previews.innerHtml(section, resource);
    }

    private HTMLHeadingElement header(final String header) {
        String readableHeader = shorten(header);
        HtmlContentBuilder<HTMLHeadingElement> builder = h(1);
        if (!readableHeader.equals(header)) {
            builder.textContent(readableHeader);
            builder.title(header);
        } else {
            builder.textContent(header);
        }
        return builder.asElement();
    }

    private String shorten(String header) {
        return header.length() > MAX_HEADER_LENGTH
                ? Strings.abbreviateMiddle(header, MAX_HEADER_LENGTH)
                : header;
    }

    protected ElementsBuilder previewBuilder() {
        return builder;
    }

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }

    @Override
    public void attach() {
        PatternFly.initComponents("." + finderPreview);
        attachables.forEach(Attachable::attach);
    }

    @Override
    public Iterable<HTMLElement> asElements() {
        return builder.asElements();
    }

    @SuppressWarnings("UnusedParameters")
    public void update(T item) {}
}
