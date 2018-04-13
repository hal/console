#! /usr/bin/env node
var express = require('express');
var app = express();

app.use('/', express.static(__dirname + '/hal'));
app.listen(3000, function () {
    console.log('HAL listening on port 3000');
});
