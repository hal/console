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
package org.jboss.hal.ballroom.typeahead;

import elemental.js.xml.JsXMLHttpRequest;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Mapping for the settings used in the {@linkplain org.jboss.hal.ballroom.typeahead.RemoteOptions.Preparator#prepare
 * prepare} callback from typeahead.js. Actually this maps to the jQuery ajax settings.
 *
 * @author Harald Pehl
 * @see <a href="https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#remote">https://github.com/twitter/typeahead.js/blob/master/doc/bloodhound.md#remote</a>
 * @see <a href="https://api.jquery.com/jquery.ajax/#jQuery-ajax-settings">https://api.jquery.com/jquery.ajax/#jQuery-ajax-settings</a>
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class AjaxSettings {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Accepts {
        public String text;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class XHRFields {
        public boolean withCredentials;
    }


    @JsFunction
    @FunctionalInterface
    public interface BeforeSend {
        void beforeSend(JsXMLHttpRequest xhr, AjaxSettings settings);
    }

    @JsFunction
    @FunctionalInterface
    public interface ErrorCallback {
        void onError(JsXMLHttpRequest xhr, String textStatus, String errorThrown);
    }


    public Accepts accepts;
    public BeforeSend beforeSend;
    public ErrorCallback error;
    public String contentType;
    public String data;
    public String dataType;
    public String method;
    public XHRFields xhrFields;
}
