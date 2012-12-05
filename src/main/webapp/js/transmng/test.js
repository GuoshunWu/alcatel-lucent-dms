// Generated by CoffeeScript 1.3.3
(function() {
  var action, formatJonString, handlers, k, v;

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

  Date.prototype.format = function(format) {
    var k, o, v;
    o = {
      'M+': this.getMonth() + 1,
      "d+": this.getDate(),
      "h+": this.getHours(),
      "m+": this.getMinutes(),
      "s+": this.getSeconds(),
      "q+": Math.floor((this.getMonth() + 3) / 3),
      "S": this.getMilliseconds()
    };
    if (/(y+)/.test(format)) {
      format = format.replace(RegExp.$1, this.getFullYear()).substr(4 - RegExp.$1.length);
    }
    for (k in o) {
      v = o[k];
      if (new RegExp("(" + k + ")").test(format)) {
        format = format.replace(RegExp.$1, RegExp.$1.length === 1 ? v : ("00" + v).substr(("" + v).length));
      }
    }
    return format;
  };

  handlers = {
    'Download': function(param) {
      return alert('download');
    },
    'History…': function(param) {
      return alert('History…');
    },
    'End': function(param) {
      return alert('End');
    },
    'X': function(param) {
      return alert('X');
    }
  };

  action = [];

  for (k in handlers) {
    v = handlers[k];
    action.push(k);
  }

  console.log(action);

}).call(this);
