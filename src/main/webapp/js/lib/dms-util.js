// Generated by CoffeeScript 1.5.0

/*
Created by IntelliJ IDEA.
User: Guoshun Wu
*/


(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(['jqueryui', "jqtree", "i18n!nls/common"], function($, jqtree, c18n) {
    var PanelGroup, checkAllGridPrivilege, checkGridPrivilege, formatJonString, genProgressBar, getTreeNodeInfo, long_polling, newOption, randomStr, sessionCheck, urlname2Action;
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
    String.prototype.repeat = function(num) {
      var buf, i;
      i = 0;
      buf = '';
      while (i++ < num) {
        buf += this;
      }
      return buf;
    };
    String.prototype.center = function(width, padding) {
      var len, pads, remain;
      if (padding == null) {
        padding = ' ';
      }
      if (this.length >= width) {
        return this;
      }
      padding = padding.slice(0, 1);
      len = width - this.length;
      remain = 0 === len % 2 ? "" : padding;
      pads = padding.repeat(parseInt(len / 2));
      return pads + this + pads + remain;
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
    randomStr = function(length, alphbet) {
      var ch, rstr, _i, _len;
      if (length == null) {
        length = 10;
      }
      if (alphbet == null) {
        alphbet = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz';
      }
      rstr = '';
      for (_i = 0, _len = alphbet.length; _i < _len; _i++) {
        ch = alphbet[_i];
        rstr += alphbet[Math.floor(Math.random() * alphbet.length)];
        length--;
        if (0 === length) {
          break;
        }
      }
      return rstr;
    };
    /*
      generate a progress bar
    */

    genProgressBar = function(autoDispaly, autoRemoveWhenCompleted) {
      var pbContainer, randStr;
      if (autoDispaly == null) {
        autoDispaly = true;
      }
      if (autoRemoveWhenCompleted == null) {
        autoRemoveWhenCompleted = true;
      }
      randStr = randomStr(5);
      pbContainer = $("<div id=\"pb_container_" + randStr + "\"  class=\"progressbar-container\">\n<div class=\"progressbar-msg\">\nLoading...\n</div>\n<div id=\"progressbar_" + randStr + "\" class=\"progressbar\">\n<div class=\"progressbar-label\">0.00%</div>\n</div>\n</div>").appendTo(document.body).draggable({
        create: function() {
          return $("#progressbar_" + randStr, this).progressbar({
            max: 100,
            create: function(e, ui) {
              this.label = $('div.progressbar-label', this);
              return this.msg = $('div.progressbar-msg', pbContainer);
            },
            change: function(e, ui) {
              if ($(this).is(":data(msg)")) {
                this.msg.html($(this).data('msg'));
              }
              return this.label.html("" + ($(this).progressbar('value').toPrecision(4)) + "%");
            },
            complete: function(e, ui) {
              if (autoRemoveWhenCompleted) {
                return pbContainer.remove();
              }
            }
          });
        }
      }).hide();
      if (autoDispaly) {
        pbContainer.show().position({
          my: 'center',
          at: 'center',
          of: window
        });
      }
      return $("#progressbar_" + randStr, pbContainer);
    };
    long_polling = function(url, postData, callback, pb) {
      var pollingInterval, reTryAjax;
      if (!postData || !postData.pqCmd) {
        postData.pqCmd = 'start';
      }
      pollingInterval = $("#pollingFreq").val() ? parseInt($("#pollingFreq").val()) : 1000;
      reTryAjax = function(retryTimes, retryCounter) {
        if (retryTimes == null) {
          retryTimes = Number.MAX_VALUE;
        }
        if (retryCounter == null) {
          retryCounter = 0;
        }
        return $.ajax(url, {
          cache: false,
          data: postData,
          type: 'post',
          dataType: "json"
        }).done(function(data, textStatus, jqXHR) {
          if ('error' === data.event.cmd) {
            $.msgBox(event.msg, null, {
              title: c18n.error
            });
            return;
          }
          if ('done' === data.event.cmd) {
            if (typeof callback === "function") {
              callback(data);
            }
            return;
          }
          if (pb) {
            pb.toggleClass('progressbar-indeterminate', -1 === data.event.percent);
            pb.data('msg', data.event.msg);
            pb.get(0).msg.html(data.event.msg);
            pb.progressbar('value', data.event.percent);
          } else {
            if (typeof callback === "function") {
              callback(data);
            }
          }
          return setTimeout((function() {
            return long_polling(url, {
              pqCmd: 'process',
              pqId: data.pqId
            }, callback, pb);
          }), pollingInterval);
        }).fail(function(jqXHR, textStatus, errorThrown) {
          if ('timeout' !== textStatus) {
            return;
          }
          if (retryTimes > 0) {
            return setTimeout((function() {
              return reTryAjax(--retryTimes, ++retryCounter);
            }), pollingInterval);
          } else {

          }
        });
      };
      return reTryAjax();
    };
    getTreeNodeInfo = function(node, treeSelecotr) {
      var parent, ptree, selectedNode, type;
      if (treeSelecotr == null) {
        treeSelecotr = '#appTree';
      }
      ptree = $.jstree._reference(treeSelecotr);
      if (!ptree) {
        return null;
      }
      selectedNode = node ? node : ptree.get_selected();
      if (0 === selectedNode.length) {
        return null;
      }
      parent = ptree._get_parent(selectedNode);
      type = selectedNode.attr('type');
      if (type === 'product') {
        type = 'prod';
      }
      return {
        id: selectedNode.attr('id'),
        text: ptree.get_text(selectedNode),
        type: type,
        parent: parent !== -1 ? getTreeNodeInfo(parent) : parent
      };
    };
    newOption = function(text, value, selected) {
      return "<option " + (selected ? 'selected ' : '') + "value='" + value + "'>" + text + "</option>";
    };
    urlname2Action = function(urlname, suffix) {
      if (urlname == null) {
        urlname = '';
      }
      if (suffix == null) {
        suffix = 'Action';
      }
      return urlname.split('/').pop().capitalize().split('-').join('') + suffix;
    };
    $.ajaxSetup({
      timeout: 1000 * 60 * 30,
      cache: false
    });
    $.ajaxPrefilter(function(options, originalOptions, jqXHR) {});
    sessionCheck = function() {
      $('#sessionTimeoutDialog').dialog({
        width: 320,
        modal: true,
        autoOpen: false,
        buttons: [
          {
            text: c18n.ok,
            click: function(e) {
              $(this).dialog('close');
              return window.location = 'login/forward-to-https';
            }
          }
        ]
      });
      return $(document).on('ajaxSuccess', function(e, xhr, settings) {
        if (203 === xhr.status) {
          return $('#sessionTimeoutDialog').dialog('open');
        }
      });
    };
    checkGridPrivilege = function(grid) {
      var forbiddenTab, gridParam, tmpHandlers, _ref, _ref1, _ref2;
      gridParam = $(grid).jqGrid('getGridParam');
      forbiddenTab = {
        cellurl: (_ref = urlname2Action(gridParam.cellurl), __indexOf.call(param.forbiddenPrivileges, _ref) >= 0),
        editurl: (_ref1 = urlname2Action(gridParam.editurl), __indexOf.call(param.forbiddenPrivileges, _ref1) >= 0),
        cellactionurl: (_ref2 = urlname2Action(gridParam.cellactionurl), __indexOf.call(param.forbiddenPrivileges, _ref2) >= 0)
      };
      if (forbiddenTab.cellurl) {
        $.each(gridParam.colModel, function(idx, obj) {
          if ($.isPlainObject(obj) && obj.name && obj.editable) {
            obj.editable = false;
            return obj.classes = obj.classes.replace('editable-column', '');
          }
        });
      }
      $.each(['add', 'edit', 'del'], function(index, value) {
        var actButton, btnSelector;
        btnSelector = "#" + value + "_" + grid.id;
        actButton = $(btnSelector);
        if (actButton.length > 0 && forbiddenTab.editurl) {
          actButton.addClass('ui-state-disabled');
        }
        btnSelector = "#custom_" + value + "_" + grid.id;
        actButton = $(btnSelector);
        if (actButton.length > 0 && (forbiddenTab.editurl || forbiddenTab.cellactionurl)) {
          return actButton.addClass('ui-state-disabled');
        }
      });
      if (forbiddenTab.cellactionurl) {
        $.each(gridParam.colModel, function(idx, obj) {
          if ($.isPlainObject(obj) && obj.name === 'cellaction') {
            obj.formatoptions.delbutton = false;
            return obj.formatoptions.editbutton = false;
          }
        });
      }
      tmpHandlers = gridParam.cellactionhandlers;
      if (tmpHandlers) {
        return $.each(tmpHandlers, function(index, value) {
          var _ref3;
          if (_ref3 = urlname2Action(value.url), __indexOf.call(param.forbiddenPrivileges, _ref3) >= 0) {
            return delete tmpHandlers[index];
          }
        });
      }
    };
    checkAllGridPrivilege = function(grids, readonly) {
      if (grids == null) {
        grids = $('table.ui-jqgrid-btable');
      }
      if (readonly == null) {
        readonly = true;
      }
      return $.each(grids, function(idx, grid) {
        return checkGridPrivilege(grid);
      });
    };
    sessionCheck();
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
            outerTableFirstRow.append($("<td></td>").append(innerColTable));
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
      getUrlParams: function(href) {
        var k, lastPos, param, params, suffix, v, _i, _len, _ref, _ref1;
        if (href == null) {
          href = window.location.href;
        }
        lastPos = !href.endWith('#') ? -1 : -2;
        suffix = href.slice(href.lastIndexOf('?') + 1, +lastPos + 1 || 9e9);
        params = {};
        if (suffix) {
          _ref = suffix.split('&');
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
      },
      afterInitilized: function(context) {
        $('div.progressbar').position({
          my: 'center',
          at: 'center',
          of: window
        });
        $('[role=button][privilegeName]').each(function(index, button) {
          var _ref;
          if (_ref = $(button).attr('privilegeName'), __indexOf.call(param.forbiddenPrivileges, _ref) >= 0) {
            return $(button).button('disable');
          }
        });
        return checkAllGridPrivilege();
      },
      urlname2Action: urlname2Action,
      createLayoutManager: function(page) {
        if (page == null) {
          page = 'appmng.jsp';
        }
        return createLayoutManager(page);
      },
      getProductTreeInfo: getTreeNodeInfo,
      genProgressBar: genProgressBar,
      updateProgress: long_polling,
      /*
        @param panels: the panel group selector
        @param currentPanel: the current panel selector
        @param onSwitch: the handler on panel switch
      */

      PanelGroup: PanelGroup = (function() {

        function PanelGroup(panels, currentPanel, onSwitch) {
          this.panels = panels;
          this.currentPanel = currentPanel;
          this.onSwitch = onSwitch != null ? onSwitch : function(oldpnl, newpnl) {};
        }

        PanelGroup.prototype.switchTo = function(panelId, callback) {
          var oldPanel;
          $("" + this.panels).hide();
          oldPanel = this.currentPanel;
          this.currentPanel = panelId;
          $("" + this.panels + "[id='" + panelId + "']").fadeIn("fast", function() {
            if ($.isFunction(callback)) {
              return callback();
            }
          });
          if ($.isFunction(this.onSwitch) && oldPanel !== this.currentPanel) {
            return this.onSwitch(oldPanel, this.currentPanel);
          }
        };

        return PanelGroup;

      })()
    };
  });

}).call(this);
