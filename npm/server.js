#! /usr/bin/env node
var express = require('express');
var app = express();

app.use('/', express.static('hal'));
app.listen(3000, function () {
    console.log('HAL.next listening on port 3000!');
});