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
package org.jboss.hal.processor.mbui.form;

import org.jboss.hal.processor.mbui.MbuiViewProcessorTest;
import org.junit.Test;

import com.google.testing.compile.Compilation;

@SuppressWarnings("DuplicateStringLiteralInspection")
public class FormTest extends MbuiViewProcessorTest {

    @Test
    public void simple() {
        Compilation compilation = compile("SimpleView");
        assertSourceEquals(compilation, "Mbui_SimpleView");
    }

    @Test
    public void attributes() {
        Compilation compilation = compile("AttributesView");
        assertSourceEquals(compilation, "Mbui_AttributesView");
    }

    @Test
    public void attributeGroups() {
        Compilation compilation = compile("AttributeGroupsView");
        assertSourceEquals(compilation, "Mbui_AttributeGroupsView");
    }

    @Test
    public void runtime() {
        Compilation compilation = compile("RuntimeView");
        assertSourceEquals(compilation, "Mbui_RuntimeView");
    }

    @Test
    public void formItemProvider() {
        Compilation compilation = compile("FormItemProviderView");
        assertSourceEquals(compilation, "Mbui_FormItemProviderView");
    }

    @Test
    public void unboundFormItem() {
        Compilation compilation = compile("UnboundFormItemView");
        assertSourceEquals(compilation, "Mbui_UnboundFormItemView");
    }

    @Test
    public void suggestHandler() {
        Compilation compilation = compile("SuggestHandlerView");
        assertSourceEquals(compilation, "Mbui_SuggestHandlerView");
    }

    @Test
    public void saveHandler() {
        Compilation compilation = compile("SaveHandlerView");
        assertSourceEquals(compilation, "Mbui_SaveHandlerView");
    }

    @Test
    public void failSafe() {
        Compilation compilation = compile("FailSafeView");
        assertSourceEquals(compilation, "Mbui_FailSafeView");
    }
}
