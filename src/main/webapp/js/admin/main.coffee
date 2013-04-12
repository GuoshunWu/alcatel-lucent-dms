define [
  'require'
  'jqueryui'

  'i18n!nls/admin'
  'i18n!nls/common'

  'admin/languagegrid'
  'admin/charsetgrid'
  'admin/usergrid'

], (require, $, i18n, c18n)->
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
      width: 500
      create: ()->
        $('select#role',@).append $.map(i18n.usergrid.roleeditoptions.split(';'), (entry, index)->
          tokens=entry.split(':')
          "<option value='#{tokens[0]}'>#{tokens[1]}</option>"
        ).join('')

        $('input#loginName',@).on 'blur', ()->
          $.ajax(url: '', async: false, success: (data, textStatus, jqXHR)->
            $('input#name',@).val()
            $('input#email',@).val()
          )

      open: ()->
        $('input#loginName',@).val('')
        $('#errMsg',@).empty()

      buttons: [
        {text: c18n.add, click: ()->
          errMsg = ''
          data = $('#addUserForm',@).serializeArray()
          $.each data, (index, formItem)->
            if(!(formItem.value))
              errMsg = "<br/><hr/>The #{formItem.name} can't be found in system."
              false
            true

          if(errMsg)
            $('#errMsg',@).html(errMsg)
            return

          # add the user

          $('#userGrid').jqGrid 'trigger', 'reloadGrid'
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





