/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
ace.define("ace/theme/logfile", ["require", "exports", "module", "ace/lib/dom"], function (require, exports, module) {

    exports.isDark = false;
    exports.cssClass = "ace-logfile";
    exports.cssText = ".ace-logfile .ace_gutter {\
background: #ebebeb;\
color: #333\
}\
.ace-logfile .ace_print-margin {\
width: 1px;\
background: #e8e8e8\
}\
.ace-logfile {\
background-color: #fff;\
color: #080808\
}\
.ace-logfile .ace_cursor {\
color: #000000\
}\
.ace-logfile .ace_marker-layer .ace_selection {\
background: rgba(39, 95, 255, 0.30)\
}\
.ace-logfile.ace_multiselect .ace_selection.ace_start {\
box-shadow: 0 0 3px 0px #F9F9F9;\
border-radius: 2px\
}\
.ace-logfile .ace_marker-layer .ace_step {\
background: rgb(255, 255, 0)\
}\
.ace-logfile .ace_marker-layer .ace_bracket {\
margin: -1px 0 0 -1px;\
border: 1px solid rgba(75, 75, 126, 0.50)\
}\
.ace-logfile .ace_marker-layer .ace_active-line {\
background: rgba(36, 99, 180, 0.12)\
}\
.ace-logfile .ace_gutter-active-line {\
background-color : #dcdcdc\
}\
.ace-logfile .ace_marker-layer .ace_selected-word {\
border: 1px solid rgba(39, 95, 255, 0.30)\
}\
.ace-logfile .ace_invisible {\
color: rgba(75, 75, 126, 0.50)\
}\
.ace-logfile .ace_keyword,\
.ace-logfile .ace_meta {\
color: #794938\
}\
.ace-logfile .ace_constant,\
.ace-logfile .ace_constant.ace_character,\
.ace-logfile .ace_constant.ace_character.ace_escape,\
.ace-logfile .ace_constant.ace_other {\
color: #811F24\
}\
.ace-logfile .ace_invalid.ace_illegal {\
text-decoration: underline;\
font-style: italic;\
color: #F8F8F8;\
background-color: #B52A1D\
}\
.ace-logfile .ace_invalid.ace_deprecated {\
text-decoration: underline;\
font-style: italic;\
color: #B52A1D\
}\
.ace-logfile .ace_support {\
color: #691C97\
}\
.ace-logfile .ace_support.ace_constant {\
color: #B4371F\
}\
.ace-logfile .ace_fold {\
background-color: #794938;\
border-color: #080808\
}\
.ace-logfile .ace_list,\
.ace-logfile .ace_markup.ace_list,\
.ace-logfile .ace_support.ace_function {\
color: #693A17\
}\
.ace-logfile .ace_storage {\
font-style: italic;\
color: #A71D5D\
}\
.ace-logfile .ace_string {\
color: #0B6125\
}\
.ace-logfile .ace_string.ace_regexp {\
color: #CF5628\
}\
.ace-logfile .ace_comment {\
font-style: italic;\
color: #5A525F\
}\
.ace-logfile .ace_heading,\
.ace-logfile .ace_markup.ace_heading {\
color: #19356D\
}\
.ace-logfile .ace_variable {\
color: #234A97\
}\
.ace-logfile .ace_indent-guide {\
background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAEklEQVQImWNgYGBgYLh/5+x/AAizA4hxNNsZAAAAAElFTkSuQmCC) right repeat-y\
}\
.ace-logfile .ace_timestamp {\
color: #447384;\
}\
.ace-logfile .ace_category {\
color: #71362D;\
}\
.ace-logfile .ace_level.ace_error {\
color: #CC0000;\
}\
.ace-logfile .ace_level.ace_warn {\
color: #FF9911;\
}\
.ace-logfile .ace_level.ace_info {\
color: inherit;\
}\
.ace-logfile .ace_level.ace_debug {\
color: #666666;\
}\
.ace-logfile .ace_exception {\
color: #FFBC11;\
background-color: #c82e2e;\
}";

    var dom = require("../lib/dom");
    dom.importCssString(exports.cssText, exports.cssClass);
});
