/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.skeleton;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.NumberSelectItem;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.Settings.Key;
import org.jboss.hal.core.mbui.dialog.ModifyResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.window;
import static java.util.Comparator.naturalOrder;
import static org.jboss.hal.config.Settings.Key.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;

class SettingsDialog {

    private static final SettingsResources RESOURCES = GWT.create(SettingsResources.class);

    private final Settings settings;
    private final ModifyResourceDialog dialog;
    private final boolean multipleLocales;
    private boolean changes;
    private FormItem<Long> pollTimeFormItem;
    private int defaultPollTime;

    SettingsDialog(Environment environment, Settings settings, Resources resources) {
        this.settings = settings;
        this.changes = false;

        List<String> locales = environment.getLocales();
        locales.sort(naturalOrder());
        multipleLocales = locales.size() > 1;

        Metadata metadata = Metadata.staticDescription(RESOURCES.settings());
        defaultPollTime = metadata.getDescription().get(ATTRIBUTES).get(POLL_TIME.key()).get(DEFAULT).asInt();
        if (multipleLocales) {
            Property locale = metadata.getDescription().findAttribute(ATTRIBUTES, LOCALE.key());
            if (locale != null && locale.getValue().hasDefined(ALLOWED)) {
                locales.forEach(l -> locale.getValue().get(ALLOWED).add(l));
            }
        }
        List<String> attributes = new ArrayList<>();
        attributes.add(TITLE.key());
        attributes.add(COLLECT_USER_DATA.key());
        if (multipleLocales) {
            attributes.add(LOCALE.key());
        }
        attributes.add(PAGE_SIZE.key());
        long[] values = new long[Settings.PAGE_SIZE_VALUES.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = Settings.PAGE_SIZE_VALUES[i];
        }
        attributes.add(POLL.key());
        attributes.add(POLL_TIME.key());
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.SETTINGS_FORM, metadata)
                .include(attributes)
                .customFormItem(PAGE_SIZE.key(),
                        attributeDescription -> new NumberSelectItem(PAGE_SIZE.key(), values))
                .build();
        form.<Boolean>getFormItem(POLL.key()).addValueChangeHandler(ev -> togglePollTime(ev.getValue()));
        pollTimeFormItem = form.getFormItem(POLL_TIME.key());

        dialog = new ModifyResourceDialog(resources.constants().settings(), form,
                (f, changedValues) -> {
                    changedValues.forEach((key, value) -> settings.set(Key.from(key), value));
                    changes = !changedValues.isEmpty();
                },
                () -> {
                    if (changes) {
                        DialogFactory.showConfirmation(resources.constants().settings(),
                                resources.messages().reloadSettings(), window.location::reload);
                    }
                });
    }

    void show() {
        ModelNode modelNode = new ModelNode();
        modelNode.get(TITLE.key()).set(Strings.nullToEmpty(settings.get(TITLE).value()));
        modelNode.get(COLLECT_USER_DATA.key()).set(settings.get(COLLECT_USER_DATA).asBoolean());
        boolean pollEnabled = settings.get(POLL).asBoolean();
        modelNode.get(POLL.key()).set(pollEnabled);
        togglePollTime(pollEnabled);
        modelNode.get(POLL_TIME.key()).set(settings.get(POLL_TIME).asInt(defaultPollTime));
        if (multipleLocales) {
            modelNode.get(LOCALE.key()).set(settings.get(LOCALE).value());
        }
        modelNode.get(PAGE_SIZE.key()).set(settings.get(PAGE_SIZE).asInt(Settings.DEFAULT_PAGE_SIZE));
        dialog.show(modelNode);
    }

    private void togglePollTime(boolean enableAllValue) {
        Elements.setVisible(pollTimeFormItem.element(Form.State.EDITING), enableAllValue);
        if (enableAllValue) {
            pollTimeFormItem.setValue((long) settings.get(POLL_TIME).asInt(defaultPollTime));
        } else {
            pollTimeFormItem.clearValue();
        }
    }
}
