/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-7
 * Time: 下午4:30
 * To change this template use File | Settings | File Templates.
 */

//==========================================================================
//MessageBox Dialog Library
//using JQuery UI Dialog Library
//Written By: Sazzad Hossain, http://blog.sumon.net, @SazzadHossain
//Version 1.1, Bug fix for confirmLink function to handle INPUT and A types
//==========================================================================
(function ($) {
    $.msgBox = $.fn.msgBox = function (msg, onDialogClosed, customOpt, buttons) {
        var id = "#msgBoxHiddenDiv";
        var div = $(id);

        var opts = $.extend({}, $.fn.msgBox.defaults, customOpt);
        var btns = {};
        $.fn.msgBox.callBack = onDialogClosed;

        if (div.length == 0) {
            div = jQuery("<div id=\"msgBoxHiddenDiv\" title=\"Message\" style=\"font-size: 10pt;display:none\"><p id=\"msgBoxHiddenDivMsg\" style=\"font-size: 10pt; color: black; text-align: center\"></p></div>");
            div.appendTo(document.body);
            var dialogOpt = {
                bgiframe:true,
                autoOpen:false,
                height:opts.height,
                width:opts.width,
                modal:opts.modal,
                resizable:opts.resizable,
                closeOnEscape:opts.closeOnEscape,
                draggable:opts.draggable,
                buttons:$.fn.msgBox.defaultButtons
            };
            div.dialog(dialogOpt);

        }

        $(id + "Msg").html(msg);

        div.dialog("option", "title", opts.title);

        if (buttons != null) {
            for (i = 0; i < buttons.length; i++) {
                btns[buttons[i]] = function (e) {
                    if (onDialogClosed) {
                        onDialogClosed($(e.currentTarget).text());
                    }
                    $(this).dialog('close');
                }
            }
            div.dialog("option", "buttons", btns);
        }
        else {
            div.dialog("option", "buttons", $.fn.msgBox.defaultButtons);
        }

        div.dialog("open");
    };

    $.fn.msgBox.defaults =
    {

        height:'auto',
        width:300,
//        maxWidth:1200,
//        maxHeight:600,
        title:"Message",
        modal:true,
        resizable:true,
        closeOnEscape:true
    };

    $.fn.msgBox.defaultButtons =
    {
        'OK':function () {
            if ($.fn.msgBox.callBack) {
                $.fn.msgBox.callBack(btnName);
            }
            $(this).dialog('close');
        }
    }

    $.fn.msgBox.confirm = function (msg, onConfirm, yesNo) {
        var keys = ["Yes", "No"];
        if (yesNo == false) {
            keys = ["Ok", "Cancel"];
        }
        $.msgBox(msg, function (keyPressed) {
            if (keyPressed == "Ok" || keyPressed == "Yes") {
                onConfirm();
            }
            ;
        }, { title:"Confirm" }, keys);
    }

    $.fn.extend({

        //pass the options variable to the function
        confirmLink:function (options) {


            //Set the default values, use comma to separate the settings, example:
            var defaults = {
                msgFromRel:true,
                defaultMsg:'Are you sure?'
            }

            var options = $.extend(defaults, options);

            return this.each(function () {

                var o = options;
                var msg = o.defaultMsg;
                var obj = $(this);

                if (o.msgFromRel) {
                    if (obj.attr("rel") != "") {
                        msg = obj.attr("rel");
                    }
                }

                obj.click(function (event) {

                    event.preventDefault();
                    $.msgBox.confirm(msg, function () {
                        if (event.target.tagName == "INPUT") {
                            $(event.target).closest("form").submit();
                        } else if (event.target.tagName == "A") {
                            window.location.href = $(event.target).attr("href");
                        }
                    });

                });

            });
        }
    });

})(jQuery);