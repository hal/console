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
package org.jboss.hal.ballroom.autocomplete;

import java.util.List;

import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.json.JsonObject;

/**
 * Abstract result processor which makes it easy to unit test the processor. If your result processor is really trivial
 * and does not need to be tested, there's no need to extend from this class.
 * <p>
 * The bulk of work should be placed into {@link #processToModel(String, ModelNode)} which can easily be unit tested.
 * Whereas {@link #asJson(List)} should contain no logic other than mapping the model to json.
 *
 * @author Harald Pehl
 */
abstract class AbstractResultProcessor<T> implements ResultProcessor {

    @Override
    public final JsonObject[] process(final String query, final ModelNode nodes) {
        return asJson(processToModel(query, nodes));
    }

    @Override
    public final JsonObject[] process(final String query, final CompositeResult compositeResult) {
        return asJson(processToModel(query, compositeResult));
    }

    protected abstract List<T> processToModel(final String query, final ModelNode nodes);

    protected abstract List<T> processToModel(final String query, final CompositeResult compositeResult);

    abstract JsonObject[] asJson(final List<T> models);
}
