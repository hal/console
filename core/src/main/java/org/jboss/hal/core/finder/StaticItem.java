/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.core.finder;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * A customizable finder item useful when you need full control over each and every item.
 *
 * @author Harald Pehl
 */
public class StaticItem {

    public static class Builder {

        private final String title;
        private final List<ItemAction<StaticItem>> actions;
        private PreviewContent previewContent;
        private String nextColumn;

        public Builder(final String title) {
            this.title = title;
            this.actions = new ArrayList<>();
        }

        public Builder action(String title, ItemActionHandler<StaticItem> action) {
            actions.add(new ItemAction<>(title, action));
            return this;
        }

        public Builder tokenAction(String title, PlaceManager placeManager, String token) {
            return placeRequestAction(title, placeManager, new PlaceRequest.Builder().nameToken(token).build());
        }

        public Builder placeRequestAction(String title, PlaceManager placeManager, PlaceRequest placeRequest) {
            return action(title, item -> placeManager.revealPlace(placeRequest));
        }

        public Builder nextColumn(String nextColumn) {
            this.nextColumn = nextColumn;
            return this;
        }

        public Builder onPreview(PreviewContent previewContent) {
            this.previewContent = previewContent;
            return this;
        }

        public StaticItem build() {
            return new StaticItem(this);
        }
    }


    private final String title;
    private final List<ItemAction<StaticItem>> actions;
    private final String nextColumn;
    private final PreviewContent previewContent;

    StaticItem(Builder builder) {
        this.title = builder.title;
        this.actions = builder.actions;
        this.nextColumn = builder.nextColumn;
        this.previewContent = builder.previewContent;
    }

    public List<ItemAction<StaticItem>> getActions() {
        return actions;
    }

    public PreviewContent getPreviewContent() {
        return previewContent;
    }

    public String getTitle() {
        return title;
    }

    public String getNextColumn() {
        return nextColumn;
    }
}
