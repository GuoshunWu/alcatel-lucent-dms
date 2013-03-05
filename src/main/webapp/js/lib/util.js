// Generated by CoffeeScript 1.5.0

/*
Created by IntelliJ IDEA.
User: Guoshun Wu
Date: -8-
Time: 下午7:
To change this template use File | Settings | File Templates.
*/


(function() {
  var __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  define(["jquery", "jqueryui", 'jqmsgbox', 'jqlayout', "i18n!nls/common"], function($, ui, msgbox, layout, c18n) {
    var PanelGroup, checkAllGridPrivilege, checkGridPrivilege, createLayoutManager, formatJonString, genProgressBar, long_polling, newOption, pageNavi, randomStr, sessionCheck, setCookie, urlname2Action;
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
      pbContainer = $("<div id=\"pb_container_" + randStr + "\"  class=\"progressbar-container\">\n<div class=\"progressbar-msg\">\nLoading...\n</div>\n<div id=\"progressbar_" + randStr + "\" class=\"progressbar progressbar-indeterminate\">\n<div class=\"progressbar-label\">0.00%</div>\n</div>\n</div>").appendTo(document.body).draggable({
        create: function() {
          return $("#progressbar_" + randStr, this).progressbar({
            max: 100,
            create: function(e, ui) {
              this.label = $('div.progressbar-label', this);
              return this.msg = $('div.progressbar-msg', pbContainer);
            },
            change: function(e, ui) {
              var _ref;
              $(this).toggleClass('progressbar-indeterminate', (_ref = $(this).progressbar('value')) === 0 || _ref === (-1));
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
    /*
      postData pqCmd should not be passed as a property
      callback callback function
      pb a jquey progressbar
    */

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
          if (typeof console !== "undefined" && console !== null) {
            console.log(data);
          }
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
            pb.data('msg', data.event.msg);
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
            if (typeof console !== "undefined" && console !== null) {
              console.log("error: " + textStatus);
            }
            return;
          }
          if (retryTimes > 0) {
            if (typeof console !== "undefined" && console !== null) {
              console.log("Request " + textStatus + ", I will retry in " + pollingInterval + " milliseconds.");
            }
            return setTimeout((function() {
              return reTryAjax(--retryTimes, ++retryCounter);
            }), pollingInterval);
          } else {
            return typeof console !== "undefined" && console !== null ? console.log("I have retried " + retryCounter + " times. There may be a network connection issue, please check network cable.") : void 0;
          }
        });
      };
      return reTryAjax();
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
    $.ajaxSetup({
      timeout: 1000 * 60 * 30,
      cache: false
    });
    $.ajaxPrefilter(function(options, originalOptions, jqXHR) {});
    pageNavi = function() {
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
      return $('#pageNavigator').change(function(e) {
        return $('#naviForm').submit();
      });
    };
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
    /*
      Create layout manager in common/toppanel.jsp
    */

    createLayoutManager = function(page) {
      var pageLayout;
      pageLayout = $("#optional-container").layout({
        defaults: {
          size: 'auto',
          minSize: 50,
          paneClass: "pane",
          buttonClass: "button",
          togglerClass: "toggler",
          resizerClass: "resizer",
          contentSelector: ".content",
          contentIgnoreSelector: "span",
          togglerLength_open: 35,
          togglerLength_closed: 35,
          hideTogglerOnSlide: true,
          togglerTip_open: "Close This Pane",
          togglerTip_closed: "Open This Pane",
          resizerTip: "Resize This Pane",
          fxName: 'slide',
          fxSpeed_open: 750,
          fxSpeed_close: 1500,
          fxSettings_open: {
            easing: "easeInQuint"
          },
          fxSettings_close: {
            easing: "easeOutQuint"
          }
        },
        north: {
          minSize: 37,
          togglerLength_closed: -1,
          resizable: false,
          fxName: 'none'
        },
        west: {
          size: 250,
          spacing_closed: 21,
          togglerLength_closed: 21,
          togglerAlign_closed: "top",
          togglerLength_open: 0,
          slideTrigger_open: "click",
          initClosed: false,
          resizable: true,
          fxSettings_open: {
            easing: "easeOutBounce"
          }
        }
      });
      return pageLayout;
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
        var actButton;
        actButton = $("#" + value + "_" + grid.id);
        if (actButton.length > 0 && forbiddenTab.editurl) {
          if (typeof console !== "undefined" && console !== null) {
            console.log("Disable button " + (actButton.attr('id')) + " due to forbidden privilege.");
          }
          actButton.addClass('ui-state-disabled');
        }
        actButton = $("#custom_" + value + "_" + grid.id);
        if (actButton.length > 0 && (forbiddenTab.editurl || forbiddenTab.cellactionurl)) {
          if (typeof console !== "undefined" && console !== null) {
            console.log("Disable button " + (actButton.attr('id')) + " due to forbidden privilege.");
          }
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
        grids = $('.ui-jqgrid-btable');
      }
      if (readonly == null) {
        readonly = true;
      }
      return $.each(grids, function(idx, grid) {
        return checkGridPrivilege(grid);
      });
    };
    pageNavi();
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
        var pageLayout, westSelector;
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
        checkAllGridPrivilege();
        pageLayout = createLayoutManager();
        if (param.naviTo === 'appmng.jsp') {
          westSelector = "#optional-container > .ui-layout-west";
          $("<span></span>").addClass("pin-button").prependTo(westSelector);
          pageLayout.addPinBtn("" + westSelector + " .pin-button", "west");
          $("<span></span>").attr("id", "west-closer").prependTo(westSelector);
          pageLayout.addCloseBtn("#west-closer", "west");
        }
        $("span[id$='Tab'][id^='nav']").button().click(function(e) {
          $('#pageNavigator').val($(this).attr('value'));
          $(this).button('disable');
          return $('#naviForm').submit();
        }).parent().buttonset();
        return $("span[id^='nav'][value='" + param.naviTo + "']").css('backgroundImage', 'url(css/jqueryLayout/images/80ade5_40x100_textures_04_highlight_hard_100.png)');
      },
      urlname2Action: urlname2Action,
      createLayoutManager: function(page) {
        if (page == null) {
          page = 'appmng.jsp';
        }
        return createLayoutManager(page);
      },
      genProgressBar: genProgressBar,
      updateProgress: long_polling,
      PanelGroup: PanelGroup = (function() {

        function PanelGroup(panels, currentPanel) {
          this.panels = panels;
          this.currentPanel = currentPanel;
        }

        PanelGroup.prototype.switchTo = function(panelId, callback) {
          $("" + this.panels).hide();
          this.currentPanel = panelId;
          return $("" + this.panels + "[id='" + panelId + "']").fadeIn("fast", function() {
            if ($.isFunction(callback)) {
              return callback();
            }
          });
        };

        return PanelGroup;

      })()
    };
  });

}).call(this);
