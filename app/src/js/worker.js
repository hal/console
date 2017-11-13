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
self.importScripts("pouchdb.min.js");
self.addEventListener("message", function (e) {
    var db = new PouchDB(e.data.database);
    db.get(e.data.document._id)
        .then(function (doc) {
            // update existing document
            e.data.document._rev = doc._rev;
            db.put(e.data.document)
                .then(function (response) {
                    console.log("Update " + e.data.database + response.id);
                })
                .catch(function (error) {
                    console.log("Unable to put " + e.data.database + e.data.document._id + ": " + failure);
                });
        })
        .catch(function (err) {
            // put new document
            db.put(e.data.document)
                .then(function (response) {
                    console.log("Insert " + e.data.database + response.id);
                })
                .catch(function (error) {
                    console.log("Unable to put " + e.data.database + e.data.document._id + ": " + failure);
                });
        });
}, false);
