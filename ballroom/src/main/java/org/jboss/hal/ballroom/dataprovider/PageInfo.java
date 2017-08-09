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
package org.jboss.hal.ballroom.dataprovider;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PageInfo {

    private final int page;
    private final int pageSize;
    private final int visible;
    private final int total;

    PageInfo(int page, int pageSize, int visible, int total) {
        this.page = page;
        this.pageSize = pageSize;
        this.visible = visible;
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof PageInfo)) { return false; }

        PageInfo pageInfo = (PageInfo) o;

        if (page != pageInfo.page) { return false; }
        if (pageSize != pageInfo.pageSize) { return false; }
        if (visible != pageInfo.visible) { return false; }
        return total == pageInfo.total;
    }

    @Override
    public int hashCode() {
        int result = page;
        result = 31 * result + pageSize;
        result = 31 * result + visible;
        result = 31 * result + total;
        return result;
    }

    @Override
    public String toString() {
        return "PageInfo(" + "page=" + page + ", pageSize=" + pageSize + ", visible=" + visible + ", total=" + total + ')';
    }

    public int getFrom() {
        return getPage() * getPageSize() + 1;
    }

    public int getTo() {
        return min(total, getFrom() + getPageSize() - 1);
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        int pages = total / pageSize;
        if (total % pageSize != 0) {
            pages++;
        }
        return max(1, pages);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getVisible() {
        return visible;
    }

    public int getTotal() {
        return total;
    }
}
