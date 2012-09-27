Object:: isArray = -> Object.prototype.toString.call(@) == "[object Array]"

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
  retval=''

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
        retval +=indentStr
        k++
    i++
  retval

a = {a:111,b:222}
console.log formatJonString JSON.stringify(a)


