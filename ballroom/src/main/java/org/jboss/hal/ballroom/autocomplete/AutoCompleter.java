/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.autocomplete;

import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;

/**
 * @author Harald Pehl
 */
public class Autocompleter {

    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void autocomplete(Options options);
    }


    public static class Builder {

        private final FormItem<String> formItem;
        private final Dispatcher dispatcher;
        private final Operation operation;
        private final ResultProcessor resultProcessor;
        private int delay;
        private int minLength;

        public Builder(final FormItem<String> formItem,
                final Dispatcher dispatcher,
                final Operation operation,
                final ResultProcessor resultProcessor) {
            this.formItem = formItem;
            this.dispatcher = dispatcher;
            this.operation = operation;
            this.resultProcessor = resultProcessor;
            this.delay = 300;
            this.minLength = 1;
        }

        public Builder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public Autocompleter build() {
            return new Autocompleter(this);
        }
    }


    private final FormItem<String> formItem;
    private final Dispatcher dispatcher;
    private final Operation operation;
    private final ResultProcessor resultProcessor;
    private final Options options;

    @SuppressWarnings("HardCodedStringLiteral")
    Autocompleter(Builder builder) {
        formItem = builder.formItem;
        dispatcher = builder.dispatcher;
        operation = builder.operation;
        resultProcessor = builder.resultProcessor;

        options = new Options();
        options.appendTo = formItem.getId(EDITING);
        options.autoFocus = false;
        options.delay = builder.delay;
        options.minLength = builder.minLength;
        options.position = new Position();
        options.position.at = "left bottom";
        options.position.my = "left top";
        options.position.collision = "flip";
        options.source = (request, response) -> dispatcher.execute(operation,
                result -> {
                    JsArrayOf<String> data = resultProcessor.process(formItem.getValue(), result);
                    response.onResponse(data);
                },
                (failedOp, failure) -> {
                    response.onResponse(JsArrayOf.create());
                },
                (exceptionalOp, exception) -> {
                    response.onResponse(JsArrayOf.create());
                });
    }

    public void attach() {
        Bridge.select("#" + formItem.getId(EDITING)).autocomplete(options);
    }
}
