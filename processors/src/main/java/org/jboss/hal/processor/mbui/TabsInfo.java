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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class TabsInfo {
    
    private static int counter = 0;
    
    // name is only used to declare the attribute name in the generated code
    private String name;
    // this is the list of Tab object
    private List<TabItem> items = new ArrayList<>();
    

    public static class TabItem {

        private String title;
        private String id;
        // only forms are allowed
        private List<String> formChildren = new ArrayList<>();

        TabItem(final String title, final String id) {
            this.title = title;
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public String getId() {
            return id;
        }

        public List<String> getFormChildren() {
            return formChildren;
        }

        void addChildId(String name) {
            formChildren.add(name);
        }

        @Override
        public String toString() {
            StringBuilder tostr = new StringBuilder();
            tostr.append("TabItem { id = ").append(id).append(", title=").append(title).append(", formChildren=");
            for (int i = 0; i < formChildren.size(); i++) {
                String child = formChildren.get(i);
                tostr.append(child);
                if (i + 1 < formChildren.size()) { 
                    tostr.append(", "); 
                }
            }
            tostr.append(" }");
            return  tostr.toString();
        }
    }

    TabsInfo() {
        this.name = "tabs" + counter; //NON-NLS
        counter++;
    }

    public String getName() {
        return name;
    }

    public List<TabItem> getItems() {
        return items;
    }

    void addItem(TabItem item) {
        items.add(item);
    }

    @Override
    public String toString() {
        StringBuilder tostr = new StringBuilder();
        tostr.append("TabsInfo { name = ").append(name).append(", items=");
        for (TabItem item: items) {
            tostr.append(item);
        }
        tostr.append(" }");
        return tostr.toString();
    }
}
