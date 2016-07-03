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

/*!
 * gulp
 * $ npm install gulp-less gulp-autoprefixer gulp-minify-css gulp-rename gulp-copy del --save-dev
 */

var gulp = require('gulp'),
    less = require('gulp-less'),
    cssnano = require('gulp-cssnano'),
    rename = require('gulp-rename'),
    copy = require('gulp-copy'),
    del = require('del');

gulp.task('less-dev', function () {
    return gulp.src('src/main/less/hal.less')
        .pipe(less())
        .pipe(gulp.dest('src/main/resources/org/jboss/hal/public/css'));
});

gulp.task('less-prod', function () {
    return gulp.src('src/main/less/hal.less')
        .pipe(less())
        .pipe(cssnano())
        .pipe(rename({suffix: '.min'}))
        .pipe(gulp.dest('src/main/resources/org/jboss/hal/public/css'));
});

gulp.task('copy', function () {
    // PatternFly fonts & images
    gulp.src('bower_components/patternfly/dist/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/fonts', {prefix: 4}));
    gulp.src('bower_components/patternfly/dist/img/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/img', {prefix: 4}));

    // jQuery
    gulp.src('bower_components/jquery/dist/jquery.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // Ace Editor
    gulp.src('bower_components/ace-builds/src-min-noconflict/ace.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // DataTables
    gulp.src('bower_components/datatables.net/js/jquery.dataTables.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-buttons/js/dataTables.buttons.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-select/js/dataTables.select.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // jsTree
    gulp.src('bower_components/jstree/dist/jstree.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/jstree/dist/themes/default/*.png')
        .pipe(copy('src/main/resources/org/jboss/hal/public/img', {prefix: 5}));
    gulp.src('bower_components/jstree/dist/themes/default/*.gif')
        .pipe(copy('src/main/resources/org/jboss/hal/public/img', {prefix: 5}));

    // Typeahead
    gulp.src('bower_components/typeahead.js/dist/typeahead.bundle.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // TagManager
    gulp.src('bower_components/tagmanager/tagmanager.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // Bootstrap & PatternFly
    gulp.src('bower_components/bootstrap/dist/js/bootstrap.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/bootstrap-select/dist/js/bootstrap-select.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/bootstrap-switch/dist/js/bootstrap-switch.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/patternfly/dist/js/patternfly.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));

    // C3 / D3
    gulp.src('bower_components/c3/c3.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/c3/c3.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/d3/d3.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/d3/d3.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));

    // ZeroClipboard
    gulp.src('bower_components/zeroclipboard/dist/ZeroClipboard.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/zeroclipboard/dist/ZeroClipboard.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/zeroclipboard/dist/ZeroClipboard.swf')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
});

// Clean
gulp.task('clean', function (cb) {
    del(['src/main/resources/org/jboss/hal/public/css/**',
        'src/main/resources/org/jboss/hal/public/fonts/**',
        'src/main/resources/org/jboss/hal/public/img/**',
        'src/main/resources/org/jboss/hal/public/js/**'], cb)
});

// Development task
gulp.task('dev', ['clean', 'copy', 'less-dev']);

// Production task
gulp.task('prod', ['clean', 'copy', 'less-prod']);

// Watch
gulp.task('watch', function () {
    // Watch .less files
    gulp.watch('src/main/less/**/*.less', ['less-dev']);
});
