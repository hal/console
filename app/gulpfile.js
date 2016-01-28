/*!
 * gulp
 * $ npm install gulp-less gulp-autoprefixer gulp-minify-css gulp-rename gulp-copy del --save-dev
 */

var gulp = require('gulp'),
    less = require('gulp-less'),
    autoprefixer = require('gulp-autoprefixer'),
    cssnano = require('gulp-cssnano'),
    rename = require('gulp-rename'),
    copy = require('gulp-copy'),
    del = require('del');

gulp.task('less', function () {
    return gulp.src('src/main/less/hal.less')
        .pipe(less())
        .pipe(autoprefixer({browsers: ['last 2 versions'], cascade: false}))
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

    // DataTables
    gulp.src('bower_components/datatables.net/js/jquery.dataTables.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-buttons/js/dataTables.buttons.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));
    gulp.src('bower_components/datatables.net-select/js/dataTables.select.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

    // jQuery
    gulp.src('bower_components/jquery/dist/jquery.*')
        .pipe(copy('src/main/resources/org/jboss/hal/public/js', {prefix: 3}));

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
});

// Clean
gulp.task('clean', function (cb) {
    del(['src/main/resources/org/jboss/hal/public/css/**',
        'src/main/resources/org/jboss/hal/public/fonts/**',
        'src/main/resources/org/jboss/hal/public/img/**',
        'src/main/resources/org/jboss/hal/public/js/**'], cb)
});

// Default task
gulp.task('default', ['copy', 'less']);

// Watch
gulp.task('watch', function () {
    // Watch .less files
    gulp.watch('src/main/less/**/*.less', ['less']);
});
