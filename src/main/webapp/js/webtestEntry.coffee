"use strict"
require ['js/config.js'],
(config, r)->
  require ['qunit','webtests/example'], (QUnit, example)->
#    console.log QUnit
    # start QUnit.
    QUnit.load()
    QUnit.start()

(err)->console.log("load module err: " + err)
