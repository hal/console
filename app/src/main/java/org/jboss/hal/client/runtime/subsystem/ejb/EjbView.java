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
package org.jboss.hal.client.runtime.subsystem.ejb;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.ejb.AddressTemplates.ejbDeploymentTemplate;

public class EjbView extends HalViewImpl implements EjbPresenter.MyView {

    private final HTMLElement header;
    private final HTMLElement lead;
    private final Map<EjbNode.Type, Form<EjbNode>> forms;

    @Inject
    public EjbView(MetadataRegistry metadataRegistry) {
        forms = new HashMap<>();
        for (EjbNode.Type type : EjbNode.Type.values()) {
            Form<EjbNode> form = ejbForm(type, metadataRegistry);
            Elements.setVisible(form.asElement(), false);
            forms.put(type, form);
        }

        initElement(row()
                .add(column()
                        .add(header = h(1).asElement())
                        .add(lead = p().css(CSS.lead).asElement())
                        .addAll(forms.values().toArray(new IsElement[forms.values().size()]))));
    }

    private Form<EjbNode> ejbForm(EjbNode.Type type, MetadataRegistry metadataRegistry) {
        Metadata metadata = metadataRegistry.lookup(ejbDeploymentTemplate(type));
        String id = Ids.build(Ids.EJB3_DEPLOYMENT, type.name(), Ids.FORM);
        return new ModelNodeForm.Builder<EjbNode>(id, metadata)
                .readOnly()
                .includeRuntime()
                .build();
    }

    @Override
    public void update(EjbNode ejb) {
        header.textContent = ejb.getName();
        lead.textContent = ejb.type.type;
        forms.get(ejb.type).view(ejb);
        forms.forEach((type, form) -> Elements.setVisible(form.asElement(), type == ejb.type));
    }
}
