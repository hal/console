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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public abstract class MbuiProcessorTest {

    @Before
    public void setUp() {
        MbuiViewProcessor.resetCounter();
    }

    protected Compilation compile(@NonNls String source) {
        return javac()
                .withProcessors(new MbuiViewProcessor())
                .compile(JavaFileObjects.forResource(getClass().getResource(source + ".java")));
    }

    protected void assertSourceEquals(final Compilation compilation, final String source) {
        assertThat(compilation)
                .generatedSourceFile(getClass().getPackage().getName() + "." + source)
                .hasSourceEquivalentTo(JavaFileObjects.forResource(getClass().getResource(source + ".java")));
    }
}
