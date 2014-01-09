require ['js/config.js'],
(config, r)->
  require ['qunit','webtests/main'], (qunit, main)->
    qunit =  QUnit unless qunit

    main.run()
    # start QUnit.
    qunit.load()
    qunit.start()

(err)->console.log("load module err: " + err)
