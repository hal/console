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
package org.jboss.hal.core.subsystem.elytron;


import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;

/**
 * Provides building blocks for {@code credential-reference} used in several resources across subsystems.
 */
// TODO Add a way to validate the alternatives between 'credential-reference` and other attributes *across* forms
public class CredentialReference {

    private final MetadataRegistry metadataRegistry;

    public CredentialReference(MetadataRegistry metadataRegistry) {this.metadataRegistry = metadataRegistry;}

    public Form<ModelNode> form(String baseId, AddressTemplate template) {
        Metadata metadata = metadataRegistry.lookup(template).forComplexAttribute(CREDENTIAL_REFERENCE);
        return new ModelNodeForm.Builder<>(Ids.build(baseId, CREDENTIAL_REFERENCE, Ids.FORM_SUFFIX), metadata)
                .build();
    }
}
