// Generated by CoffeeScript 1.5.0
(function() {

  define(function(require) {
    var $, dialogs, grid, i18n, init, nodeSelectHandler, onShow, ready, urls, util;
    $ = require('jqueryui');
    i18n = require('i18n!nls/common');
    util = require('dms-util');
    urls = require('dms-urls');
    grid = require('taskmng/task_grid');
    dialogs = 'taskmng/dialogs';
    nodeSelectHandler = function(node, nodeInfo) {
      var type;
      type = node.attr('type');
      if ('products' === type) {
        return;
      }
      $('#versionTypeLabel', "div[id='taskmng']").text("" + nodeInfo.text);
      if ('product' === type) {
        $.getJSON(urls.prod_versions, {
          base: nodeInfo.id,
          prop: 'id,version'
        }, function(json) {
          return $('#selVersion', "div[id='taskmng']").empty().append(util.json2Options(json)).trigger('change');
        });
        return;
      }
      if ('app' === type) {
        return $.getJSON("" + urls.app_versions + nodeInfo.id, function(json) {
          return $('#selVersion', "div[id='taskmng']").empty().append(util.json2Options(json)).trigger('change');
        });
      }
    };
    onShow = function() {
      var gridParent;
      gridParent = $('.taskGrid_parent');
      return $('#taskGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110);
    };
    init = function() {
      return typeof console !== "undefined" && console !== null ? console.debug("transmng panel init...") : void 0;
    };
    ready = function(param) {
      if (typeof console !== "undefined" && console !== null) {
        console.debug("transmng panel ready...");
      }
      return $('#selVersion', '#taskmng').change(function() {
        var nodeInfo, postData, type;
        if (!this.value || -1 === parseInt(this.value)) {
          return;
        }
        nodeInfo = (require('ptree')).getNodeInfo();
        type = nodeInfo.type;
        if (type.startWith('prod')) {
          type = type.slice(0, 4);
        }
        postData = {
          prop: 'id,name'
        };
        postData[type] = this.value;
        param = {
          release: {
            id: $(this).val(),
            version: $(this).find("option:selected").text()
          },
          type: type
        };
        if (!param.release.id || parseInt(param.release.id) === -1) {
          return false;
        }
        return grid.productVersionChanged(param);
      });
    };
    init();
    ready(this);
    return {
      onShow: onShow,
      nodeSelect: nodeSelectHandler
    };
  });

}).call(this);
