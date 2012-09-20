define ['jqlayout', 'jquery', 'i18n!nls/transmng', 'i18n!nls/common', 'transmng/trans_grid', 'require', 'jqmsgbox'], ($, jq, i18n, c18n, grid, require)->
#  console.log module
#  private variables
  ids = {
  languageFilterTableId: 'languageFilterTable'
  languageFilterDialogId: 'languageFilterDialog'
  container:
    {
    page: 'optional-container'
    }
  }

  generateLanguageTable = (languages, tableId, colNum)->
    tableId = ids.languageFilterTableId if !tableId
    colNum = 5 if !colNum

    languageFilterTable = $("<table id='#{tableId}' align='center' border='0'></table>")
    languages = $(languages).map ()->$("<td><input type='checkbox' checked value=\"#{this.name}\" name='languages' id=#{this.id} /><label for=#{this.id}>#{this.name}</label></td>").css('width', '180px')
    languages.each (index)->
      $("<tr/>").appendTo languageFilterTable if 0 == index % colNum
      this.appendTo $("tr:eq(#{Math.floor(index / colNum)})", languageFilterTable)

    checkedAll = $("<input type='checkbox'id='all_#{tableId}' checked><label for='all_#{tableId}'>All</label>")
    checkedAll.change ()->$(":checkbox[name='languages']", languageFilterTable).attr('checked', this.checked)
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'/>").append $("<hr width='100%'>")
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'></td>").append checkedAll

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
      languages: ($(":checkbox[name='languages']", $("#" + ids.languageFilterDialogId)).map () -> {id: this.id, name: this.value} if this.checked).get()
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
    languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='#{ids.languageFilterDialogId}'>").dialog {
    autoOpen: false, position: [23, 126], width: 950
    show: { effect: 'slide', direction: "up" }
    create: ->$.getJSON 'rest/languages?prop=id,name', {}, (languages)=>$(this).append(generateLanguageTable languages)
    buttons: [
      { text: c18n.ok, click: ()->
        $('#productRelease').trigger "change"
        $(this).dialog "close"
      }
      {text: c18n.cancel, click: ()->$(this).dialog "close"}
    ]
    }

    $('#languageFilter').button().click ()->$("##{ids.languageFilterDialogId}").dialog "open"

    #    for view level
    $(':radio[name=viewOption]').change -> $('#productRelease').trigger "change"


    #   create dialogs
    taskDialog = $("#createTranslationTaskDialog").dialog {
    autoOpen: false, width: 950, height: 'auto', position: [25, 100], show: { effect: 'slide', direction: "down" }
    open: ->
      info = grid.getTotalSelectedRowInfo()
      #      tableType is app or dict
      tableType = grid.getTableType()
      nums = info.selectedNum
      console.log "table type=#{tableType}, nums = #{nums}"

      $("#dictSelected").html "<b>#{nums}</b>"

      if 'app' == tableType
        nums = -1
#        get dictionary number of selected application from rest

      $("#totalLabels").html "<b>#{info.totalLabels}</b>"
      # update target languages from rest

      langFilterTableId="languageFilter_#{$(this).attr('id')}"
      $("##{langFilterTableId}").remove()
      postData = ($({prop: 'id,name'}).attr tableType, info.rowIds.join(',')).get(0)
      $.getJSON 'rest/languages', postData, (languages)=>$(this).append  generateLanguageTable languages,langFilterTableId

    buttons: [
      {text: c18n.create
      click: ->
        languages=($(":checkbox[name='languages']",$(this)).map -> {id: this.id, name: this.value} if this.checked).get()
#        check languages.length=0

        $(this).dialog "close"
      }
      {text: c18n.cancel, click: -> $(this).dialog "close"}
    ]
    }

    $("#create").button().click ->
      require('jqmsgbox')
      info = grid.getTotalSelectedRowInfo()
      type = $(':radio[name=viewOption][checked]').val()

      if !info.selectedNum
        $.msgBox "Please select #{type} first!", null, title: "Select #{type}", width: 300, height: "auto"
        return

      taskDialog.dialog "open"
  # initialize page
  initPage()


  #    public variables and methods
  name: 'layout'

