define ['jqlayout', 'jquery', 'i18n!nls/transmng','jqueryui'], ($, jq, i18n,jqui)->
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
      $('#productBase').append new Option("#{i18n.select.producttip}", -1)
      $('#productBase').append $(json).map ()->new Option this.name, this.id

      #  load product in product base
      $('#productBase').change ()->
        $('#productRelease').empty()
        return false if parseInt($('#productBase').val()) == -1

        $.getJSON "rest/products/#{$('#productBase').val()}", {}, (json)->
          $('#productRelease').append $(json).map ()->new Option this.version, this.id


    $('#productRelease').change ()->
      alert 'To be or not to be, this ia a question.'

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
      checkedAll = $("<input type='checkbox' checked='checked' id='checkedAll'><label for='checkedAll'>Checked all</label>")
      checkedAll.change ()->
        $("input[name='languages']", languageFilterTable).attr('checked', this.checked)
      checkedAll.appendTo($('div.ui-dialog-buttonpane'))
    buttons:
      {
      'OK': ()->
        selectedLanguages = $("input[name='languages']", languageFilterTable).map () -> {id: this.id, checked: this.checked, name: this.value} if this.checked
        $("#taskGridList").updateTaskLanguage (selectedLanguages.map ()->this.name).get(), 'json/taskgrid1.json'
        $(this).dialog "close"
      'Cancel': ()->$(this).dialog "close"
      }

    }
    languageFilterDialog.append languageFilterTable

    $.getJSON 'rest/languages', {}, (json)->
      languages = $(json).map ()->
        $("<td><input type='checkbox' checked value=#{this.name} name='languages' id=#{this.id} /><label for=#{this.id}>#{this.name}</label></td>").css('width', '180px')
      languages.each (index)->
        $("<tr/>").appendTo languageFilterTable if 0 == index % 5
        this.appendTo $("tr:eq(#{Math.floor(index / 5)})", languageFilterTable)

    $('#languageFilter').button().click ()->$("##{languageFilterDialogId}").dialog "open"

  # initialize page
  initPage()
  #    public variables and methods
  name: 'layout'







