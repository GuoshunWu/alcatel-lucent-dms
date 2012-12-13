define (require)->
  $ = require 'jqgrid'
  require 'jqlayout'
  util = require 'util'
  charsetgrid = require 'admin/charsetgrid'
  languagegrid = require 'admin/languagegrid'

  #  global layout
  pageLayout = $("#optional-container").layout {resizable: true, closable: true}
  $(".header-footer").hover (->$(@).addClass "ui-state-hover"), -> $(@).removeClass "ui-state-hover"

  $('#tabs').tabs {

  }

  $('#loading-container').fadeOut 'slow', ()->$(@).remove()

