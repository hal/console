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

import java.util.List;

import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.dmr.ModelNode;

/**
 * Abstract result processor for implementations which makes it easy to unit test the processor. If your result
 * processor is really trivial and does not need to be tested, there's no need to extend from this class.
 *
 * @author Harald Pehl
 */
abstract class AbstractResultProcessor<T> implements ResultProcessor {

    @Override
    public JsArrayOf<JsJsonObject> process(final String query, final ModelNode result) {
        return asJson(processToModel(query, result));
    }

    protected abstract List<T> processToModel(final String query, final ModelNode result);

    protected abstract JsArrayOf<JsJsonObject> asJson(final List<T> models);
}
