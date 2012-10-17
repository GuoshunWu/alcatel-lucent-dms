###
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
To change this template use File | Settings | File Templates.
###
define ["jquery"], ($) ->

#    prototype enhancement
  String:: format = -> args = arguments; @replace /\{(\d+)\}/g, (m, i) ->args[i]

  String:: endWith = (str) ->
    return false  if !str or str.length > @length
    @substring(@length - str.length) is str

  String:: startWith = (str) ->
    return false  if !str or str.length > @length
    @substr(0, str.length) is str
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

  ###
  Test here.
  ###
  #  a=[1,2,3]
  #  console.log a.insert 1,["a",'b']


  json2string: (jsonObj) ->formatJonString JSON.stringify(jsonObj)
  getDictLanguagesByDictId: (id,callback)->$.getJSON 'rest/languages', {prop: 'id,name', dict: id}, (languages)=>callback languages
