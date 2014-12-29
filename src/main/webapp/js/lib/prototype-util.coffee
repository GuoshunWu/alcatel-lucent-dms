define [
], (
)->

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

  Array::unique=()->
    output = {}
    output[@[key]] = @[key] for key in [0...@length]
    value for key, value of output

  {
    "string": String,
    "array": Array
    "date": Date
  }
