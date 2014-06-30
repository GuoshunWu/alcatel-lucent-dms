###
  This script is used in TestImportDictionary->testDeliverDictionaries
  for client dictionary language count
  must be called by executeAsyncScript

  @param arguments[0] the dictionary name to count
  @param arguments[1] timeOut to wait for client get result
###

callback = arguments[arguments.length - 1]
if(arguments.length < 3)
  callback null
  return
dictName = arguments[0]
labelKey = arguments[1]
timeOut = arguments[2]

# call the external util function
stringId = getDictActionIds(dictName).strId
unless stringId
  callback null
  return
$('#' + stringId, '#dictionaryGridList').click()

setTimeout(()->
  rows = (row for row in $('#stringSettingsGrid').getRowData() when labelKey is row.key)
  callback(rows[0])
  $('#stringSettingsDialog').dialog 'close'
, timeOut)
