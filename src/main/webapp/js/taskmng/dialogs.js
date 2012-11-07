// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqueryui', 'require', 'taskmng/taskreport_grid', 'taskmng/transdetail_grid', 'jqmsgbox'], function($, require, reportgrid, detailgrid) {
    var c18n, languageChooserDialog, transReport, util, viewDetail;
    c18n = require('i18n!nls/common');
    util = require('util');
    c18n = require('i18n!nls/common');
    languageChooserDialog = $("<div title='Study' id='languageChooser'>").dialog({
      autoOpen: false,
      position: [23, 126],
      height: 'auto',
      width: 'auto',
      show: {
        effect: 'slide',
        direction: "up"
      },
      create: function() {
        var _this = this;
        return $.getJSON('rest/languages?prop=id,name', {}, function(languages) {
          return $(_this).append(util.generateLanguageTable(languages));
        });
      },
      buttons: [
        {
          text: c18n.ok,
          click: function() {
            console.log(($(":checkbox[name='languages']", this).map(function() {
              if (this.checked) {
                return {
                  id: this.id,
                  name: this.value
                };
              }
            })).get());
            return $(this).dialog("close");
          }
        }, {
          text: c18n.cancel,
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    transReport = $('#translationReportDialog').dialog({
      autoOpen: false,
      width: 'auto',
      height: 'auto',
      open: function() {
        var param;
        param = $(this).data('param');
        return $.ajax('rest/languages', {
          async: false,
          dataType: 'json',
          data: {
            task: param.id,
            prop: 'id,name'
          },
          success: function(languages) {
            return reportgrid.regenerateGrid({
              id: param.id,
              languages: languages
            });
          }
        });
      },
      buttons: [
        {
          text: 'Import',
          click: function() {
            var param;
            param = $(this).data('param');
            $.post('/task/apply-task', {
              id: param.id
            }, function(json) {
              if (json.status !== 0) {
                return $.msgBox(json.message, null, {
                  title: c18n.error
                });
              }
            });
            return $(this).dialog("close");
          }
        }, {
          text: 'Cancel',
          click: function() {
            return $(this).dialog("close");
          }
        }
      ]
    });
    $('#langChooser').button({}).click(function() {
      return languageChooserDialog.dialog('open');
    });
    viewDetail = $('#translationDetailDialog').dialog({
      autoOpen: false,
      width: 'auto',
      height: 'auto',
      open: function() {
        var param, postData;
        param = $(this).data('param');
        postData = $.extend(param, {
          format: 'grid',
          prop: 'labelKey,maxLength,text.context.name,text.reference,newTranslation'
        });
        return detailgrid.setGridParam({
          url: 'rest/task/details',
          postData: postData
        }).trigger('reloadGrid');
      }
    });
    return {
      transReport: transReport,
      viewDetail: viewDetail
    };
  });

}).call(this);
