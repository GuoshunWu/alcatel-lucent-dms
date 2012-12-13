// Generated by CoffeeScript 1.3.3

/*
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
To change this template use File | Settings | File Templates.
*/


(function() {

  define(["jquery"], function($) {
    var formatJonString, newOption, setCookie;
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
    String.prototype.capitalize = function() {
      return this.toLowerCase().replace(/\b[a-z]/g, function(letter) {
        return letter.toUpperCase();
      });
    };
    /*
        Dateformat
    */

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
    /*
      insert elem at pos in array.
    */

    Array.prototype.insert = function(pos, elem) {
      var newarray, _i, _len;
      newarray = this.slice(0, pos);
      if ($.isArray(elem)) {
        newarray = newarray.concat(elem.slice(0));
      } else {
        newarray.push(elem);
      }
      newarray = newarray.concat(this.slice(pos, this.length));
      this.length = 0;
      for (_i = 0, _len = newarray.length; _i < _len; _i++) {
        elem = newarray[_i];
        this.push(elem);
      }
      return this;
    };
    /*
      remove the element at pos in array, return the removed element.
    */

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
    /*
      format json string to pretty.
    */

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
    setCookie = function(name, value, expires, domain, path, secure) {
      var arg, c, start, _i, _len, _ref;
      c = "" + name + "=" + (escape(value));
      start = 2;
      _ref = ['expires', 'domain', 'path', 'secure'];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        arg = _ref[_i];
        if (arguments[start]) {
          c += ";" + arg + "=" + arguments[start++];
        }
      }
      return document.cookie = c;
    };
    newOption = function(text, value, selected) {
      return "<option " + (selected ? 'selected ' : '') + "value='" + value + "'>" + text + "</option>";
    };
    $('#naviForm').bind('submit', function(e) {
      var appTree, node, productBaseId, type;
      $("#curProductBaseId").val($("#productBase").val());
      $("#curProductId").val($("#productRelease").val());
      if (param.naviTo === 'appmng.jsp') {
        $("#curProductId").val($("#selVersion").val() ? $("#selVersion").val() : -1);
        appTree = $.jstree._reference("#appTree");
        node = appTree.get_selected();
        productBaseId = -1;
        if (node.length > 0) {
          type = node.attr('type');
          if (type === 'product') {
            productBaseId = node.attr('id');
          } else if (type === 'app') {
            productBaseId = appTree._get_parent(node).attr('id');
          }
        }
        return $("#curProductBaseId").val(productBaseId);
      }
    });
    $('#pageNavigator').change(function(e) {
      return $('#naviForm').submit();
    });
    return {
      /*
        Test here.
      */

      generateLanguageTable: function(languages, tableId, colNum) {
        var checkedAll, innerColTable, languageCells, languageFilterTable, outerTableFirstRow, rowCount;
        if (!tableId) {
          tableId = 'languageFilterTable';
        }
        if (!colNum) {
          colNum = 5;
        }
        rowCount = Math.ceil(languages.length / colNum);
        languageFilterTable = $("<table id='" + tableId + "' align='center'width='100%' border='0'><tr valign='top' /></table>");
        outerTableFirstRow = $("tr:eq(0)", languageFilterTable);
        languageCells = $(languages).map(function() {
          return $("<td><input type='checkbox' checked value=\"" + this.name + "\" name='languages' id=" + this.id + " /><label for=" + this.id + ">" + this.name + "</label></td>").css('width', '180px');
        });
        innerColTable = null;
        languageCells.each(function(index) {
          if (0 === index % rowCount) {
            innerColTable = $("<table border='0'/>");
            outerTableFirstRow.append($("<td/>").append(innerColTable));
          }
          return innerColTable.append($("<tr/>").append(this));
        });
        checkedAll = $("<input type='checkbox'id='all_" + tableId + "' checked><label for='all_" + tableId + "'>All</label>").change(function() {
          return $(":checkbox[name='languages']", languageFilterTable).attr('checked', this.checked);
        });
        languageFilterTable.append($('<tr/>').append($("<td colspan='" + colNum + "'/>").append($("<hr width='100%'>"))));
        return languageFilterTable.append($('<tr/>').append($("<td colspan='" + colNum + "'></td>").append(checkedAll)));
      },
      json2string: function(jsonObj) {
        return formatJonString(JSON.stringify(jsonObj));
      },
      getDictLanguagesByDictId: function(id, callback) {
        var _this = this;
        return $.getJSON('rest/languages', {
          prop: 'id,name',
          dict: id
        }, function(languages) {
          return callback(languages);
        });
      },
      setCookie: setCookie,
      getCookie: function(name) {
        var c, cname, value, _i, _len, _ref, _ref1;
        _ref = document.cookie.split("; ");
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          c = _ref[_i];
          _ref1 = c.split('='), cname = _ref1[0], value = _ref1[1];
          if (cname === name) {
            return value;
          }
        }
        return null;
      },
      delCookie: function(name) {
        return document.cookie = "" + name + "=" + (escape('')) + "; expires=Fri, 31 Dec 1999 23:59:59 GMT;";
      },
      getUrlParams: function(suffix) {
        var k, param, params, v, _i, _len, _ref, _ref1;
        if (suffix == null) {
          suffix = window.location.search || window.location.hash;
        }
        params = {};
        if (suffix) {
          _ref = suffix.split('?')[1].split('&');
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            param = _ref[_i];
            _ref1 = param.split('='), k = _ref1[0], v = _ref1[1];
            params[k] = decodeURIComponent(v);
          }
        }
        return params;
      },
      newOption: newOption,
      /*
        convert a json array to a list of options.
        @params
        json: json array
        selectedValue: default selected option value
      */

      json2Options: function(json, selectedValue, textFieldName, valueFieldName, sep) {
        if (selectedValue == null) {
          selectedValue = false;
        }
        if (textFieldName == null) {
          textFieldName = "version";
        }
        if (valueFieldName == null) {
          valueFieldName = "id";
        }
        if (sep == null) {
          sep = '\n';
        }
        return $(json).map(function(index, elem) {
          var selected;
          selected = (String(selectedValue)) === (String(this[valueFieldName]));
          return newOption(this[textFieldName], this[valueFieldName], selected);
        }).get().join(sep);
      }
    };
  });

}).call(this);
