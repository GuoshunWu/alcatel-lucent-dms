// Generated by CoffeeScript 1.6.2
(function() {
  define(['require', 'jqueryui', 'blockui', 'jqmsgbox', 'i18n!nls/admin', 'i18n!nls/common', 'dms-urls', 'admin/languagegrid', 'admin/charsetgrid', 'admin/usergrid'], function(require, $, blockui, jqmsgbox, i18n, c18n, urls) {
    var init, ready;

    init = function() {
      var pheight, tabs;

      $('#adminTabs').tabs({
        show: function(event, ui) {
          var pheight, pwidth;

          pheight = $(ui.panel).height();
          pwidth = $(ui.panel).width();
          return $('table.ui-jqgrid-btable', ui.panel).setGridHeight(pheight - 90).setGridWidth(pwidth - 20);
        }
      });
      tabs = $('#adminTabs');
      pheight = tabs.parent().height();
      tabs.tabs('option', 'pheight', pheight);
      $('div.ui-tabs-panel', tabs).height(pheight - 50);
      $('#buildLuceneIndex').button().click(function(e) {
        $.blockUI();
        return $.post(urls.config.create_index, function(json) {
          $.unblockUI();
          if (json.status !== 0) {
            $.msgBox(json.message, null, {
              title: c18n.error,
              width: 300,
              height: 'auto'
            });
            return false;
          }
          return $.msgBox(json.message, null, {
            title: c18n.info,
            width: 300,
            height: 'auto'
          });
        });
      });
      return $('#addUserDialog').dialog({
        autoOpen: false,
        modal: true,
        width: 550,
        height: 275,
        create: function() {
          var me;

          $('select#role', this).append($.map(i18n.usergrid.roleeditoptions.split(';'), function(entry, index) {
            var tokens;

            tokens = entry.split(':');
            return "<option value='" + tokens[0] + "'>" + tokens[1] + "</option>";
          }).join(''));
          me = this;
          return $('input#loginName', this).on('blur', function() {
            var loginName, loginNameInput;

            loginNameInput = this;
            loginName = this.value;
            if (!loginName) {
              $('#errMsg', me).html("<br/><hr/>" + (c18n.required.format(this.name.bold())));
              return;
            }
            $('#errMsg', me).empty();
            return $.ajax({
              url: "" + urls.ldapuser + "/" + loginName,
              dataType: 'text',
              async: false,
              success: function(json, textStatus, jqXHR) {
                if (!json) {
                  $('#errMsg', me).html("<br/><hr/>" + (i18n.usernotfound.format(loginNameInput.name.bold(), loginName)));
                  $('input#name', me).val('');
                  $('input#email', me).val('');
                  me.isValid = false;
                  return;
                }
                json = $.parseJSON(json);
                $('input#name', me).val(json.name);
                $('input#email', me).val(json.email);
                return me.isValid = true;
              }
            });
          });
        },
        open: function() {
          $('input#loginName', this).val('');
          $('input#name', this).val('');
          $('input#email', this).val('');
          return $('#errMsg', this).empty();
        },
        buttons: [
          {
            text: c18n.add,
            click: function() {
              var postData;

              if (!this.isValid) {
                return;
              }
              postData = {
                oper: 'add',
                loginName: $('input#loginName', this).val(),
                name: $('input#name', this).val(),
                email: $('input#email', this).val(),
                userStatus: Number(Boolean($('input#enabled', this).attr('checked'))),
                role: $('select#role', this).val()
              };
              if (typeof console !== "undefined" && console !== null) {
                console.log(postData);
              }
              $.post(urls.user.update, postData, function(json) {
                if (json.status !== 0) {
                  $.msgBox(json.message, null, {
                    title: c18n.error,
                    width: 300,
                    height: 'auto'
                  });
                  return;
                }
                return $("#userGrid").trigger('reloadGrid');
              });
              return $(this).dialog('close');
            }
          }, {
            text: c18n.cancel,
            click: function() {
              return $(this).dialog('close');
            }
          }
        ]
      });
    };
    ready = function() {};
    init();
    return ready();
  });

}).call(this);
