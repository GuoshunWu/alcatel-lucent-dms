Object.prototype.isArray = -> Object.prototype.toString.call(this) == "[object Array]"

Array.prototype.insert= (pos, elem) ->
  newarray=this.slice(0,pos)
  if(elem.isArray())
    newarray.push e for e in elem
  else
    newarray.push(elem)
  newarray = newarray.concat(this.slice(pos,this.length))
  this.length=0
  this.push(elem) for elem in newarray
  elem
Array.prototype.remove= (pos) ->
  newarray=this.slice(0,pos)
  newarray=newarray.concat(this.slice(pos+1, this.length))
  delElem=this[pos]
  this.length=0
  this.push(elem) for elem in newarray
  return delElem

#
a=["a","b","c"]
console.log a.insert(1,['ad','df','dfdf'])
console.log(a)

