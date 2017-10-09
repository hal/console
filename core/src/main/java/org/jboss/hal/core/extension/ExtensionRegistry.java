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
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadElement;
import elemental2.dom.HTMLLinkElement;
import elemental2.dom.HTMLScriptElement;
import elemental2.dom.XMLHttpRequest;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.ApplicationReadyEvent.ApplicationReadyHandler;
import org.jboss.hal.core.extension.Extension.Point;
import org.jboss.hal.json.Json;
import org.jboss.hal.json.JsonObject;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.EsParam;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.hidden;

/** Registry to manage HAL extensions written in JavaScript. */
@JsType(namespace = "hal.core")
public class ExtensionRegistry implements ApplicationReadyHandler {

    @FunctionalInterface
    public interface MetadataCallback {

        void result(int status, JsonObject json);
    }


    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionRegistry.class);

    private final Queue<Extension> queue;
    private final Set<String> extensions;
    private boolean ready;
    private elemental2.dom.Element headerDropdown;
    private elemental2.dom.Element headerExtensions;
    private elemental2.dom.Element footerDropdown;
    private elemental2.dom.Element footerExtensions;

    @Inject
    @JsIgnore
    public ExtensionRegistry(final EventBus eventBus) {
        this.queue = new LinkedList<>();
        this.extensions = new HashSet<>();
        eventBus.addHandler(ApplicationReadyEvent.getType(), this);
    }

    @JsIgnore
    public void verifyMetadata(final String url, final MetadataCallback metadataCallback) {
        SafeUri safeUrl = UriUtils.fromString(url);
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.onload = event -> {
            int status = (int) xhr.status;
            if (status >= 200 && status < 400) {
                String responseText = xhr.responseText;
                if (Strings.isNullOrEmpty(responseText)) {
                    metadataCallback.result(415, null); // 415 - Unsupported Media Type
                } else {
                    JsonObject extensionJson = Json.parse(responseText);
                    metadataCallback.result(status, extensionJson);
                }
            } else {
                metadataCallback.result(status, null);
            }
        };
        xhr.addEventListener("error", event -> metadataCallback.result(503, null), false); //NON-NLS
        xhr.open(GET.name(), safeUrl.asString(), true);
        xhr.send();
    }

    @JsIgnore
    public boolean verifyScript(final String script) {
        return document.head.querySelector("script[src='" + script + "']") != null; //NON-NLS
    }

    @JsIgnore
    public void inject(final String script, final List<String> stylesheets) {
        jsInject(script, stylesheets.toArray(new String[stylesheets.size()]));
    }

    @Override
    @JsIgnore
    @SuppressWarnings("HardCodedStringLiteral")
    public void onApplicationReady(final ApplicationReadyEvent event) {
        ready = true;
        headerDropdown = document.getElementById(Ids.HEADER_EXTENSIONS_DROPDOWN);
        headerExtensions = document.getElementById(Ids.HEADER_EXTENSIONS);
        footerDropdown = document.getElementById(Ids.FOOTER_EXTENSIONS_DROPDOWN);
        footerExtensions = document.getElementById(Ids.FOOTER_EXTENSIONS);

        while (!queue.isEmpty()) {
            failSafeApply(queue.poll());
        }
    }

    /**
     * Registers an extension. Use this method to register your extension.
     * <p>
     * If the extension is already registered, this method will do nothing.
     *
     * @param extension the extension to register.
     */
    public void register(final Extension extension) {
        if (!ready) {
            queue.offer(extension);
        } else {
            failSafeApply(extension);
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
            HTMLElement li = li()
                    .add(a().id(extension.name)
                            .css(clickable)
                            .textContent(extension.title)
                            .on(click, event -> extension.entryPoint.execute())
                            .asElement())
                    .asElement();

            elemental2.dom.Element ul;
            elemental2.dom.Element dropdown;
            if (extension.point == Point.HEADER) {
                dropdown = headerDropdown;
                ul = headerExtensions;
            } else {
                dropdown = footerDropdown;
                ul = footerExtensions;
            }
            ul.appendChild(li);
            dropdown.classList.remove(hidden);

        } else if (extension.point == Extension.Point.FINDER_ITEM) {
            // TODO Handle finder item extensions
        }
    }


    // ------------------------------------------------------ JS methods

    /**
     * Injects the script and stylesheets of an extension. This method is used during development. Normally you don't
     * have to call this method.
     *
     * @param script      the extension's script.
     * @param stylesheets an optional list of stylesheets.
     */
    @JsMethod(name = "inject")
    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public void jsInject(String script, @EsParam("string[]") String[] stylesheets) {
        HTMLHeadElement head = document.head;

        if (stylesheets != null && stylesheets.length != 0) {
            for (String stylesheet : stylesheets) {
                HTMLLinkElement linkElement = (HTMLLinkElement) document.createElement("link");
                linkElement.rel = "stylesheet";
                linkElement.href = stylesheet;
                head.appendChild(linkElement);
            }
        }
        HTMLScriptElement scriptElement = (HTMLScriptElement) document.createElement("script");
        scriptElement.src = script;
        scriptElement.setAttribute("async", true);
        head.appendChild(scriptElement);
    }
}
