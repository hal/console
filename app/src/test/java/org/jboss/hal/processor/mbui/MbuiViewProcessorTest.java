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
package org.jboss.hal.processor.mbui;

import javax.tools.JavaFileObject;

import org.junit.Before;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@SuppressWarnings("HardCodedStringLiteral")
public abstract class MbuiViewProcessorTest {

    @Before
    public void setUp() {
        MbuiViewProcessor.resetCounter();
    }

    protected Compilation compile(String source) {
        return javac()
                .withOptions("-proc:only")
                .withProcessors(new MbuiViewProcessor())
                .compile(javaSource((source)));
    }

    protected void assertSourceEquals(final Compilation compilation, final String source) {
        String generated = getClass().getPackage().getName() + "." + source;
        assertThat(compilation)
                .generatedSourceFile(generated)
                .hasSourceEquivalentTo(javaSource(source));
    }

    private JavaFileObject javaSource(String classname) {
        return JavaFileObjects.forResource(getClass().getResource(classname + ".java"));
    }
}
