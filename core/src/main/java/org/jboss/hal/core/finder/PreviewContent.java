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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.core.Strings;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Previews;

import static org.jboss.hal.resources.CSS.finderPreview;

/**
 * Wrapper for the preview content which consists of a header (mandatory) and one or more optional elements.
 *
 * @author Harald Pehl
 */
public class PreviewContent<T> implements HasElements, Attachable, SecurityContextAware {

    private static final String CONTENT_ELEMENT = "contentElement";
    private static final int MAX_HEADER_LENGTH = 30;

    private final List<Attachable> attachables;
    private final Elements.Builder builder;

    /**
     * Empty preview w/o content
     */
    public PreviewContent(final String header) {
        this(header, (String) null);
    }

    public PreviewContent(final String header, final String lead) {
        attachables = new ArrayList<>();
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
    }

    public PreviewContent(final String header, final SafeHtml html) {
        this(header, null, html);
    }

    public PreviewContent(final String header, final String lead, final SafeHtml html) {
        attachables = new ArrayList<>();
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        builder.section().innerHtml(html).end();
    }

    public PreviewContent(final String header, final Element first, final Element... rest) {
        this(header, null, first, rest);
    }

    public PreviewContent(final String header, final String lead, final Element first, final Element... rest) {
        attachables = new ArrayList<>();
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        builder.section().rememberAs(CONTENT_ELEMENT).add(first);
        if (rest != null) {
            for (Element element : rest) {
                builder.add(element);
            }
        }
        builder.end();
    }

    public PreviewContent(final String header, final ExternalTextResource resource) {
        this(header, null, resource);
    }

    public PreviewContent(final String header, final String lead, final ExternalTextResource resource) {
        attachables = new ArrayList<>();
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        builder.section().rememberAs(CONTENT_ELEMENT).end();
        Element content = builder.referenceFor(CONTENT_ELEMENT);
        Previews.innerHtml(content, resource);
    }

    private Elements.Builder header(final String header) {
        String readableHeader = header.length() > MAX_HEADER_LENGTH
                ? Strings.abbreviateMiddle(header, MAX_HEADER_LENGTH)
                : header;
        Elements.Builder builder = new Elements.Builder().h(1).textContent(readableHeader);
        if (!readableHeader.equals(header)) {
            builder.title(header);
        }
        return builder.end();
    }

    protected Elements.Builder previewBuilder() {
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
    public Iterable<Element> asElements() {
        return builder.elements();
    }

    @SuppressWarnings("UnusedParameters")
    public void update(T item) {}

    public Element getContentSection() {
        return builder.referenceFor(CONTENT_ELEMENT);
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
