/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */self.importScripts("pouchdb.min.js"),self.addEventListener("message",function(t){let a=new PouchDB(t.data.database);a.get(t.data.document._id).then(function(e){// update existing document
t.data.document._rev=e._rev,a.put(t.data.document).then(function(a){info("Update "+t.data.database+a.id)}).catch(function(a){a("Unable to put "+t.data.database+t.data.document._id+": "+a)})}).catch(function(){// put new document
a.put(t.data.document).then(function(a){info("Insert "+t.data.database+a.id)}).catch(function(a){a("Unable to put "+t.data.database+t.data.document._id+": "+a)})})},!1),self.info=function(t){// use the same log format as HAL
console.info(timestamp()+" INFO  worker.js                                "+t)},self.error=function(t){// use the same log format as HAL
console.error(timestamp()+" ERROR worker.js                                "+t)},self.timestamp=function(){let t=new Date;return t.getHours().toString().padStart(2,"0")+":"+t.getMinutes().toString().padStart(2,"0")+":"+t.getSeconds().toString().padStart(2,"0")+"."+t.getMilliseconds().toString().padStart(3,"0")};//# sourceMappingURL=worker.4b4ddf8a.js.map

//# sourceMappingURL=worker.4b4ddf8a.js.map
