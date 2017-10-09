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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.ballroom.form;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.resources.Constants;

public class RequiredValidation<T> implements FormItemValidation<T> {

    private final static Constants CONSTANTS = GWT.create(Constants.class);

    private final FormItem<T> formItem;
    private final String errorMessage;

    RequiredValidation(final FormItem<T> formItem) {
        this.formItem = formItem;
        errorMessage = CONSTANTS.requiredField();
    }

    @Override
    public ValidationResult validate(final T value) {
        boolean valid = true;
        if (formItem.isRequired()) {
            valid = !formItem.isEmpty();
        }
        return valid ? ValidationResult.OK : ValidationResult.invalid(errorMessage);
    }
}
