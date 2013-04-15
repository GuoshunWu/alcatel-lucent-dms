define [
  'require'
  'jqueryui'

  'i18n!nls/admin'
  'i18n!nls/common'

  'dms-urls'

  'admin/languagegrid'
  'admin/charsetgrid'
  'admin/usergrid'

], (require, $, i18n, c18n, urls)->
  init = ()->
    #    console?.log "transmng panel init..."
    $('#adminTabs').tabs(
      show: (event, ui)->
        pheight = $(ui.panel).height()
        pwidth = $(ui.panel).width()
        #        console?.log "height=#{pheight}, width=#{pwidth}."
        $('table.ui-jqgrid-btable', ui.panel).setGridHeight(pheight - 90).setGridWidth(pwidth - 20)
    )

    tabs = $('#adminTabs')
    pheight = tabs.parent().height()
    tabs.tabs 'option', 'pheight', pheight

    $('div.ui-tabs-panel', tabs).height(pheight - 50)
    # console?.log "language grid height=#{$('#languageGrid').getGridParam('height')}."

    # init dialogs
    $('#addUserDialog').dialog(
      autoOpen: false
      modal: true
      width: 550, height: 275
      create: ()->
        $('select#role', @).append $.map(i18n.usergrid.roleeditoptions.split(';'),(entry, index)->
          tokens=entry.split(':')
          "<option value='#{tokens[0]}'>#{tokens[1]}</option>"
        ).join('')
        me = @
        $('input#loginName', @).on 'blur', ()->
          loginNameInput=@
          loginName =@value
          if(!loginName)
            $('#errMsg', me).html("<br/><hr/>#{c18n.required.format(@name.bold())}")
            return

          $('#errMsg', me).empty()
          $.ajax(url: "#{urls.ldapuser}/#{loginName}", dataType:'text', async: false, success: (json, textStatus, jqXHR)->
            if(!json)
              $('#errMsg', me).html("<br/><hr/>#{i18n.usernotfound.format(loginNameInput.name.bold(), loginName)}")
              $('input#name', me).val('')
              $('input#email', me).val('')
              return

            json = $.parseJSON(json)
            $('input#name', me).val(json.name)
            $('input#email', me).val(json.email)
          )

      open: ()->
        $('input#loginName', @).val('')
        $('input#name', @).val('')
        $('input#email', @).val('')

        $('#errMsg', @).empty()

      buttons: [
        {text: c18n.add, click: ()->
          data = $('#addUserForm', @).serializeArray()
          # add the user
          $("#userGrid").trigger 'reloadGrid'
          $(@).dialog 'close'
        }
        {
        text: c18n.cancel, click: ()->$(@).dialog 'close'
        }
      ]
    )

  ready = ()->
    #    console?.log "transmng panel ready..."
  init()
  ready()





