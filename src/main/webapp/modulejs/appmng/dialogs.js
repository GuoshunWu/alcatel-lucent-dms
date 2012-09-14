/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-8
 * Time: 上午11:18
 * Defines a module.
 * @param {string=} id The module id.
 * @param {Array.|string=} deps The module dependencies.
 * @param {function()|Object} factory The module factory function.
 */
define(['jquery', 'jqueryui'], function ($, jQUI) {
//    private  variable
    var info = {
        name:'appmng_dialogs.',
        version:'1.0'
    };


    $("#testDialog").dialog({
        autoOpen:false,
        create:function () {
//            $($(".ui-button-text", ".ui-dialog-buttonset")[0]).html('OK');
//            $($(".ui-button-text", ".ui-dialog-buttonset")[1]).html('Cancel');
//            $($(".ui-button-text", ".ui-dialog-buttonset")[2]).html('Third');
        },
        buttons:{
            'OK':function () {
                alert("Hello");
            }
        }
    });


//    $('span, .ui-button-text').text('HelloWorld');

    $("#newProduct").button().click(function () {
        $("#testDialog").dialog("open");
    });

    var new_product = $('#newProductDialog').dialog({
        autoOpen:true,
        height:200,
        width:400,
        modal:true,
        buttons:{
            'OK':function () {
                var url = 'app/create-product';
                var productName = {'name':$('#productName').val()};
                $.post(url, productName, function (data) {
                    if (data.status != 0) {
                        alert(data.message);
                        return;
                    }
                    //create success.
                    //todo mytest:
//                    $("#appTree").jstree("create_node",-1, "last", {data:productName.name, attr:{id:data.id}});
                    $("#appTree").jstree("refresh", -1);
                });

                $(this).dialog("close");
            },
            'Cancel':function () {
                $(this).dialog("close");
            }
        },
        close:function () {
            //alert('Are you sure?');
        }
    });

//    var new_product_release = $('#newProductReleaseDialog').dialog({
//        autoOpen:false,
//        height:200,
//        width:500,
//        modal:true,
//        buttons:{
//            'OK':function () {
//                //根据选定的Version复制操作
//                var url = 'app/create-product-release';
//                var versionName = $('#versionName').val();
//                var dupVersionId = $("#dupVersion").val();
//
//                $.post(url, {version:versionName, dupVersionId:dupVersionId, id:pageStatus.selectedProductBase.id}, function (data, textStatus, jqXHR) {
//                    if (data.status != 0) {
//                        alert(data.message);
//                        return;
//                    }
//                    var newOption = new Option(versionName, data.id);
//                    newOption.selected = true;
//                    $("#selVersion")[0].options.add(newOption);
//                    pageStatus.selectedProduct = {version:newOption.text, id:newOption.value};
//                    $("#selVersion").trigger("change");
//                });
//
//                $(this).dialog("close");
//            },
//            'Cancel':function () {
//                $(this).dialog("close");
//            }
//        }
//    });
//
//    var new_or_add_app = $('#newOrAddApplicationDialog').dialog({
//        autoOpen:false,
//        height:200,
//        width:400,
//        modal:true,
//        position:"center",
//        show:{ effect:'drop', direction:"up" },
//        create:function (event, ui) {
//            var input = $('<input>').insertAfter($('#applicationName')).hide();
//            $('#applicationName').data('myinput', input);
//            input = $('<input>').insertAfter($("#version")).hide();
//            $('#version').data('myinput', input);
//
//            $("select", this).css('width', "80px");
//
//            $("#applicationName").change(function (e) {
//                $("#version").empty();
//                $("#version").append(new Option('new', -1));
//
//                var appBaseId = $(this).val();
//                if (-1 == appBaseId) {
//                    $(this).data('myinput').show();
//                    $("#version").trigger("change");
//                    return;
//                }
//                $(this).data('myinput').hide();
//
//                var url = 'rest/applications/apps/' + appBaseId;
//                $.getJSON(url, {}, function (data, textStatus, jqXHR) {
//                    $(data).map(
//                        function () {
//                            return  new Option(this.version, this.id);
//                        }).appendTo($("#version"));
//                    $("#version").trigger("change");
//                });
//            });
//
//            $("#version").change(function (e) {
//                var appId = $(this).val();
//                if (-1 == appId) {
//                    $(this).data('myinput').show();
//                    return;
//                }
//                $(this).data('myinput').hide();
//            });
//        },
//        open:function (event, ui) {
//            //update applicationBases in new ApplicationDialog Application Select according to the product id
//
//            var productId = $("#selVersion").val();
//            var url = 'rest/applications/base/' + productId;
//            $.getJSON(url, {}, function (data, textStatus, jqXHR) {
//                var appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").get(0).options;
//                appBasesOptions.length = 0;
//                appBasesOptions.add(new Option('new', -1));
//
//                $(data).each(function (index, applicationBase) {
//                    appBasesOptions.add(new Option(applicationBase.name, applicationBase.id));
//                });
//
//                $('#applicationName').trigger('change');
//
//            });
//
//
//        },
//        buttons:{
//            'OK':function () {
//                var url = 'app/create-or-add-application';
//                var params = {
//                    productId:$("#selVersion").val(),
//                    appBaseId:$('#applicationName').val(),
//                    appId:$('#version').val(),
//                    appBaseName:$('#applicationName').data('myinput').val(),
//                    appVersion:$('#version').data('myinput').val()
//                };
//
//                $.post(url, params, function (data, textStatus, jqXHR) {
//                    if (data.status != 0) {
//                        $.msgBox(data.message, null, {
//                            width:'auto',
//                            height:'auto'
//                        });
//                        return false;
//                    }
//                    //create success
//
//                    if (-1 == params.appBaseId) {
//                        // create new applicationBase, the appTree need to be updated
//                        var selectedNode = $('#appTree').jstree("get_selected");
//                        $("#appTree").jstree("create_node", selectedNode, "last", {data:params.appBaseName, attr:{id:data.appBaseId}});
//                    }
//                    //trigger table.
//                    $("#applicationGridList").trigger("reloadGrid");
//                });
//                $(this).dialog("close");
//
//            },
//            'Cancel':function () {
//                $(this).dialog("close");
//            }
//        },
//        close:function () {
//            //alert('Are you sure?');
//        }
//    });

    return{

        //privileged method
//        get_info:function () {
//            return JSON.stringify(info);
//        },

        //privileged variable
//        new_product:new_product,
//        new_product_release:new_product_release,
//        new_or_add_app:new_or_add_app
    }
});