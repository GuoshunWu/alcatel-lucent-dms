define ['jqgrid'], ($)->
  $.jgrid.extend {
  getId: ()->
    "##{this.attr 'id'}"
  addColumns: (newColNames, newColModelEntrys, url,postData)->
    gridParam = this.getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> ""!=val
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> "rn" != val.name

    $.merge gridParam.colModel , newColModelEntrys
    $.merge gridParam.colNames , newColNames

    this.reloadAll(url,postData)

  reloadAll: (url,postData)->
    return if !url
    gridParam = this.getGridParam()
    this.GridUnload this.getId()
    gridParam.url = url
    gridParam.postData=postData if postData
    console.log "recreate grid, gridpostData="
    console.log  gridParam.postData
    newGrid = $(this.getId()).jqGrid gridParam
    this.getGridParam('afterCreate') newGrid

  addTaskLanguage: (language, url, postData)->
    cols = ['T', 'N', 'I']
    colModels = ($(cols).map ()-> {name: "#{language}.#{this}", index: "#{language}.#{this}", width: 20, editable: false, align: 'center'}).get()

    this.getGridParam('groupHeaders').push {startColumnName: "#{language}.T", numberOfColumns: 3, titleText: "<bold>#{language}</bold>"}
    this.addColumns cols, colModels, url, postData

  updateTaskLanguage: (languages, url,postData)->
  #  get all the columns which are not Languages
    return false if $.isEmptyObject languages
    cols = ['T', 'N', 'I']
    gridParam = this.getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> val not in cols
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> not /.+\.[TIN]/g.test val.name

    if not $.isArray languages
      this.addTaskLanguage languages, url, postData
      return

    $(languages).each (index, language)=>
      return false if index == languages.length - 1
      this.addTaskLanguage(language)
    this.addTaskLanguage(languages[languages.length-1], url, postData)

  }

