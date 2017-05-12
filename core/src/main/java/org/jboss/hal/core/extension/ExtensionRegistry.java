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
package org.jboss.hal.core.extension;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.html.HeadElement;
import elemental.html.LinkElement;
import elemental.html.ScriptElement;
import elemental.js.util.JsArrayOf;
import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonObject;
import elemental.xml.XMLHttpRequest;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.ApplicationReadyEvent.ApplicationReadyHandler;
import org.jboss.hal.core.extension.Extension.Point;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;

/**
 * @author Harald Pehl
 */
@JsType
public class ExtensionRegistry implements ApplicationReadyHandler {

    @FunctionalInterface
    public interface PingResult {

        void result(int status, JsonObject json);
    }


    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionRegistry.class);

    private final Queue<Extension> queue;
    private final Set<String> extensions;
    private boolean ready;
    private Element headerDropdown;
    private Element headerExtensions;
    private Element footerDropdown;
    private Element footerExtensions;

    @Inject
    @JsIgnore
    public ExtensionRegistry(final EventBus eventBus) {
        this.queue = new LinkedList<>();
        this.extensions = new HashSet<>();
        eventBus.addHandler(ApplicationReadyEvent.getType(), this);
    }

    public void register(final Extension extension) {
        if (!ready) {
            queue.offer(extension);
        } else {
            failSafeApply(extension);
        }
    }

    @JsIgnore
    @SuppressWarnings("HardCodedStringLiteral")
    public void ping(final String url, final PingResult pingResult) {
        SafeUri safeUrl = UriUtils.fromString(url);
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                String responseText = xhr.getResponseText();
                if (Strings.isNullOrEmpty(responseText)) {
                    pingResult.result(415, null); // 415 - Unsupported Media Type
                } else {
                    JsonObject extensionJson = null;
                    try {
                        extensionJson = Json.parse(responseText);
                    } catch (JsonException e) {
                        logger.error("Unable to parse {} as JSON", safeUrl.asString());
                        pingResult.result(500, null);
                    }
                    pingResult.result(xhr.getStatus(), extensionJson);
                }
            }
        });
        xhr.addEventListener("error",  event -> pingResult.result(503, null), false);
        xhr.open("GET", safeUrl.asString(), true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    @JsIgnore
    public void inject(final String script, final List<String> stylesheets) {
        jsInject(script, JsHelper.asJsArray(stylesheets));
    }

    @Override
    @JsIgnore
    @SuppressWarnings("HardCodedStringLiteral")
    public void onApplicationReady(final ApplicationReadyEvent event) {
        ready = true;
        headerDropdown = Browser.getDocument().getElementById(Ids.HEADER_EXTENSIONS_DROPDOWN);
        headerExtensions = Browser.getDocument().getElementById(Ids.HEADER_EXTENSIONS);
        footerDropdown = Browser.getDocument().getElementById(Ids.FOOTER_EXTENSIONS_DROPDOWN);
        footerExtensions = Browser.getDocument().getElementById(Ids.FOOTER_EXTENSIONS);

        while (!queue.isEmpty()) {
            failSafeApply(queue.poll());
        }
    }

    private void failSafeApply(Extension extension) {
        if (ready && headerDropdown != null && headerExtensions != null &&
                footerDropdown != null && footerExtensions != null) {
            if (extensions.contains(extension.name)) {
                logger.warn("Extension {} already registered", extension.name);
            } else {
                apply(extension);
            }
        } else {
            logger.error("Cannot register extension {}: Console not ready", extension.name);
        }
    }

    private void apply(Extension extension) {
        extensions.add(extension.name);
        if (extension.point == Extension.Point.HEADER || extension.point == Point.FOOTER) {
            // @formatter:off
            Element li = new Elements.Builder()
                .li()
                    .a()
                        .id(extension.name)
                        .css(clickable)
                        .textContent(extension.title)
                        .on(click, event -> extension.entryPoint.execute())
                    .end()
                .end()
            .build();
            // @formatter:on

            Element ul;
            Element dropdown;
            if (extension.point == Point.HEADER) {
                dropdown = headerDropdown;
                ul = headerExtensions;
            } else {
                dropdown = footerDropdown;
                ul = footerExtensions;
            }
            ul.appendChild(li);
            dropdown.getClassList().remove(hidden);

        } else if (extension.point == Extension.Point.FINDER_ITEM) {
            // TODO Handle finder item extensions
        }
    }


    // ------------------------------------------------------ JS methods

    @JsMethod(name = "inject")
    @SuppressWarnings("HardCodedStringLiteral")
    public void jsInject(final String script, final JsArrayOf<String> stylesheets) {
        Document document = Browser.getDocument();
        HeadElement head = document.getHead();

        if (stylesheets != null && !stylesheets.isEmpty()) {
            for (int i = 0; i < stylesheets.length(); i++) {
                LinkElement linkElement = document.createLinkElement();
                linkElement.setRel("stylesheet"); //NON-NLS
                linkElement.setHref(stylesheets.get(i));
                head.appendChild(linkElement);
            }
        }
        ScriptElement scriptElement = document.createScriptElement();
        scriptElement.setAsync(true);
        scriptElement.setSrc(script);
        head.appendChild(scriptElement);
    }
}
