#"use strict"
define ['jquery'], ($)->
  run=()->
    test  "This is a text example.", ()->ok( 1 == 1, "Passed!" )

  run: run
