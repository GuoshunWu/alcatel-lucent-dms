// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, charsetgrid, languagegrid, pageLayout, util;
    $ = require('jqgrid');
    require('jqlayout');
    util = require('util');
    charsetgrid = require('admin/charsetgrid');
    languagegrid = require('admin/languagegrid');
    pageLayout = $("#optional-container").layout({
      resizable: true,
      closable: true
    });
    $(".header-footer").hover((function() {
      return $(this).addClass("ui-state-hover");
    }), function() {
      return $(this).removeClass("ui-state-hover");
    });
    $('#tabs').tabs({});
    return $('#loading-container').remove();
  });

}).call(this);
