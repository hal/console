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

public class CompoundProgress implements Progress {

    private final Progress[] progresses;

    public CompoundProgress(Progress... progresses) {
        this.progresses = progresses == null ? new Progress[0] : progresses;
    }

    @Override
    public void reset() {
        for (Progress progress : progresses) {
            progress.reset();
        }
    }

    @Override
    public void reset( int max, String label) {
        for (Progress progress : progresses) {
            progress.reset(max, label);
        }
    }

    @Override
    public void tick(String label) {
        for (Progress progress : progresses) {
            progress.tick();
        }
    }

    @Override
    public void finish() {
        for (Progress progress : progresses) {
            progress.finish();
        }
    }
}
