define ['jqlayout', 'jquery', 'i18n!nls/transmng', 'i18n!nls/common', 'transmng/trans_grid', 'require'], ($, jq, i18n, c18n, grid, require)->
#  console.log module
#  private variables
  ids = {
  container:
    {
    page: 'optional-container'
    }
  }
  #  private method
  initPage = ->
    pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}

    ###################################### Initialize elements in north panel ######################################
    # populate option for product base
    $.getJSON 'rest/products/trans/productbases', {}, (json)->
      $('#productBase').append new Option(i18n.select.product.tip, -1)
      $('#productBase').append $(json).map ()->new Option this.name, this.id


      #  load product in product base
      $('#productBase').change ()->
        $('#productRelease').empty()
        return false if parseInt($('#productBase').val()) == -1


        $.getJSON "rest/products/#{$('#productBase').val()}", {}, (json)->
          $('#productRelease').append new Option(i18n.select.release.tip, -1)
          $('#productRelease').append $(json).map ()->new Option this.version, this.id

          $('#productRelease').trigger "change"


    $('#productRelease').change ->
      param = {
      release: {id: $(this).val(), version: $(this).find("option:selected").text()}
      languages: ($(":checkbox[name='languages']", languageFilterTable).map () -> {id: this.id, checked: this.checked, name: this.value} if this.checked).get()
      level: $(":radio[name='viewOption'][checked]").val()
      }
      if !$('#productBase').val() || parseInt($('#productBase').val()) == -1
      #        $.msgBox i18n.select.product.msg, null,title: i18n.select.product.msgtitle, width: 300, height: "auto"
        return false

      if !param.release.id || parseInt(param.release.id) == -1
      #        $.msgBox i18n.select.release.msg, null, title: i18n.select.release.msgtitle, width: 300 , height: "auto"
        return false

      grid.productReleaseChanged param


    ###################################### Elements in summary panel ######################################
    #generate language filter dialog
    languageFilterTableId = 'languageFilterTable'
    languageFilterDialogId = 'languageFilterDialog'

    languageFilterTable = $("<table id='#{languageFilterTableId}' align='center' border='0'></table>")
    languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='#{languageFilterDialogId}'>").dialog {
    autoOpen: false
    position: [23, 126]
    width: 950
    show: { effect: 'slide', direction: "up" }
    create: ()->
      checkedAll = $("<input type='checkbox' checked='checked' id='checkedAll'><label for='checkedAll'>All</label>")
      checkedAll.change ()->
        $(":checkbox[name='languages']", languageFilterTable).attr('checked', this.checked)
      checkedAll.appendTo($('div.ui-dialog-buttonpane'))
    buttons: [
      { text: c18n.ok, click: ()->
        $('#productRelease').trigger "change"
        $(this).dialog "close"
      }
      {text: c18n.cancel, click: ()->$(this).dialog "close"}
    ]

    }
    languageFilterDialog.append languageFilterTable

    $.getJSON 'rest/languages?prop=id,name', {}, (json)->
      languages = $(json).map ()->
        $("<td><input type='checkbox' checked value=\"#{this.name}\" name='languages' id=#{this.id} /><label for=#{this.id}>#{this.name}</label></td>").css('width', '180px')
      languages.each (index)->
        $("<tr/>").appendTo languageFilterTable if 0 == index % 5
        this.appendTo $("tr:eq(#{Math.floor(index / 5)})", languageFilterTable)

    $('#languageFilter').button().click ()->$("##{languageFilterDialogId}").dialog "open"

    $("#applicationView").change -> $('#productRelease').trigger "change"
    $("#dictionaryView").change -> $('#productRelease').trigger "change"


    #   create dialogs
    taskDialog = $("#createTranslationTaskDialog").dialog {autoOpen: false, width: 420, height: 'auto'
    open: ->
      grid = (require 'transmng/trans_grid')
      info = grid.getTotalSelectedRowInfo()
      tableType = grid.getTableType()
      nums = info.selectedNum
      nums = -1 if 'application' == tableType

      console.log "table type=#{tableType}, nums = #{nums}"

      $("#dictSelected").html "<b>#{nums}</b>"
#      TODO: application num of string
      $("#totalLabels").html "<b>#{info.totalLabels}</b>"
    # update target languages from rest

    buttons: [
      {text: c18n.create
      click: ->
        alert "OK"
        $(this).dialog "close"
      }
      {text: c18n.cancel, click: -> $(this).dialog "close"}
    ]
    }

    $("#create").button().click ->
      taskDialog.dialog "open"
  # initialize page
  initPage()


  #    public variables and methods
  name: 'layout'
  getSelectedLanguages: -> $(":checkbox[name='languages']", languageFilterTable).map () -> {id: this.id, checked: this.checked, name: this.value} if this.checked

