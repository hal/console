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
package org.jboss.hal.core.mbui.form;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;

/**
 *
 * A helper class to create ModelNodeForm that uses an OBJECT attribute that contains other nested attributes, the
 * metadata is re-constructed to repackage the nested attributes in the attributes path.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class ComplexAttributeModelNodeForm {

    private Metadata metadata;
    private String parentId;
    private String complexAttributeName;

    public ComplexAttributeModelNodeForm(String parentId, Metadata metadata, String complexAttributeName) {
        this.parentId = parentId;
        this.complexAttributeName = complexAttributeName;

        this.metadata = metadata.repackageComplexAttribute(complexAttributeName, false, false);
    }

    public ModelNodeForm.Builder<NamedNode> builder() {
        ModelNodeForm.Builder<NamedNode> form = new ModelNodeForm.Builder<NamedNode>(
                Ids.build(parentId, complexAttributeName, Ids.FORM_SUFFIX), metadata)
                .unsorted();
        return form;
    }

    public ModelNodeForm<NamedNode> build() {
        ModelNodeForm<NamedNode> form = builder().build();
        return form;
    }
}
