/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.managementinterface;

import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;

import com.google.gwt.safehtml.shared.SafeHtml;

/** Not a real presenter, but common methods for {@code HostPresenter} and {@code StandaloneServerPresenter} */
public interface ConstantHeadersPresenter {

    void addConstantHeaderPath(ModelNode payload, SafeHtml successMessage);

    void saveConstantHeaderPath(int index, String path, SafeHtml successMessage);

    void removeConstantHeaderPath(int index, String path, SafeHtml successMessage);

    void addHeader(int pathIndex, ModelNode model, SafeHtml successMessage);

    void saveHeader(int pathIndex, int index, String header, Metadata metadata, Map<String, Object> changedValues,
            SafeHtml successMessage);

    void removeHeader(int pathIndex, int index, String header, SafeHtml successMessage);
}
