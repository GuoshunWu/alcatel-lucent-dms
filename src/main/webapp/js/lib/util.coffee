###
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
To change this template use File | Settings | File Templates.
###
define ["jquery", "jqueryui", "i18n!nls/common"], ($, ui, c18n) ->

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

  setCookie = (name, value, expires, domain, path, secure)->
    c = "#{name}=#{escape(value)}"
    start = 2
    for arg in ['expires', 'domain', 'path', 'secure']
      c += ";#{arg}=#{arguments[start++]}" if arguments[start]
    document.cookie = c

  newOption = (text, value, selected)->"<option #{if selected then 'selected ' else ''}value='#{value}'>#{text}</option>"

  $.ajaxSetup {timeout: 1000 * 60 * 30}
  $.ajaxPrefilter (options, originalOptions, jqXHR)->
  #  for page navigator
  pageNavi = ()->
    $('#naviForm').bind 'submit', (e)->
      $("#curProductBaseId").val $("#productBase").val()
      $("#curProductId").val $("#productRelease").val()

      #    param is global var in /common/env.jsp
      if (param.naviTo == 'appmng.jsp')
        $("#curProductId").val(if $("#selVersion").val() then $("#selVersion").val() else -1)
        appTree = $.jstree._reference("#appTree")
        node = appTree.get_selected()

        productBaseId = -1
        #  -1 indicate that no node is selected
        #      a node is selected
        if node.length > 0
          type = node.attr('type')
          if type == 'product'
            productBaseId = node.attr('id')
          else if type == 'app'
            productBaseId = appTree._get_parent(node).attr('id')
        $("#curProductBaseId").val productBaseId

    $('#pageNavigator').change (e)->$('#naviForm').submit()

  #  Ajax event for all pages
  sessionCheck = ()->
    $('#sessionTimeoutDialog').dialog {
    width: 320, modal: true
    autoOpen: false
    buttons: [
      {
      text: c18n.ok, click: (e)->
        $(@).dialog 'close'
        window.location = 'login/forward-to-https'
      }
    ]
    }

    $(document).on 'ajaxSuccess', (e, xhr, settings)->
    #      console?.log "xhr.status=#{xhr.status}"
      if 203 == xhr.status
        $('#sessionTimeoutDialog').dialog 'open'
  #        console?.log $.parseJSON(xhr.responseText)

  ###
    Create layout manager in common/toppanel.jsp
  ###
  createLayoutManager = (page)->
    pageLayout = $("#optional-container").layout(
      defaults:
        size: 'auto'
        minSize: 50
        paneClass: "pane"     #default = 'ui-layout-pane'
        buttonClass: "button"  # default = 'ui-layout-button'
        togglerClass: "toggler"  # default = 'ui-layout-toggler'
        resizerClass: "resizer"  # default = 'ui-layout-resizer'
        contentSelector: ".content"  # inner div to auto-size so only it scrolls, not the entire pane!
        contentIgnoreSelector: "span"  # 'paneSelector' for content to 'ignore' when measuring room for content
        togglerLength_open: 35
        togglerLength_closed: 35
        hideTogglerOnSlide: true
        togglerTip_open: "Close This Pane"
        togglerTip_closed: "Open This Pane"
        resizerTip: "Resize This Pane"
        # effect defaults - overridden on some panes
        fxName: 'slide'
        fxSpeed_open: 750
        fxSpeed_close: 1500
        fxSettings_open: { easing: "easeInQuint" }
        fxSettings_close: { easing: "easeOutQuint" }
      north:
        minSize: 37
        #      spacing_open: 1
        #      togglerLength_open: 0
        togglerLength_closed: -1
        resizable: false
        # override default effect
        fxName: 'none'
      west:
        size: 250
        spacing_closed: 21      # wider space when closed
        togglerLength_closed: 21      # make toggler 'square' - 21x21
        togglerAlign_closed: "top"    # align to top of resizer
        togglerLength_open: 0      # NONE - using custom togglers INSIDE west-pane
        togglerTip_open: "Close West Pane"
        togglerTip_closed: "Open West Pane"
        resizerTip_open: "Resize West Pane"
        slideTrigger_open: "click"   # default
        initClosed: false
        #      #	add 'bounce' option to default 'slide' effect
        fxSettings_open: { easing: "easeOutBounce" }
    )

    #  create toolbar on north panel.
    #    pageLayout.addToggleBtn('#tbarToggleNorth', 'north')
    #    pageLayout.addOpenBtn("#tbarOpenSouth", "south")
    #    pageLayout.addCloseBtn("#tbarCloseSouth", "south")
    #
    #    pageLayout.addPinBtn("#tbarPinWest", "west")
    #    pageLayout.addPinBtn("#tbarPinEast", "east")

    pageLayout

  urlname2Action = (urlname = '', suffix = 'Action')->urlname.split('/').pop().capitalize().split('-').join('') + suffix
  checkGridPrivilege = (grid)->
    console?.log "check the privilege of grid '#{grid.id}'."
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
    $.each ['add', 'edit', 'del'], (index, value)->
    # for jqgrid predefined navigate buttons
      actButton = $("##{value}_#{grid.id}")

      if actButton.length > 0 and forbiddenTab.editurl
        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
        actButton.addClass 'ui-state-disabled'

      # for custom buttons in navigate gird.
      actButton = $("#custom_#{value}_#{grid.id}")
      if actButton.length > 0 and (forbiddenTab.editurl or forbiddenTab.cellactionurl)
        console?.log "Disable button #{actButton.attr('id')} due to forbidden privilege."
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
  checkAllGridPrivilege = (grids = $('.ui-jqgrid-btable'), readonly = true)->$.each grids, (idx, grid)->checkGridPrivilege grid


  #  for all the JSP pages
  pageNavi()
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
        outerTableFirstRow.append $("<td/>").append innerColTable
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
  setCookie: setCookie
  #  Retrieve the value of the cookie with the specified name.
  getCookie: (name)->
    for c in document.cookie.split("; ")
      [cname, value] = c.split('=')
      return value if cname == name
    null
  #  Del cookie
  delCookie: (name)->document.cookie = "#{name}=#{escape('')}; expires=Fri, 31 Dec 1999 23:59:59 GMT;"
  getUrlParams: (suffix = window.location.search or window.location.hash)->
    params = {}
    ([k, v]=param.split('=');params[k] = decodeURIComponent(v) ) for param in suffix.split('?')[1].split('&') if suffix
    params
  newOption: newOption
  ###
  convert a json array to a list of options.
  @params
  json: json array
  selectedValue: default selected option value

###
  json2Options: (json, selectedValue = false, textFieldName = "version", valueFieldName = "id", sep = '\n')->
    $(json).map(
      (index, elem)->
      #        selected = if !selectedValue then index == json.length - 1 else (String selectedValue) == (String @[valueFieldName])
        selected = (String selectedValue) == (String @[valueFieldName])
        newOption @[textFieldName], @[valueFieldName], selected
    ).get().join(sep)

  afterInitilized: (context)->
    console?.log "...Page #{param.naviTo} privilege check..."
    #    check all buttons' privilege
    $('[role=button][privilegeName]').each (index, button)->
    #    .attr('privilegeName', util.urlname2Action 'app/deliver-app-dict')
      $(button).button 'disable' if $(button).attr('privilegeName') in param.forbiddenPrivileges
    #   check all the grids' privilege
    checkAllGridPrivilege()

    # create layout
    pageLayout = createLayoutManager()

    if(param.naviTo == 'appmng.jsp')
    # save selector strings to vars so we don't have to repeat it
    # must prefix paneClass with "#optional-container >" to target ONLY the Layout panes
    # west pane
      westSelector = "#optional-container > .ui-layout-west"

      # CREATE SPANs for pin-buttons - using a generic class as identifiers
      $("<span></span>").addClass("pin-button").prependTo(westSelector)
      # BIND events to pin-buttons to make them functional
      pageLayout.addPinBtn("#{westSelector} .pin-button", "west")

      # CREATE SPANs for close-buttons - using unique IDs as identifiers
      $("<span></span>").attr("id", "west-closer").prependTo(westSelector)
      # BIND layout events to close-buttons to make them functional
      pageLayout.addCloseBtn("#west-closer", "west")

    #    update navigator.
    $('span[id$=Tab][id^=nav]').button().click(
      (e)->
        $('#pageNavigator').val $(@).attr('value')
        $(@).button 'disable'
        $('#naviForm').submit()
    ).parent().buttonset()
  urlname2Action: urlname2Action
  createLayoutManager: (page = 'appmng.jsp')->createLayoutManager(page)


