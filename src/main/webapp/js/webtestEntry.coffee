"use strict"
require ['js/config.js'],(config)->require ['qunit'], ((QUnit, example)->
#    console.log QUnit
    # start QUnit.
    QUnit.load()
    QUnit.start()
  ), (err)->console.log("load module err: " + err)
