define (require)->
  $ = require 'jqlayout'

  blockui = require 'blockui'
  msgbox = require 'jqmsgbox'

  grid = require 'transmng/trans_grid'
#  detailgrid = require 'transmng/transdetail_grid'
  c18n = require 'i18n!nls/common'
  i18n = require 'i18n!nls/transmng'
  util = require 'dms-util'


  ids =
    languageFilterTableId: 'languageFilterTable'
    languageFilterDialogId: 'languageFilterDialog'
  #  private method
  initPage = ->
  #    public variables and methods
  name: 'layout'
