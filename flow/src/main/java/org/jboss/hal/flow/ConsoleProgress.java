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
package org.jboss.hal.flow;

/** Progress implementation for the dev mode which prints the progress to {@code System.out}. */
@SuppressWarnings("HardCodedStringLiteral")
public final class ConsoleProgress implements Progress {

    private final String id;
    private int max;
    private int current;

    public ConsoleProgress(final String id) {this.id = id;}

    @Override
    public void reset() {
        reset(0);
    }

    @Override
    public void reset(int max, String label) {
        this.current = 0;
        this.max = max;
        System.out.println("progress#" + id + ".reset(" + max + ")");
    }

    @Override
    public void tick(String label) {
        current++;
        if (max == 0) {
            System.out.println("progress#" + id + ".tick(" + current + ")");
        } else {
            System.out.println("progress#" + id + ".tick(" + current + " / " + max + ")");
        }
    }

    @Override
    public void finish() { System.out.println("progress#" + id + ".finish()"); }
}
