###
  This is the entry of the login page js
###

require ['js/config.js'],
  (config, r)->require ['login/main'],
  (err)->console.log("load module err: " + err)
