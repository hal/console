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
package org.jboss.hal.ballroom.dialog;

/**
 * A dialog w/o any means to close the dialog (no close icon, no esc handler). The only way to close this dialog is using the
 * {@link #close()} method.
 */
@SuppressWarnings("WeakerAccess")
public class BlockingDialog extends Dialog {

    public BlockingDialog(Dialog.Builder builder) {
        super(builder.closeIcon(false).closeOnEsc(false));
    }

    @Override
    public void close() {
        super.close();
    }
}
