#"use strict"
define ['jquery'], ($)->
  run=()->
    test  "hello test", ()->
      ok( 1 == "1", "Passed!" )

  run: run
