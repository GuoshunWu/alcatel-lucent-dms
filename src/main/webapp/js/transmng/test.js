// Generated by CoffeeScript 1.3.3
(function() {
  var a;

    $.getJSON(url, {}, function(json) {
        return $("#version").append($(json).map(function() {
            return new Option(this.version, this.id);
        })).trigger("change");
    });

  Object.prototype.isArray = function() {
    return Object.prototype.toString.call(this) === "[object Array]";
  };

  Array.prototype.insert = function(pos, elem) {
    var e, newarray, _i, _j, _len, _len1;
    newarray = this.slice(0, pos);
    if (elem.isArray()) {
      for (_i = 0, _len = elem.length; _i < _len; _i++) {
        e = elem[_i];
        newarray.push(e);
      }
    } else {
      newarray.push(elem);
    }
    newarray = newarray.concat(this.slice(pos, this.length));
    this.length = 0;
    for (_j = 0, _len1 = newarray.length; _j < _len1; _j++) {
      elem = newarray[_j];
      this.push(elem);
    }
    return elem;
  };

  Array.prototype.remove = function(pos) {
    var delElem, elem, newarray, _i, _len;
    newarray = this.slice(0, pos);
    newarray = newarray.concat(this.slice(pos + 1, this.length));
    delElem = this[pos];
    this.length = 0;
    for (_i = 0, _len = newarray.length; _i < _len; _i++) {
      elem = newarray[_i];
      this.push(elem);
    }
    return delElem;
  };

  a = ["a", "b", "c"];

  console.log(a.insert(1, ['ad', 'df', 'dfdf']));

  console.log(a);

}).call(this);
