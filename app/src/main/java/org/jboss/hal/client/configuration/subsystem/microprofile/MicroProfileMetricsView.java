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
package org.jboss.hal.client.configuration.subsystem.microprofile;

import java.util.List;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.MICRO_PROFILE_METRICS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPOSED_SUBSYSTEMS;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;

public class MicroProfileMetricsView extends HalViewImpl implements MicroProfileMetricsPresenter.MyView {

    private final SwitchItem exposeAll;
    private final FormItem<List<String>> exposedSubsystems;
    private final Form<ModelNode> form;
    private MicroProfileMetricsPresenter presenter;

    @Inject
    public MicroProfileMetricsView(MetadataRegistry metadataRegistry, Dispatcher dispatcher,
            StatementContext statementContext, Resources resources) {
        exposeAll = new SwitchItem("expose-all-subsystems", "Expose All Subsystems");
        exposeAll.addValueChangeHandler(event -> toggleSubsystems(event.getValue()));

        Metadata metadata = metadataRegistry.lookup(MICRO_PROFILE_METRICS_TEMPLATE);
        form = new ModelNodeForm.Builder<>(Ids.MICRO_PROFILE_METRICS_FORM, metadata)
                .unboundFormItem(exposeAll, 0)
                .prepareReset(form -> presenter.reset(form))
                .onSave((f, changedValues) -> presenter.save(changedValues))
                .build();
        exposedSubsystems = form.getFormItem(EXPOSED_SUBSYSTEMS);
        exposedSubsystems.registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext,
                AddressTemplate.of("{selected.profile}/subsystem=*")));
        form.addFormValidation(f -> {
            if (!exposeAll.getValue() && exposedSubsystems.getValue().isEmpty()) {
                exposedSubsystems.showError(resources.constants().requiredField());
                return ValidationResult.invalid("Please select at least one subsystem");
            }
            return ValidationResult.OK;
        });
        registerAttachable(form);

        HTMLElement root = row()
                .add(column()
                        .add(h(1).textContent(Names.MICROPROFILE_METRICS))
                        .add(p().textContent(metadata.getDescription().getDescription()))
                        .add(form))
                .asElement();
        initElement(root);
    }

    @Override
    public void setPresenter(MicroProfileMetricsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(ModelNode payload) {
        form.view(payload);

        List<String> subsystemValues = failSafeList(payload, EXPOSED_SUBSYSTEMS).stream()
                .map(ModelNode::asString)
                .collect(toList());
        exposeAll.setValue(subsystemValues.contains("*"));
        toggleSubsystems(exposeAll.getValue());
    }

    private void toggleSubsystems(boolean enableAllValue) {
        Elements.setVisible(exposedSubsystems.asElement(Form.State.EDITING), !enableAllValue);
        if (enableAllValue) {
            exposedSubsystems.setValue(asList("*"));
        } else {
            if (!exposedSubsystems.isUndefined() && exposedSubsystems.getValue().size() == 1 &&
                    exposedSubsystems.getValue().get(0).equals("*")) {
                exposedSubsystems.clearValue();
            }
            exposedSubsystems.setFocus(true);
        }
    }
}
