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
package org.jboss.hal.meta.description;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.jboss.hal.db.Document;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static elemental2.dom.DomGlobal.console;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@JsType
public class ResourceDescriptionDocument extends Document {

    String payload;

    public ResourceDescriptionDocument(ResourceAddress address, ResourceDescription payload) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this._id = address.toString();
        this.payload = payload.toBase64String();
        console.log("RDD(" + address.toString() + "): " + stopwatch.stop().elapsed(MILLISECONDS));
    }

    @JsIgnore
    public ResourceAddress getAddress() {
        return AddressTemplate.of(_id).resolve(StatementContext.NOOP);
    }

    @JsIgnore
    public ResourceDescription getResourceDescription() {
        return new ResourceDescription(ModelNode.fromBase64(payload));
    }
}
