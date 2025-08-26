/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.installer;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.ModelDescriptionConstants;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;

class CertificatePreview extends PreviewContent<CertificateInfo> {

    CertificatePreview(CertificateInfo certificate) {
        super(certificate.getKeyID());

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<CertificateInfo> attributes = new PreviewAttributes<>(certificate);
        attributes
                .append(model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(KEY_ID), certificate.getKeyID()));
        attributes.append(
                model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(ModelDescriptionConstants.FINGERPRINT),
                        certificate.getFingerprint()));
        attributes.append(
                model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(DESCRIPTION), certificate.getDescription()));
        attributes
                .append(model -> new PreviewAttributes.PreviewAttribute(labelBuilder.label(STATUS), certificate.getStatus()));

        previewBuilder().addAll(attributes);
    }
}
