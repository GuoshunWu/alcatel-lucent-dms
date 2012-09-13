pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}

###################################### Elements in north panel ######################################
# populate option for product base
$.getJSON 'rest/products/trans/productbases', {}, (json)->
  $('#productBase').append new Option('Please select product', -1)
  $('#productBase').append $(json).map ()->new Option this.name, this.id


$('#productBase').change ()->
#  load product in product base
  $('#productRelease').empty()
  return false if parseInt($('#productBase').val()) == -1
  log "populate version."
  $.getJSON "rest/products/#{$('#productBase').val()}", {}, (json)->
    $('#productRelease').append $(json).map ()->new Option this.version, this.id


$('#productRelease').change ()->
  alert 'To be or not to be, this ia a question.'


###################################### Elements in summary panel ######################################
#generate language filter dialog
languageFilterTableId = 'languageFilterTable'
languageFilterDialogId = 'languageFilterDialog'

languageFilterTable = $("<table id='#{languageFilterTableId}' align='center' border='0'></table>")
languageFilterDialog = $("<div title='Choose the languages you want to show' id='#{languageFilterDialogId}'>").dialog {
autoOpen: false
position: [18, 120]
width: 950
show: { effect: 'slide', direction: "up" }
buttons: [
  {
  text: 'OK'
  click: ()->
    selectedLanguages = $("input[name='languages']", languageFilterTable).map () -> {id: this.id, checked: this.checked, name: this.value} if this.checked
    $("#taskGridList").updateTaskLanguage (selectedLanguages.map ()->this.name).get(), 'json/taskgrid1.json'
    $(this).dialog "close"
  }
  {
  text: 'Cancel'
  click: ()->$(this).dialog "close"
  }
  {
    text: 'Checked all'
    click: ()->
  }
]
}
languageFilterDialog.append languageFilterTable

$.getJSON 'rest/languages', {}, (json)->
  languages = $(json).map ()->$("<td><input type='checkbox' checked='checked' value='#{this.name}' name='languages' id='#{this.id}' /><label for='#{this.name}'>#{this.name}</label></td>").css('width', '180px')
  languages.each (index)->
    $("<tr/>").appendTo languageFilterTable if 0 == index % 5
    this.appendTo $("tr:eq(#{Math.floor(index / 5)})", languageFilterTable)


$('#languageFilter').button().click ()->$("##{languageFilterDialogId}").dialog "open"






