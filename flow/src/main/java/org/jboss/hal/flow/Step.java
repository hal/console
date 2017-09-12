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
package org.jboss.hal.flow;

import rx.Single;
import rx.functions.Func1;

/** Encapsulates one operation inside a flow */
public interface Step<C> extends Func1<C, Single<C>> {

    /**
     * Execute the step. Please make sure that you <strong>always</strong> call either {@link Control#proceed()} or
     * {@link Control#abort(String)}.
     */
    void execute(C context, Control control);

    @Override default Single<C> call(C ctx) {
        return Single.fromEmitter(emitter -> execute(ctx, new Control() {
            @Override public void proceed() { emitter.onSuccess(ctx); }
            @Override public void abort(String error) { emitter.onError(new FlowException(error, ctx)); }
        }));
    }
}
