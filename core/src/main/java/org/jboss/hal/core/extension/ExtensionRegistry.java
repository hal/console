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
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.html.HeadElement;
import elemental.html.LinkElement;
import elemental.html.ScriptElement;
import elemental.js.util.JsArrayOf;
import elemental.xml.XMLHttpRequest;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.core.ApplicationReadyEvent;
import org.jboss.hal.core.ApplicationReadyEvent.ApplicationReadyHandler;
import org.jboss.hal.core.extension.ExtensionPoint.Kind;
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

        void result(int status);
    }


    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionRegistry.class);

    private final Queue<ExtensionPoint> queue;
    private final Set<String> extensionPoints;
    private boolean ready;
    private Element headerDropdown;
    private Element headerExtensions;
    private Element footerDropdown;
    private Element footerExtensions;

    @Inject
    @JsIgnore
    public ExtensionRegistry(final EventBus eventBus) {
        this.queue = new LinkedList<>();
        this.extensionPoints = new HashSet<>();
        eventBus.addHandler(ApplicationReadyEvent.getType(), this);
    }

    public void register(final ExtensionPoint extensionPoint) {
        if (!ready) {
            queue.offer(extensionPoint);
        } else {
            failSafeApply(extensionPoint);
        }
    }

    @JsIgnore
    @SuppressWarnings("HardCodedStringLiteral")
    public void ping(final Extension extension, final PingResult pingResult) {
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                pingResult.result(xhr.getStatus());
            }
        });
        xhr.addEventListener("error",  event -> pingResult.result(500), false);
        xhr.open("GET", extension.getScript(), true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    @JsIgnore
    public void inject(final Extension extension) {
        jsInject(extension.getScript(), JsHelper.asJsArray(extension.getStyles()));
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

    private void failSafeApply(ExtensionPoint extensionPoint) {
        if (ready && headerDropdown != null && headerExtensions != null &&
                footerDropdown != null && footerExtensions != null) {
            if (extensionPoints.contains(extensionPoint.id)) {
                logger.warn("Extension {} already registered", extensionPoint.id);
            } else {
                apply(extensionPoint);
            }
        } else {
            logger.error("Cannot register extension {}: Console not ready", extensionPoint.id);
        }
    }

    private void apply(ExtensionPoint extensionPoint) {
        extensionPoints.add(extensionPoint.id);
        if (extensionPoint.kind == Kind.HEADER || extensionPoint.kind == Kind.FOOTER) {
            // @formatter:off
            Element li = new Elements.Builder()
                .li()
                    .a()
                        .id(extensionPoint.id)
                        .css(clickable)
                        .textContent(extensionPoint.title)
                        .on(click, event -> extensionPoint.entryPoint.execute())
                    .end()
                .end()
            .build();
            // @formatter:on

            Element ul;
            Element dropdown;
            if (extensionPoint.kind == Kind.HEADER) {
                dropdown = headerDropdown;
                ul = headerExtensions;
            } else {
                dropdown = footerDropdown;
                ul = footerExtensions;
            }
            ul.appendChild(li);
            dropdown.getClassList().remove(hidden);

        } else if (extensionPoint.kind == Kind.FINDER_ITEM) {
            // TODO Handle finder item extensions
        }
    }


    // ------------------------------------------------------ JS methods

    @JsMethod(name = "inject")
    @SuppressWarnings("HardCodedStringLiteral")
    public void jsInject(final String script, final JsArrayOf<String> styles) {
        Document document = Browser.getDocument();
        HeadElement head = document.getHead();

        if (styles != null && !styles.isEmpty()) {
            for (int i = 0; i < styles.length(); i++) {
                LinkElement linkElement = document.createLinkElement();
                linkElement.setRel("stylesheet"); //NON-NLS
                linkElement.setHref(styles.get(i));
                head.appendChild(linkElement);
            }
        }
        ScriptElement scriptElement = document.createScriptElement();
        scriptElement.setAsync(true);
        scriptElement.setSrc(script);
        head.appendChild(scriptElement);
    }
}
