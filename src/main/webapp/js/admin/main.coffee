define [
  'require'
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/admin'
  'i18n!nls/common'

  'dms-urls'
  'dms-util'

  './languagegrid'
  './charsetgrid'
  './usergrid'
  './glossarygrid'

], (require, $, blockui,jqmsgbox, i18n, c18n, urls, util)->
  init = ()->
    #    console?.log "transmng panel init..."
    isFirst = true
    $('#adminTabs').tabs(
      activate: (event, ui)->
        pheight = $(ui.newPanel).height()
        pwidth = $(ui.newPanel).width()
#        console?.log "height=#{pheight}, width=#{pwidth}."
        if isFirst
          $('table.ui-jqgrid-btable', @).setGridHeight(pheight - 90).setGridWidth(pwidth - 20)
          $('#glossaryGrid',@).setGridHeight(pheight - 120)
        isFirst = false
    )

    tabs = $('#adminTabs')
    pheight = tabs.parent().height()
    tabs.tabs 'option', 'pheight', pheight

    $('div.ui-tabs-panel', tabs).height(pheight - 50)
    # console?.log "language grid height=#{$('#languageGrid').getGridParam('height')}."

    $('#buildLuceneIndex').button().click (e)->
      pb = util.genProgressBar()
      util.updateProgress(urls.config.create_index, {}, (json)->
        pb.parent().remove()
        msg = json.event.msg
        $.msgBox msg, null, {title: c18n.info, width: 300, height: 'auto'}
      , pb)

    $('#consistentGlossaries').button().click (e)->
      $.blockUI(message: '')
      pb = util.genProgressBar()
      util.updateProgress(urls.glossary.apply, {oper: 'consistentGlossaries'}, (json)->
        $.unblockUI()
        pb.parent().remove()
        msg = json.event.msg
        $.msgBox msg, null, {title: c18n.info, width: 300, height: 'auto'}
      , pb)

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
              me.isValid = false
              return

            json = $.parseJSON(json)
            $('input#name', me).val(json.name)
            $('input#email', me).val(json.email)

            me.isValid=true
          )

      open: ()->
        $('input#loginName', @).val('')
        $('input#name', @).val('')
        $('input#email', @).val('')

        $('#errMsg', @).empty()

      buttons: [
        {text: c18n.add, click: ()->
          return if(!@isValid)
          postData =
            oper: 'add'
            loginName: $('input#loginName', @).val()
            name: $('input#name', @).val()
            email: $('input#email', @).val()
            userStatus: Number(Boolean($('input#enabled', @).attr('checked')))
            role: $('select#role', @).val()

          console?.log postData

          $.post urls.user.update, postData, (json)->
            if(json.status !=0 )
              $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
              return
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





