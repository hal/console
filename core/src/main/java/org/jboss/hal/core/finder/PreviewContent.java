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
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.core.Strings;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.CSS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the preview content which consists of a header (mandatory) and one or more optional elements.
 *
 * @author Harald Pehl
 */
public class PreviewContent implements HasElements, SecurityContextAware, Attachable {

    protected static final String CONTENT_ELEMENT = "contentRepository";

    private static final String ERROR_MESSAGE = "Unable to get preview content from '{}': {}";
    private static final Logger logger = LoggerFactory.getLogger(PreviewContent.class);
    private static final int MAX_HEADER_LENGTH = 30;


    private final Elements.Builder builder;
    private final List<Attachable> attachables;
    private boolean attached;

    /**
     * Empty preview w/o content
     */
    public PreviewContent(final String header) {
        this(header, (String)null);
    }

    public PreviewContent(final String header, final String lead) {
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        attachables = new ArrayList<>();
        attached = false;
    }

    public PreviewContent(final String header, final SafeHtml html) {
        this(header, null, html);
    }

    public PreviewContent(final String header, final String lead, final SafeHtml html) {
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        builder.section().innerHtml(html).end();
        attachables = new ArrayList<>();
        attached = false;
    }

    public PreviewContent(final String header, final Element first, final Element... rest) {
        this(header, null, first, rest);
    }

    public PreviewContent(final String header, final String lead, final Element first, final Element... rest) {
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
        attachables = new ArrayList<>();
        attached = false;
    }

    public PreviewContent(final String header, final ExternalTextResource resource) {
        this(header, null, resource);
    }

    public PreviewContent(final String header, final String lead, final ExternalTextResource resource) {
        builder = header(header);
        if (lead != null) {
            builder.p().css(CSS.lead).textContent(lead).end();
        }
        builder.section().rememberAs(CONTENT_ELEMENT).end();
        Element content = builder.referenceFor(CONTENT_ELEMENT);

        if (resource != null) {
            try {
                resource.getText(new ResourceCallback<TextResource>() {
                    @Override
                    public void onError(final ResourceException e) {
                        logger.error(ERROR_MESSAGE, resource.getName(), e.getMessage());
                    }

                    @Override
                    public void onSuccess(final TextResource textResource) {
                        SafeHtml html = SafeHtmlUtils.fromSafeConstant(textResource.getText());
                        content.setInnerHTML(html.asString());
                    }
                });
            } catch (ResourceException e) {
                logger.error(ERROR_MESSAGE, resource.getName(), e.getMessage());
            }
        }
        attachables = new ArrayList<>();
        attached = false;
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

    protected void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }

    protected <A extends Attachable> void registerAttachables(List<A> attachables) {
        this.attachables.addAll(attachables);
    }

    protected Elements.Builder previewBuilder() {
        return builder;
    }

    @Override
    public Iterable<Element> asElements() {
        return builder.elements();
    }

    @Override
    public void attach() {
        //noinspection Duplicates
        if (!attached) {
            PatternFly.initComponents();
            for (Attachable attachable : attachables) {
                attachable.attach();
            }
            attached = true;
        }
    }

    public void onReset() {

    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
