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
package org.jboss.hal.processor.mbui;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.jetbrains.annotations.NonNls;
import org.junit.Before;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class MbuiTest {

    @Before
    public void setUp() {
        MbuiViewProcessor.resetCounter();
    }

    @Test
    public void simpleForm() {
        Compilation compilation = compile("SimpleFormView");
        assertSourceEquals(compilation, "Mbui_SimpleFormView");
    }

    @Test
    public void attributesForm() {
        Compilation compilation = compile("AttributesFormView");
        assertSourceEquals(compilation, "Mbui_AttributesFormView");
    }

    @Test
    public void runtimeForm() {
        Compilation compilation = compile("RuntimeFormView");
        assertSourceEquals(compilation, "Mbui_RuntimeFormView");
    }

    @Test
    public void formItemProviderForm() {
        Compilation compilation = compile("FormItemProviderView");
        assertSourceEquals(compilation, "Mbui_FormItemProviderView");
    }

    @Test
    public void onSaveForm() {
        Compilation compilation = compile("OnSaveFormView");
        assertSourceEquals(compilation, "Mbui_OnSaveFormView");
    }

    @Test
    public void handlebars() {
        Compilation compilation = compile("HandlebarsView");
        assertSourceEquals(compilation, "Mbui_HandlebarsView");
    }

    private Compilation compile(@NonNls String source) {
        return javac()
                .withProcessors(new MbuiViewProcessor())
                .compile(JavaFileObjects.forResource(MbuiTest.class.getResource(source + ".java")));
    }

    private void assertSourceEquals(final Compilation compilation, final String source) {
        assertThat(compilation)
                .generatedSourceFile(MbuiTest.class.getPackage().getName() + "." + source)
                .hasSourceEquivalentTo(JavaFileObjects.forResource(MbuiTest.class.getResource(source + ".java")));
    }
}
