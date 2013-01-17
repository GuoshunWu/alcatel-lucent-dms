// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqueryui', 'taskmng/taskreport_grid', 'taskmng/transdetail_grid', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/taskmng', 'util', 'require'], function($, reportgrid, detailgrid, msgbox, c18n, i18n, util, require) {
    var languageChooserDialog, transReport, viewDetail;
    languageChooserDialog = $("<div title='Study' id='languageChooser'>").dialog({
      autoOpen: false,
      position: [23, 126],
      height: 'auto',
      width: 900,
      modal: true,
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
      modal: true,
      width: $(window).width() * 0.8,
      height: 'auto',
      open: function() {
        var buttons, param;
        param = $(this).data('param');
        buttons = [
          {
            text: c18n.close,
            click: function() {
              return $(this).dialog("close");
            }
          }
        ];
        if (!param.viewReport) {
          buttons.unshift({
            text: c18n["import"],
            click: function() {
              param = $(this).data('param');
              $.blockUI;
              $.post('task/apply-task', {
                id: param.id
              }, function(json) {
                $.unblockUI();
                if (json.status !== 0) {
                  $.msgBox(json.message, null, {
                    title: c18n.error
                  });
                  return;
                }
                return $.msgBox(i18n.task.confirmmsg, (function(keyPressed) {
                  if (c18n.no === keyPressed) {
                    $.blockUI;
                    return $.post('task/close-task', {
                      id: param.id
                    }, function(json) {
                      $.unblockUI();
                      if (json.status !== 0) {
                        $.msgBox(json.message, null, {
                          title: c18n.error
                        });
                        return;
                      }
                      return $("#taskGrid").trigger('reloadGrid');
                    });
                  }
                }), {
                  title: c18n.confirm
                }, [c18n.yes, c18n.no]);
              });
              return $(this).dialog("close");
            }
          });
        }
        $(this).dialog('option', 'buttons', buttons);
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
      }
    });
    $('#langChooser').button({}).click(function() {
      return languageChooserDialog.dialog('open');
    });
    viewDetail = $('#translationDetailDialog').dialog({
      autoOpen: false,
      modal: true,
      width: 850,
      height: 'auto',
      resize: function(event, ui) {
        return $("#viewDetailGrid", this).setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 145, true);
      },
      open: function() {
        var param, postData;
        param = $(this).data('param');
        console.log(param);
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
