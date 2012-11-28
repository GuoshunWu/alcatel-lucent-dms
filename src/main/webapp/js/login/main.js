// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, i18n;
    $ = require('formvalidate');
    i18n = require('i18n!nls/login');
    $.formValidator.initConfig({
      formID: "loginForm",
      autoTip: true
    }, {
      onError: function(msg) {}
    }, {
      inIframe: false
    });
    $("#idLoginname").formValidator({
      onShow: i18n.nameshowtip,
      onFocus: i18n.namefocustip,
      onCorrect: ""
    }).inputValidator({
      min: 1,
      max: 30,
      onError: i18n.nameerrtip
    });
    $("#idPassword").formValidator({
      onShow: i18n.pwdshowtip,
      onFocus: i18n.pwdfocustip,
      onCorrect: ""
    }).inputValidator({
      min: 1,
      max: 30,
      onError: i18n.pwderrtip
    });
    return $('#loginForm').bind('submit', function() {
      if ($.formValidator.pageIsValid()) {
        return $("#idSubmit").attr('disabled', true).css('color', 'grey');
      }
    });
  });

}).call(this);
