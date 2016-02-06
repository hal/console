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

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.resources.Constants;

import java.util.LinkedHashMap;

/**
 * @author Harald Pehl
 */
public class StaticItem {

    public static class Builder {

        static final Constants CONSTANTS = GWT.create(Constants.class);

        private final String title;
        private final LinkedHashMap<String, ItemAction<StaticItem>> actions;
        private boolean folder;
        private SelectCallback<StaticItem> selectCallback;
        private PreviewContent previewContent;

        public Builder(final String title) {
            this.title = title;
            this.actions = new LinkedHashMap<>();
            this.folder = false;
        }

        public Builder token(PlaceManager placeManager, String token) {
            actions.put(CONSTANTS.view(), item -> {
                PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
                placeManager.revealPlace(placeRequest);
            });
            return this;
        }

        public Builder action(String name, ItemAction<StaticItem> action) {
            actions.put(name, action);
            return this;
        }

        public Builder folder() {
            this.folder = true;
            return this;
        }

        public Builder onSelect(SelectCallback<StaticItem> selectCallback) {
            this.selectCallback = selectCallback;
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
    private final LinkedHashMap<String, ItemAction<StaticItem>> actions;
    private final boolean folder;
    private final SelectCallback<StaticItem> selectCallback;
    private final PreviewContent previewContent;

    StaticItem(Builder builder) {
        this.title = builder.title;
        this.actions = builder.actions;
        this.folder = builder.folder;
        this.selectCallback = builder.selectCallback;
        this.previewContent = builder.previewContent;
    }

    public LinkedHashMap<String, ItemAction<StaticItem>> getActions() {
        return actions;
    }

    public boolean isFolder() {
        return folder;
    }

    public PreviewContent getPreviewContent() {
        return previewContent;
    }

    public SelectCallback<StaticItem> getSelectCallback() {
        return selectCallback;
    }

    public String getTitle() {
        return title;
    }
}
