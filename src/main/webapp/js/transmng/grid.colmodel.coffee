define ['jqgrid'], ($)->
  $.jgrid.extend {
  getId: ()->"##{@attr 'id'}"

  addColumns: (newColNames, newColModelEntrys)->
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> "" != val
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> "rn" != val.name

    $.merge gridParam.colModel, newColModelEntrys
    $.merge gridParam.colNames, newColNames


  reloadAll: (url = this.getGridParam('url'), postData = this.getGridParam('postData'))->
    gridParam = @getGridParam()
    $(gridParam.colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable

    @GridUnload @getId()
    gridParam.url = url
    gridParam.postData = postData if postData

#    console.log "recreate grid, grid rowNum =", gridParam.rowNum
    delete gridParam.selarrrow
    #    delete gridParam.selrow
    gridParam.rowNum = parseInt gridParam.rowNum
    newGrid = $(@getId()).jqGrid gridParam

    #    save search tool bar status before recreate the grid
    newGrid.getGridParam('afterCreate') newGrid

    newGrid


  addTaskLanguage: (language)->
    cols = ['T', 'N', 'I']
    level = $("input:radio[name='viewOption'][checked]").val()
    colModels = $(cols).map(
      (index, elem)->
        model = {
        name: "#{language.name}.#{@}",
        sortable: true, firstsortorder: 'desc', index: "s(#{language.id})[#{index}]",
        width: 40, align: 'right',
        search: false, editable: false
        }
        if elem == 'T'
          model.classes = 'language-group-border'
        if level != 'app'
          model.formatter = 'showlink'
          model.formatoptions = {baseLinkUrl: "javascript:void(0);", addParam: encodeURI("&languageId=#{language.id}&languageName=#{model.name}")}
        model
    ).get()
    @getGridParam('groupHeaders').push {startColumnName: "#{language.name}.T", numberOfColumns: cols.length, titleText: "<bold>#{language.name}</bold>"}
    @addColumns cols, colModels

  updateTaskLanguage: (languages)->
  #  get all the columns which are not Languages
    return if $.isEmptyObject languages
    return if $.isArray(languages) and 0 == languages.length

    cols = ['T', 'N', 'I']
    gridParam = @getGridParam()
    gridParam.colNames = $.grep gridParam.colNames, (val, key)-> !(val  in cols)
    gridParam.colModel = $.grep gridParam.colModel, (val, key)-> !(/.+\.[TIN]/g.test val.name)

    if $.isArray languages
      $(languages).each (index, language)=> @addTaskLanguage(language)
      return

    @addTaskLanguage languages
  }


