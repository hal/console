/*!
 * gulp
 * $ npm install gulp-LESS gulp-autoprefixer gulp-minify-css gulp-rename gulp-copy del --save-dev
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
        .pipe(rename({suffix: '.min'}))
        .pipe(minifycss())
        .pipe(gulp.dest('src/main/resources/org/jboss/hal/public/css'));
});

gulp.task('copy', function () {
    // Patternfly
    gulp.src('bower_components/patternfly/dist/css/*.min.css')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/img/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
    gulp.src('bower_components/patternfly/dist/js/*.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));

    // JQuery
    gulp.src('bower_components/patternfly/components/jquery/dist/jquery.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));

    // Bootstrap
    gulp.src('bower_components/patternfly/components/bootstrap/dist/js/bootstrap.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
    gulp.src('bower_components/patternfly/components/bootstrap-select/dist/js/bootstrap-select.min.js')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));

    // Fonts
    gulp.src('bower_components/patternfly/components/bootstrap/dist/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
    gulp.src('bower_components/patternfly/components/font-awesome/fonts/*')
        .pipe(copy('src/main/resources/org/jboss/hal/public', {prefix: 1}));
});

// Clean
gulp.task('clean', function (cb) {
    del(['src/main/resources/org/jboss/hal/public/patternfly/**',
        'src/main/resources/org/jboss/hal/public/css/**'], cb)
});

// Default task
gulp.task('default', ['less', 'copy']);

// Watch
gulp.task('watch', function () {
    // Watch .less files
    gulp.watch('src/main/less/**/*.less', ['less']);
});
