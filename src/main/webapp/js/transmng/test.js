// Generated by CoffeeScript 1.3.3
(function() {
  var a, formatJonString;

  Object.prototype.isArray = function() {
    return Object.prototype.toString.call(this) === "[object Array]";
  };

  String.prototype.format = function() {
    var args;
    args = arguments;
    return this.replace(/\{(\d+)\}/g, function(m, i) {
      return args[i];
    });
  };

  String.prototype.endWith = function(str) {
    if (!str || str.length > this.length) {
      return false;
    }
    return this.substring(this.length - str.length) === str;
  };

  String.prototype.startWith = function(str) {
    if (!str || str.length > this.length) {
      return false;
    }
    return this.substr(0, str.length) === str;
  };

  Array.prototype.insert = function(pos, elem) {
    var newarray, _i, _len, _results;
    newarray = this.slice(0, pos);
    if (elem.isArray()) {
      newarray = newarray.concat(elem.slice(0));
    } else {
      newarray.push(elem);
    }
    newarray = newarray.concat(this.slice(pos, this.length));
    this.length = 0;
    _results = [];
    for (_i = 0, _len = newarray.length; _i < _len; _i++) {
      elem = newarray[_i];
      _results.push(this.push(elem));
    }
    return _results;
  };

  Array.prototype.remove = function(start, len) {
    var delElem, elem, newarray, _i, _len;
    if (!len) {
      len = 1;
    }
    newarray = this.slice(0, start);
    newarray = newarray.concat(this.slice(start + len, this.length));
    delElem = len > 1 ? this.slice(start, start + len) : this[start];
    this.length = 0;
    for (_i = 0, _len = newarray.length; _i < _len; _i++) {
      elem = newarray[_i];
      this.push(elem);
    }
    return delElem;
  };

  formatJonString = function(jsonString) {
    var char, i, indentStr, j, k, newLine, pos, retval, str;
    str = jsonString;
    pos = i = 0;
    indentStr = "  ";
    newLine = "\n";
    retval = '';
    while (i < str.length) {
      char = str.substring(i, i + 1);
      if (char === "}" || char === "]") {
        retval += newLine;
        --pos;
        j = 0;
        while (j < pos) {
          retval += indentStr;
          j++;
        }
      }
      retval += char;
      if (char === "{" || char === "[" || char === ",") {
        retval += newLine;
        if (char === "{" || char === "[") {
          ++pos;
        }
        k = 0;
        while (k < pos) {
          retval += indentStr;
          k++;
        }
      }
      i++;
    }
    return retval;
  };

  a = {
    init: (function(me) {
      return {
        a: '123,',
        b: '456'
      };
    })(this),
    name: this.a.init.b
  };

}).call(this);