define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'
  util = require 'util'
  charsetgrid = require 'admin/charsetgrid'
  languagegrid = require 'admin/languagegrid'

  $('#tabs').tabs {

  }

  $('#loading-container').fadeOut 'slow', ()->$(@).remove()
  util.afterInitilized(this)

