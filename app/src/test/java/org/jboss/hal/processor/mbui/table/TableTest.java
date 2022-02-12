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
package org.jboss.hal.processor.mbui.table;

import org.jboss.hal.processor.mbui.MbuiViewProcessorTest;
import org.junit.Test;

import com.google.testing.compile.Compilation;

@SuppressWarnings("DuplicateStringLiteralInspection")
public class TableTest extends MbuiViewProcessorTest {

    @Test
    public void simple() {
        Compilation compilation = compile("SimpleView");
        assertSourceEquals(compilation, "Mbui_SimpleView");
    }

    @Test
    public void defaultActions() {
        Compilation compilation = compile("DefaultActionsView");
        assertSourceEquals(compilation, "Mbui_DefaultActionsView");
    }

    @Test
    public void addWithAttributesActions() {
        Compilation compilation = compile("AddWithAttributesView");
        assertSourceEquals(compilation, "Mbui_AddWithAttributesView");
    }

    @Test
    public void addWithSuggestHandlerActions() {
        Compilation compilation = compile("AddWithSuggestHandlerView");
        assertSourceEquals(compilation, "Mbui_AddWithSuggestHandlerView");
    }

    @Test
    public void customAction() {
        Compilation compilation = compile("CustomActionView");
        assertSourceEquals(compilation, "Mbui_CustomActionView");
    }

    @Test
    public void addConstraintAction() {
        Compilation compilation = compile("AddConstraintActionView");
        assertSourceEquals(compilation, "Mbui_AddConstraintActionView");
    }

    @Test
    public void removeConstraintAction() {
        Compilation compilation = compile("RemoveConstraintActionView");
        assertSourceEquals(compilation, "Mbui_RemoveConstraintActionView");
    }

    @Test
    public void customConstraintAction() {
        Compilation compilation = compile("CustomConstraintActionView");
        assertSourceEquals(compilation, "Mbui_CustomConstraintActionView");
    }

    @Test
    public void customColumn() {
        Compilation compilation = compile("CustomColumnView");
        assertSourceEquals(compilation, "Mbui_CustomColumnView");
    }
}
