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
package org.jboss.hal.client.configuration.subsystem.elytron;

import com.google.gwt.safehtml.shared.SafeHtml;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.PropertiesItem;

/**
 * Custom Properties Item that displays and edit a form item whose attribute value is a LIST of OBJECT, that contains
 * a name=value pair, this custom implementaion allows to set the property key name and value.
 *
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class CustomPropertiesItem extends PropertiesItem {

    private String keyName;
    private String valueName;

    public CustomPropertiesItem(final String name,
            final SafeHtml inputHelp, final String viewSeparator) {
        super(name, new LabelBuilder().label(name), inputHelp, viewSeparator);
    }

    public void setPropertyValue(String keyName, String valueName) {
        this.keyName = keyName;
        this.valueName = valueName;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getValueName() {
        return valueName;
    }
}
