define [
], (
)->


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

  (
    window.console=
      log:->
      warn:->
      info:->
  )unless window.console

  randomStr = (length = 10, alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz')->
    rstr = ''
    for ch in alphbet
      rstr += alphbet[Math.floor Math.random() * alphbet.length]
      length--
      break if 0 == length
    rstr

  randomNum = (min=0, max=100)->delta = max - min;  Math.floor Math.random() * delta + min

  formatJonString: formatJonString
  randomStr: randomStr
  randomNum: randomNum