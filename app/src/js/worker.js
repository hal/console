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
self.importScripts("polyfill.min.js", "pouchdb.min.js");

self.addEventListener("message", function (e) {
    var db = new PouchDB(e.data.database);
    db.get(e.data.document._id)
        .then(function (doc) {
            // update existing document
            e.data.document._rev = doc._rev;
            db.put(e.data.document)
                .then(function (response) {
                    info("Update " + e.data.database + response.id);
                })
                .catch(function (error) {
                    error("Unable to put " + e.data.database + e.data.document._id + ": " + error);
                });
        })
        .catch(function (err) {
            // put new document
            db.put(e.data.document)
                .then(function (response) {
                    info("Insert " + e.data.database + response.id);
                })
                .catch(function (error) {
                    error("Unable to put " + e.data.database + e.data.document._id + ": " + error);
                });
        });
}, false);

self.info = function (message) {
    // use the same log format as HAL
    console.info(timestamp() + " INFO  worker.js                                " + message);
};

self.error = function (message) {
    // use the same log format as HAL
    console.error(timestamp() + " ERROR worker.js                                " + message);
};

self.timestamp = function () {
    var d = new Date();
    return d.getHours().toString().padStart(2, "0") + ":" +
        d.getMinutes().toString().padStart(2, "0") + ":" +
        d.getSeconds().toString().padStart(2, "0") + "." +
        d.getMilliseconds().toString().padStart(3, "0");
}
