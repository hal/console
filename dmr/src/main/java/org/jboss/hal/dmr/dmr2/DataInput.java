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
package org.jboss.hal.dmr.dmr2;

import elemental2.core.Uint8Array;

class DataInput {

    private final Uint8Array input;

    DataInput(Uint8Array input) {
        this.input = input;
    }


    // ------------------------------------------------------ read a-z

    boolean readBoolean() {
        return false;
    }

    byte readByte() {
        return 0;
    }

    double readDouble() {
        return 0;
    }

    void readFully(byte[] b) {
    }

    int readInt() {
        return 0;
    }

    long readLong() {
        return 0;
    }

    int readUnsignedByte() {
        return 0;
    }

    String readUTF() {
        return null;
    }
}
