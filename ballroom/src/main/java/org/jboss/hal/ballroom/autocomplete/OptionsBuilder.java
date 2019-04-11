/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

public class OptionsBuilder<T> {

    private final SourceFunction<T> source;

    private int minChars;
    private int delay;
    private int offsetLeft;
    private int offsetTop;
    private boolean cache;
    private String menuClass;
    private ItemRenderer<T> renderItem;

    public OptionsBuilder(final SourceFunction<T> source) {
        this.source = source;
        this.minChars = 1;
        this.delay = 150;
        this.offsetLeft = 0;
        this.offsetTop = 1;
        this.cache = false;
        this.menuClass = "";
        this.renderItem = new StringRenderer<>(String::valueOf);
    }

    public OptionsBuilder renderItem(ItemRenderer<T> renderItem) {
        this.renderItem = renderItem;
        return this;
    }

    public Options build() {
        Options options = new Options();
        options.source = source;
        options.minChars = minChars;
        options.delay = delay;
        options.offsetLeft = offsetLeft;
        options.offsetTop = offsetTop;
        options.cache = cache;
        options.menuClass = menuClass;
        options.renderItem = renderItem;
        return options;
    }
}
