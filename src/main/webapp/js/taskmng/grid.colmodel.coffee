$.jgrid.extend {
getId: ()->
  "##{this.attr 'id'}"
addColumns: (newColNames, newColModelEntrys, url)->
  gridParam = this.getGridParam()
  gridParam.colModel = gridParam.colModel.slice(1,gridParam.colModel.length).concat newColModelEntrys
  gridParam.colNames = gridParam.colNames.slice(1,gridParam.colNames.length).concat newColNames
  log "colModel len: #{gridParam.colModel.length}, colName len:#{gridParam.colNames.length}."

  this.reloadAll(url)

reloadAll: (url)->
  return if !url
  gridParam = this.getGridParam()
  this.GridUnload this.getId()
  gridParam.url=url
  log gridParam.colNames
  newGrid = $(this.getId()).jqGrid gridParam
  this.getGridParam('afterCreate') newGrid

addTaskLanguage: (language, url)->
  cols = ['T', 'N', 'I']
  colModels = ($(cols).map ()-> {name: "#{language}.#{this}", index: "#{language}.#{this}", width: 20, editable: false, align: 'center'}).get()

  this.getGridParam('groupHeaders').push {startColumnName: "#{language}.T", numberOfColumns: 3, titleText: "<bold>#{language}</bold>"}
  this.addColumns cols, colModels, url

removeTaskLanguage: (language)->
  cols = ['T', 'N', 'I']
  colModels = ($(cols).map ()-> {name: "#{language}.#{this}", index: "#{language}.#{this}", width: 20, editable: false, align: 'center'}).get()
}

