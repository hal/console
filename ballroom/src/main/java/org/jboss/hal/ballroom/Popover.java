/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom;

import com.google.gwt.safehtml.shared.SafeHtml;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

public class Popover {

    private final Bridge bridge;

    private Popover(Builder builder) {
        Options options = new Options();
        options.content = builder.content.asString();
        options.html = true;
        options.sanitize = false;
        options.placement = builder.placement.id;
        if (builder.template != null) {
            options.template = builder.template.asString();
        }
        options.title = builder.title;
        options.trigger = builder.trigger.id;
        bridge = Bridge.$(builder.selector).popover(options);
    }

    public void show() {
        bridge.popover("show");
    }

    public void toggle() {
        bridge.popover("toggle");
    }

    public void hide() {
        bridge.popover("hide");
    }

    public void destroy() {
        bridge.popover("destroy");
    }

    public void onInserted(Callback callback) {
        bridge.on("inserted.bs.popover", callback);
    }

    public void onShown(Callback callback) {
        bridge.on("shown.bs.popover", callback);
    }

    public void onHidden(Callback callback) {
        bridge.on("hidden.bs.popover", callback);
    }


    public static class Builder {

        private final String selector;
        private final String title;
        private final SafeHtml content;
        private SafeHtml template;
        private Placement placement;
        private Trigger trigger;

        public Builder(String selector, String title, SafeHtml content) {
            this.selector = selector;
            this.title = title;
            this.content = content;
            this.placement = Placement.RIGHT;
            this.trigger = Trigger.CLICK;
        }

        public Builder placement(Placement placement) {
            this.placement = placement;
            return this;
        }

        public Builder trigger(Trigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder template(SafeHtml template) {
            this.template = template;
            return this;
        }

        public Popover build() {
            return new Popover(this);
        }
    }


    public enum Placement {
        AUTO("auto"), TOP("top"), BOTTOM("bottom"), LEFT("left"), RIGHT("right");

        private final String id;

        Placement(String id) {
            this.id = id;
        }
    }


    public enum Trigger {
        CLICK("click"), HOVER("hover"), FOCUS("focus"), MANUAL("manual");

        private final String id;

        Trigger(String id) {
            this.id = id;
        }
    }


    @JsType(isNative = true)
    private static class Bridge {

        @JsMethod(namespace = GLOBAL)
        static native Bridge $(@NonNls String selector);

        native Bridge popover(Options options);

        native void popover(@NonNls String method);

        native void on(@NonNls String event, Callback callback);
    }


    @JsFunction
    @FunctionalInterface
    public interface Callback {

        void action();
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    private static class Options {

        String content;
        boolean html;
        boolean sanitize;
        String placement;
        String template;
        String title;
        String trigger;
    }
}
