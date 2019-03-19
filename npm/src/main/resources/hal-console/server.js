#! /usr/bin/env node
var express = require('express');
var app = express();

app.use('/', express.static(__dirname + '/hal'));

var port = process.env.PORT || 3000;

app.listen(3000, function () {
    console.log('HAL listening on port ' + port);
});
