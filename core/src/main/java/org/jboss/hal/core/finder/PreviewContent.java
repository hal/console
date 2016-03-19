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

import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the preview content which consists of a header (mandatory) and one or more optional elements.
 *
 * @author Harald Pehl
 */
public class PreviewContent implements SecurityContextAware {

    protected static final String CONTENT_ELEMENT = "content";
    private static final String ERROR_MESSAGE = "Unable to get preview content from '{}': {}";
    private static final Logger logger = LoggerFactory.getLogger(PreviewContent.class);

    protected Elements.Builder builder;

    /**
     * Empty preview w/o content
     */
    public PreviewContent(final String header) {
        builder = header(header);
    }

    public PreviewContent(final String header, final SafeHtml html) {
        builder = header(header).section().innerHtml(html).end();
    }

    public PreviewContent(final String header, final Element first, final Element... rest) {
        builder = header(header).section().rememberAs(CONTENT_ELEMENT).add(first);
        if (rest != null) {
            for (Element element : rest) {
                builder.add(element);
            }
        }
        builder.end();
    }

    public PreviewContent(final String header, final ExternalTextResource resource) {
        builder = header(header).section().rememberAs(CONTENT_ELEMENT).end();
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
    }

    private Elements.Builder header(final String header) {
        return new Elements.Builder().h(1).textContent(header).end();
    }

    public Iterable<Element> elements() {
        return builder.elements();
    }

    @Override
    public void onSecurityContextChange(final SecurityContext securityContext) {

    }
}
