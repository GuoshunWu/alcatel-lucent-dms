###
Created by IntelliJ IDEA.
User: Guoshun Wu
###
define ['jqueryui',"jqtree", "i18n!nls/common"], ($, jqtree, c18n)->
  #    prototype enhancement
  String:: format = -> args = arguments; @replace /\{(\d+)\}/g, (m, i) ->args[i]

  String:: endWith = (str) ->
    return false  if !str or str.length > @length
    @substring(@length - str.length) is str

  String:: startWith = (str) ->
    return false  if !str or str.length > @length
    @substr(0, str.length) is str
  String:: capitalize = () ->@toLowerCase().replace(/\b[a-z]/g, (letter)->letter.toUpperCase())

  String:: repeat = (num)->
    i = 0
    buf = ''
    buf += this while i++ < num
    buf
  String:: center = (width, padding = ' ')->
    return this if this.length >= width
    padding = padding[..0]
    len = width - this.length
    remain = if 0 == len % 2 then "" else padding
    pads = padding.repeat(parseInt(len / 2))
    pads + this + pads + remain
  ###
    Dateformat
  ###
  Date:: format = (format)->
    o =
      'M+': @getMonth() + 1, #month
      "d+": @getDate(), #day
      "h+": @getHours(), #hour
      "m+": @getMinutes(), #minute
      "s+": @getSeconds(), #second
      "q+": Math.floor((@getMonth() + 3) / 3), #quarter
      "S": @getMilliseconds()
    #millisecond
    format = format.replace(RegExp.$1, @getFullYear()).substr(4 - RegExp.$1.length) if /(y+)/.test format
    for k,v of o
      format = format.replace(RegExp.$1, if RegExp.$1.length == 1 then v else "00#{v}".substr("#{v}".length)) if new RegExp("(#{k})").test(format)
    format

  ###
  insert elem at pos in array.
  ###
  Array:: insert = (pos, elem) ->
    newarray = @slice(0, pos)
    if($.isArray(elem))
      newarray = newarray.concat elem.slice 0
    else
      newarray.push(elem)
    newarray = newarray.concat(@slice(pos, @length))
    @length = 0
    @push(elem) for elem in newarray
    @

  ###
  remove the element at pos in array, return the removed element.
  ###
  Array:: remove = (start, len) ->
    len = 1 if !len
    newarray = @slice(0, start)
    newarray = newarray.concat(@slice(start + len, @length))
    delElem = if len > 1 then @slice start, start + len else @[start]
    @length = 0
    @push(elem) for elem in newarray
    delElem

  ###
  format json string to pretty.
  ###
  formatJonString = (jsonString) ->
    str = jsonString
    pos = i = 0
    indentStr = "  "
    newLine = "\n"
    retval = ''

    while i < str.length
      char = str.substring(i, i + 1)
      if char is "}" or char is "]"
        retval += newLine
        --pos
        j = 0

        while j < pos
          retval += indentStr
          j++
      retval += char

      if char is "{" or char is "[" or char is ","
        retval += newLine
        ++pos if char is "{" or char is "["
        k = 0
        while k < pos
          retval += indentStr
          k++
      i++
    retval


  randomStr = (length = 10, alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz')->
    rstr = ''
    for ch in alphbet
      rstr += alphbet[Math.floor Math.random() * alphbet.length]
      length--
      break if 0 == length
    rstr

  ###
    generate a progress bar
  ###
  genProgressBar = (autoDispaly = true, autoRemoveWhenCompleted = true)->
    randStr = randomStr(5)
    pbContainer=$("""
                  <div id="pb_container_#{randStr}"  class="progressbar-container">
                  <div class="progressbar-msg">
                  Loading...
                  </div>
                  <div id="progressbar_#{randStr}" class="progressbar">
                  <div class="progressbar-label">0.00%</div>
                  </div>
                  </div>
                  """).appendTo(document.body)
      .draggable(
        create: ()->
          $("#progressbar_#{randStr}", @).progressbar(
            max: 100
            create: (e, ui) ->
              @label = $('div.progressbar-label', @)
              @msg = $('div.progressbar-msg', pbContainer)
            change: (e, ui) ->
              @msg.html $(@).data('msg') if $(@).is(":data(msg)")
              @label.html "#{$(@).progressbar('value').toPrecision(4)}%"
            complete: (e, ui) ->
              pbContainer.remove() if autoRemoveWhenCompleted
          )
      ).hide()
    pbContainer.show().position(my: 'center', at: 'center', of: window) if autoDispaly
    $("#progressbar_#{randStr}", pbContainer)


  long_polling =(url, postData, callback, pb)->
    # call by terminal user
    postData.pqCmd = 'start' if !postData || !postData.pqCmd
#    console?.log "postData="
#    console?.log postData

    # initlize the test parameters
    pollingInterval = if $("#pollingFreq").val() then parseInt($("#pollingFreq").val()) else 1000
    reTryAjax = (retryTimes = Number.MAX_VALUE, retryCounter = 0)->
      $.ajax(url,
        cache: false
        data: postData
        type: 'post'
        dataType: "json"
      ).done((data, textStatus, jqXHR) ->
        #        console?.log data
        if 'error' == data.event.cmd
          $.msgBox event.msg, null, {title: c18n.error}
          return

        if 'done' == data.event.cmd
          callback? data
          return

        if pb
          pb.toggleClass('progressbar-indeterminate', -1 == data.event.percent)
          pb.data 'msg', data.event.msg
          pb.get(0).msg.html data.event.msg
          pb.progressbar 'value', data.event.percent
        else
          callback? data

        setTimeout (->long_polling  url, {pqCmd: 'process', pqId: data.pqId}, callback, pb), pollingInterval
      ).fail((jqXHR, textStatus, errorThrown)->
        if 'timeout' != textStatus
#          console?.log "error: #{textStatus}"
          return

        if(retryTimes > 0)
#          console?.log "Request #{textStatus}, I will retry in #{pollingInterval} milliseconds."
          setTimeout (->reTryAjax(--retryTimes, ++retryCounter)), pollingInterval
        else
#          console?.log "I have retried #{retryCounter} times. There may be a network connection issue, please check network cable."
      )

    reTryAjax()

  getTreeNodeInfo = (node, treeSelecotr = '#appTree')->
    ptree=$.jstree._reference(treeSelecotr)
    return null if !ptree
    selectedNode = if node then node else ptree.get_selected()
    return null if 0 == selectedNode.length
    parent = ptree._get_parent(selectedNode)

    type = selectedNode.attr('type')
    type = 'prod' if type == 'product'

    id: selectedNode.attr('id')
    text: ptree.get_text(selectedNode)
    type: type
    parent: if parent != -1 then getTreeNodeInfo(parent) else parent

  newOption = (text, value, selected)->"<option #{if selected then 'selected ' else ''}value='#{value}'>#{text}</option>"
  urlname2Action = (urlname = '', suffix = 'Action')->urlname.split('/').pop().capitalize().split('-').join('') + suffix

  $.ajaxSetup {timeout: 1000 * 60 * 30, cache: false}

  #  Ajax event for all pages
  sessionCheck = ()->
    $('#sessionTimeoutDialog').dialog(
      width: 320, modal: true
      autoOpen: false
      zIndex: 3999
      buttons: [
        {
        text: c18n.ok, click: (e)->
          $(@).dialog 'close'
          window.location = 'login/forward-to-https'
        }
      ]
    )
    $.ajaxSetup(
      converters :
        "text json": (jsonText)->
          json = jQuery.parseJSON(jsonText)
          $('#sessionTimeoutDialog').dialog 'open' if json.status and 203 == json.status
          json

#      statusCode:
#        203: ()->
#          console?.log "session expired."
#          $('#sessionTimeoutDialog').dialog 'open'
    )
#    $(document).on 'ajaxComplete', (e, xhr, settings)->
#    if 203 == xhr.status
#      $('#sessionTimeoutDialog').dialog 'open'


  checkGridPrivilege = (grid)->
    # console?.log "check the privilege of grid '#{grid.id}'."
    gridParam = $(grid).jqGrid 'getGridParam'
    forbiddenTab =
      cellurl: urlname2Action(gridParam.cellurl) in param.forbiddenPrivileges
      editurl: urlname2Action(gridParam.editurl) in param.forbiddenPrivileges
      cellactionurl: urlname2Action(gridParam.cellactionurl) in param.forbiddenPrivileges

    #    for the celledit
    if forbiddenTab.cellurl
      $.each gridParam.colModel, (idx, obj) ->
        if $.isPlainObject(obj) and obj.name and obj.editable
          obj.editable = false
          obj.classes = obj.classes.replace('editable-column', '')
    #
    #    for the grid  navigatebar, ['view', 'search', 'refresh'] are readonly operation, enabled
    $.each ['add', 'edit', 'del', 'lock'], (index, value)->
      # for jqgrid predefined navigate buttons
      btnSelector = "##{value}_#{grid.id}"
      actButton = $ btnSelector

      if actButton.length > 0 and forbiddenTab.editurl
#        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
        actButton.addClass 'ui-state-disabled'

      # for custom buttons in navigate gird.
      btnSelector = "#custom_#{value}_#{grid.id}"
      actButton = $(btnSelector)

      if actButton.length > 0 and (forbiddenTab.editurl or forbiddenTab.cellactionurl)
#        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
        actButton.addClass 'ui-state-disabled'


    #   for the default cell action of the grid.
    if forbiddenTab.cellactionurl
      $.each gridParam.colModel, (idx, obj) ->
        if $.isPlainObject(obj) and obj.name == 'cellaction'
          obj.formatoptions.delbutton = false
          obj.formatoptions.editbutton = false
    #    for the custom cell action of the grid.
    tmpHandlers = gridParam.cellactionhandlers
    if tmpHandlers
      $.each tmpHandlers, (index, value)->delete tmpHandlers[index] if urlname2Action(value.url) in param.forbiddenPrivileges

  #    $(grid).trigger 'reloadGrid'
  checkAllGridPrivilege = (grids = $('table.ui-jqgrid-btable'), readonly = true)->$.each grids, (idx, grid)->checkGridPrivilege grid
  sessionCheck()

  ###
  Test here.
  ###
  #  a=[1,2,3]
  #  console.log a.insert 1,["a",'b']

  generateLanguageTable: (languages, tableId, colNum)->
    tableId = 'languageFilterTable' if !tableId
    colNum = 5 if !colNum
    rowCount = Math.ceil(languages.length / colNum)

    languageFilterTable = $("<table id='#{tableId}' align='center'width='100%' border='0'><tr valign='top' /></table>")
    outerTableFirstRow = $("tr:eq(0)", languageFilterTable)

    languageCells = $(languages).map ()->$("<td><input type='checkbox' checked value=\"#{@name}\" name='languages' id=#{@id} /><label for=#{@id}>#{@name}</label></td>").css('width', '180px')

    innerColTable = null
    languageCells.each (index)->
      if 0 == index % rowCount
        innerColTable = $("<table border='0'/>")
        outerTableFirstRow.append $("<td></td>").append innerColTable
      innerColTable.append $("<tr/>").append @

    checkedAll = $("<input type='checkbox'id='all_#{tableId}' checked><label for='all_#{tableId}'>All</label>").change ()->
      $(":checkbox[name='languages']", languageFilterTable).attr('checked', @checked)
    #    hr line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'/>").append $("<hr width='100%'>")
    #    check all line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'></td>").append checkedAll

  json2string: (jsonObj)->formatJonString JSON.stringify(jsonObj)
  getDictLanguagesByDictId: (id, callback)->$.getJSON 'rest/languages', {prop: 'id,name', dict: id}, (languages)=>callback languages
  # expires=date; Setting no expiration date on a cookie causes it to expire when the browser closes. If you set an expiration date in the future, the cookie is saved across browser sessions. If you set an expiration date in the past, the cookie is deleted. Use GMT format to specify the date.
  # domain=domainname; Setting the domain of the cookie allows pages on a domain made up of more than one server to share cookie information.
  #  path=path;Setting a path for the cookie allows the current document to share cookie information with other pages within the same domain—that is, if the path is set to /thispathname, all pages in /thispathname and all pages in subfolders of /thispathname can access the same cookie information.
  #  secure; Setting a cookie as secure; means the stored cookie information can be accessed only from a secure environment.
  getUrlParams: (href = window.location.href)->
    lastPos = unless href.endWith '#' then -1 else -2
    suffix = href[href.lastIndexOf('?') + 1..lastPos]
    params = {}
    ([k, v]=param.split('=');params[k] = decodeURIComponent(v) ) for param in suffix.split('&') if suffix
    params
  newOption: newOption
  ###
  convert a json array to a list of options.
  @params
  json: json array
  selectedValue: default selected option value

###
  json2Options: (json, selectedValue = ':last', textFieldName = "version", valueFieldName = "id", sep = '\n')->
    $(json).map(
      (index, elem)->
        #        selected = if !selectedValue then index == json.length - 1 else (String selectedValue) == (String @[valueFieldName])
        # :last indicate that the last option need to be selected
        return false unless jQuery.isArray(json)
        selectedValue = json.slice(-1)[0][valueFieldName] if ':last' == selectedValue
        selectedValue = json.slice(0)[0][valueFieldName] if ':first' == selectedValue
        selected = (String selectedValue) == (String @[valueFieldName])
        newOption @[textFieldName], @[valueFieldName], selected
    ).get().join(sep)

#    if ':last' == selectedValue

  afterInitilized: (context)->
    # center progressbar
    $('div.progressbar').position(my: 'center', at: 'center',of: window)
    #    check all buttons' privilege
    $('[role=button][privilegeName]').each (index, button)->
      if $(button).attr('privilegeName') in param.forbiddenPrivileges
        $(button).button 'disable'
#        console?.log "Button: #{button.id} which privilegeName=#{$(button).attr('privilegeName')} is disabled."
#      else
#        console?.warn "Button: #{button.id} which privilegeName=#{$(button).attr('privilegeName')} is not disabled."

    #   check all the grids' privilege
    checkAllGridPrivilege()



  urlname2Action: urlname2Action
  createLayoutManager: (page = 'appmng.jsp')->createLayoutManager(page)

  getProductTreeInfo: getTreeNodeInfo
  genProgressBar:genProgressBar
  updateProgress: long_polling
  ###
    @param panels: the panel group selector
    @param currentPanel: the current panel selector
    @param onSwitch: the handler on panel switch
  ###
  PanelGroup: class PanelGroup
    constructor: (@panels, @currentPanel, @onSwitch = (oldpnl, newpnl)->)->
    switchTo: (panelId, callback)->

      $("#{@panels}").hide()
#      console?.log "switch to #{@panels}[id='#{panelId}']."
      oldPanel = @currentPanel
      @currentPanel = panelId
      $("#{@panels}[id='#{panelId}']").fadeIn "fast", ()->callback() if $.isFunction(callback)

      @onSwitch oldPanel, @currentPanel if $.isFunction(@onSwitch) and oldPanel != @currentPanel



