#Object:: isArray = -> Object.prototype.toString.call(@) == "[object Array]"

String:: format = -> args = arguments; @replace /\{(\d+)\}/g, (m, i) ->args[i]

String:: endWith = (str) ->
  return false  if !str or str.length > @length
  @substring(@length - str.length) is str

String:: startWith = (str) ->
  return false  if !str or str.length > @length
  @substr(0, str.length) is str

Array:: insert = (pos, elem) ->
  newarray = @slice(0, pos)
  if(elem.isArray())
    newarray = newarray.concat elem.slice 0
  else
    newarray.push(elem)
  newarray = newarray.concat(@slice(pos, @length))
  @length = 0
  @push(elem) for elem in newarray

Array:: remove = (start, len) ->
  len = 1 if !len
  newarray = @slice(0, start)
  newarray = newarray.concat(@slice(start + len, @length))
  delElem = if len > 1 then @slice start, start + len else @[start]
  @length = 0
  @push(elem) for elem in newarray
  delElem

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

console.log new Date().format('yyyy-MM-dd')





