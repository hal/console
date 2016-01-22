/*!
 * gulp
 * $ npm install gulp-less gulp-autoprefixer gulp-minify-css gulp-rename gulp-copy del --save-dev
 */

var gulp = require('gulp'),
    less = require('gulp-less'),
    autoprefixer = require('gulp-autoprefixer'),
    minifycss = require('gulp-minify-css'),
    rename = require('gulp-rename'),
    copy = require('gulp-copy'),
    del = require('del');

gulp.task('less', function () {
    return gulp.src('src/main/less/hal.less')
        .pipe(less())
        .pipe(autoprefixer({browsers: ['last 2 versions'], cascade: false}))
        .pipe(minifycss())
        .pipe(rename({suffix: '.min'}))
        .pipe(gulp.dest('src/main/resources/org/jboss/hal/public/css'));
});

gulp.task('copy', function () {
    // Fonts
    gulp.src('bower_components/patternfly/dist/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/fonts', {prefix: 4}));
    gulp.src('bower_components/patternfly/components/font-awesome/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/fonts', {prefix: 5}));
    gulp.src('bower_components/patternfly/components/bootstrap/dist/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/fonts', {prefix: 6}));

    // Images
    gulp.src('bower_components/patternfly/dist/img/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/img', {prefix: 4}));

    // JavaScript
    gulp.src('bower_components/datatables.net/js/jquery.dataTables.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-buttons/js/dataTables.buttons.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-select/js/dataTables.select.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    gulp.src('bower_components/jquery/dist/jquery.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    gulp.src('bower_components/jquery-ui/ui/core.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/jquery-ui/ui/widget.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/jquery-ui/ui/position.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/jquery-ui/ui/menu.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/jquery-ui/ui/autocomplete.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    gulp.src('bower_components/jquery-ui/ui/minified/core.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/jquery-ui/ui/minified/widget.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/jquery-ui/ui/minified/position.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/jquery-ui/ui/minified/menu.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));
    gulp.src('bower_components/jquery-ui/ui/minified/autocomplete.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));

    gulp.src('bower_components/jquery-tag-editor/jquery.caret.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/jquery-tag-editor/jquery.tag-editor.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/jquery-tag-editor/jquery.tag-editor.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 2}));
    gulp.src('bower_components/jquery-tag-editor/jquery.tag-editor.css')
        .pipe(copy('src/main/resources/org/jboss/hal/public/css', {prefix: 2}));
    gulp.src('bower_components/jquery-tag-editor/delete.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/css', {prefix: 2}));

    gulp.src('bower_components/patternfly/components/bootstrap/dist/js/bootstrap.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 6}));
    gulp.src('bower_components/patternfly/components/bootstrap-select/dist/js/bootstrap-select.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 6}));
    gulp.src('bower_components/patternfly/components/bootstrap-switch/dist/js/bootstrap-switch.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 6}));

    gulp.src('bower_components/patternfly/dist/js/patternfly.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 4}));

    gulp.src('bower_components/typeahead.js/dist/typeahead.bundle.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
});

// Clean
gulp.task('clean', function (cb) {
    del(['src/main/resources/org/jboss/hal/public/css/**',
        'src/main/resources/org/jboss/hal/public/fonts/**',
        'src/main/resources/org/jboss/hal/public/img/**',
        'src/main/resources/org/jboss/hal/public/js/**'], cb)
});

// Default task
gulp.task('default', ['less', 'copy']);

// Watch
gulp.task('watch', function () {
    // Watch .less files
    gulp.watch('src/main/less/**/*.less', ['less']);
});
