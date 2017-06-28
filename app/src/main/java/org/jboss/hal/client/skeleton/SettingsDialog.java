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
package org.jboss.hal.client.skeleton;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.dialog.DialogFactory;
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
import static org.jboss.hal.config.Settings.Key.COLLECT_USER_DATA;
import static org.jboss.hal.config.Settings.Key.LOCALE;
import static org.jboss.hal.config.Settings.Key.PAGE_LENGTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;

/**
 * @author Harald Pehl
 */
class SettingsDialog {

    private static final SettingsResources RESOURCES = GWT.create(SettingsResources.class);

    private final Settings settings;
    private final ModifyResourceDialog dialog;
    private final boolean multipleLocales;
    private boolean changes;

    SettingsDialog(final Environment environment, final Settings settings, final Resources resources) {
        this.settings = settings;
        this.changes = false;

        List<String> locales = environment.getLocales();
        locales.sort(naturalOrder());
        multipleLocales = locales.size() > 1;

        Metadata metadata = Metadata.staticDescription(RESOURCES.settings());
        if (multipleLocales) {
            Property locale = metadata.getDescription().findAttribute(ATTRIBUTES, LOCALE.key());
            if (locale != null && locale.getValue().hasDefined(ALLOWED)) {
                locales.forEach(l -> locale.getValue().get(ALLOWED).add(l));
            }
        }

        List<String> attributes = new ArrayList<>();
        attributes.add(COLLECT_USER_DATA.key());
        if (multipleLocales) {
            attributes.add(LOCALE.key());
        }
        attributes.add(PAGE_LENGTH.key());
        ModelNodeForm<ModelNode> form = new ModelNodeForm.Builder<>(Ids.SETTINGS_FORM, metadata)
                .include(attributes)
                .customFormItem(PAGE_LENGTH.key(),
                        attributeDescription -> new NumberSelectItem(PAGE_LENGTH.key(), new long[]{10, 20, 50}))
                .build();

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
        modelNode.get(COLLECT_USER_DATA.key()).set(settings.get(COLLECT_USER_DATA).asBoolean());
        if (multipleLocales) {
            modelNode.get(LOCALE.key()).set(settings.get(LOCALE).value());
        }
        modelNode.get(PAGE_LENGTH.key()).set(settings.get(PAGE_LENGTH).asInt(Settings.DEFAULT_PAGE_LENGTH));
        dialog.show(modelNode);
    }
}
