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
package org.jboss.hal.client.rhcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.google.common.collect.Iterators;
import elemental2.dom.HTMLElement;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.js.JsonObject;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.client.rhcp.RhcpColumns.Decade.DECADES;
import static org.jboss.hal.resources.CSS.listGroupItem;
import static org.jboss.hal.resources.CSS.preview;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "SpellCheckingInspection"})
public class RhcpColumns {

    private static final String TITLE = "title";
    private static final String YEAR = "year";


    @AsyncColumn("rhcp-color")
    public static class Color extends StaticItemColumn {

        @Inject
        public Color(Finder finder) {
            super(finder, "rhcp-color", "Color",
                    asList(new StaticItem.Builder("Red").nextColumn("rhcp-temperature").build(),
                            new StaticItem.Builder("Orange").build(),
                            new StaticItem.Builder("Yellow").build(),
                            new StaticItem.Builder("Green").build(),
                            new StaticItem.Builder("Cyan").build(),
                            new StaticItem.Builder("Blue").build(),
                            new StaticItem.Builder("Violet").build())
            );
        }
    }


    @AsyncColumn("rhcp-temperature")
    public static class Temperature extends StaticItemColumn {

        @Inject
        public Temperature(Finder finder) {
            super(finder, "rhcp-temperature", "Temperature",
                    asList(new StaticItem.Builder("Absolute Zero").build(),
                            new StaticItem.Builder("Freezing").build(),
                            new StaticItem.Builder("Cold").build(),
                            new StaticItem.Builder("Lukewarm").build(),
                            new StaticItem.Builder("Warm").build(),
                            new StaticItem.Builder("Hot").nextColumn("rhcp-vegetable").build(),
                            new StaticItem.Builder("Sun's Surface").build()));
        }
    }


    @AsyncColumn("rhcp-vegetable")
    public static class Vegetables extends StaticItemColumn {

        @Inject
        public Vegetables(Finder finder) {
            super(finder, "rhcp-vegetable", "Vegetable",
                    asList(new StaticItem.Builder("Beans").build(),
                            new StaticItem.Builder("Carrot").build(),
                            new StaticItem.Builder("Chili").nextColumn("rhcp-spice").build(),
                            new StaticItem.Builder("Cucumber").build(),
                            new StaticItem.Builder("Garlic").build(),
                            new StaticItem.Builder("Onion").build(),
                            new StaticItem.Builder("Tomato").build()));
        }
    }


    @AsyncColumn("rhcp-spice")
    public static class Spice extends StaticItemColumn {

        @Inject
        public Spice(Finder finder) {
            super(finder, "rhcp-spice", "Spice",
                    asList(new StaticItem.Builder("Cinnamon").build(),
                            new StaticItem.Builder("Peppers").nextColumn("rhcp-decade").build(),
                            new StaticItem.Builder("Salt").build(),
                            new StaticItem.Builder("Sugar").build()));
        }
    }


    @AsyncColumn("rhcp-decade")
    public static class Decade extends StaticItemColumn {

        static final Map<String, Predicate<JsonObject>> DECADES = new LinkedHashMap<>();

        static {
            DECADES.put("1980 - 1989", input -> input != null && (int) input.getNumber(YEAR) >= 1980 && (int) input
                    .getNumber(YEAR) < 1990);
            DECADES.put("1990 - 1999", input -> input != null && (int) input.getNumber(YEAR) >= 1990 && (int) input
                    .getNumber(YEAR) < 2000);
            DECADES.put("2000 - 2010", input -> input != null && (int) input.getNumber(YEAR) >= 2000 && (int) input
                    .getNumber(YEAR) < 2010);
            DECADES.put("2010 - 2020", input -> input != null && (int) input.getNumber(YEAR) >= 2010 && (int) input
                    .getNumber(YEAR) < 2020);
        }

        @Inject
        public Decade(Finder finder) {
            super(finder, "rhcp-decade", "Decade", DECADES.keySet().stream()
                    .map(decade -> new StaticItem.Builder(decade).nextColumn("rhcp-album").build())
                    .collect(toList()));
        }
    }


    @AsyncColumn("rhcp-album")
    public static class Album extends FinderColumn<JsonObject> {

        @Inject
        public Album(Finder finder) {
            super(new Builder<JsonObject>(finder, "rhcp-album", "Album")
                    .itemsProvider((context, callback) -> {
                        String decade = Iterators.getLast(context.getPath().iterator()).getItemTitle();
                        List<JsonObject> albums = new ArrayList<>();
                        for (String key : RhcpResources.DISCOGRAPHY.keys()) {
                            JsonObject album = RhcpResources.DISCOGRAPHY.getObject(key);
                            if (DECADES.get(decade).test(album)) {
                                albums.add(album);
                            }
                        }
                        callback.onSuccess(albums);
                    })
                    .itemRenderer(item -> new ItemDisplay<JsonObject>() {
                        @Override
                        public HTMLElement element() {
                            return ItemDisplay
                                    .withSubtitle(item.getString(TITLE), String.valueOf(item.getNumber(YEAR)));
                        }

                        @Override
                        public String getTitle() {
                            return item.getString(TITLE);
                        }

                        @Override
                        public String nextColumn() {
                            return "rhcp-track";
                        }
                    })
                    .onPreview(item -> new PreviewContent<>(item.getString(TITLE),
                            "Released " + item.getString("released"),
                            collect()
                                    .add(img(item.getString("cover")).css(preview))
                                    .add(p()
                                            .add(span().textContent("More infos: "))
                                            .add(a(item.getString("url"))
                                                    .attr("target", "_blank")
                                                    .textContent(item.getString("url")))).elements())));
        }
    }


    @AsyncColumn("rhcp-track")
    public static class Track extends FinderColumn<JsonObject> {

        @Inject
        public Track(Finder finder, ItemActionFactory itemActionFactory) {
            super(new Builder<JsonObject>(finder, "rhcp-track", "Track")
                    .itemsProvider((context, callback) -> {
                        List<JsonObject> tracks = new ArrayList<>();
                        StreamSupport.stream(context.getPath().spliterator(), false)
                                .filter(segment -> "rhcp-album".equals(segment.getColumnId()))
                                .findAny()
                                .map(segment -> RhcpResources.DISCOGRAPHY.getObject(segment.getItemTitle()))
                                .ifPresent(album -> {
                                    for (int i = 0; i < album.getArray("tracks").length(); i++) {
                                        tracks.add(album.getArray("tracks").getObject(i));
                                    }
                                });
                        callback.onSuccess(tracks);
                    })
                    .itemRenderer(item -> new ItemDisplay<JsonObject>() {
                        @Override
                        public HTMLElement element() {
                            return ItemDisplay.withSubtitle(
                                    item.getNumber("track") + ". " + item.getString(TITLE),
                                    item.getString("length"));
                        }

                        @Override
                        public String getTitle() {
                            return item.getString(TITLE);
                        }

                        @Override
                        public List<ItemAction<JsonObject>> actions() {
                            if ("Under the Bridge".equals(item.getString(TITLE))) {
                                return singletonList(itemActionFactory.view("utb"));
                            }
                            return ItemDisplay.super.actions();
                        }
                    })
                    .onPreview(item -> {
                        String album = Iterators.getLast(finder.getContext().getPath().iterator()).getItemTitle();
                        String length = item.getString("length");
                        HTMLElement ul = ul()
                                .add(li().css(listGroupItem).textContent("Album: " + album))
                                .add(li().css(listGroupItem).textContent("Length: " + length)).element();
                        if (item.hasKey("writer")) {
                            List<String> writers = new ArrayList<>();
                            for (int i = 0; i < item.getArray("writer").length(); i++) {
                                writers.add(item.getArray("writer").getString(i));
                            }
                            ul.appendChild(li().css(listGroupItem)
                                    .textContent("Writer: " + String.join(", ", writers)).element());
                        }
                        return new PreviewContent<>(item.getString(TITLE), ul);
                    }));
        }
    }
}