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

a = [1, 2, 3, 4, 5]
console.log a.remove 2
console.log a

