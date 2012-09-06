/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-3
 * Time: 上午10:11
 */
var pageLayout;


$(function () {

    pageLayout = $('#optional-container').layout({
        resizable:true, closable:true
//           ,spacing_open: 0
//           ,north__paneSelector:	"#page_header"	// page header
    });

    var pageStatus = {};

    var dmsPanels = $('#ui_center').children("div[id^='DMS']");
    dmsPanels.addClass("ui-layout-content ui-corner-bottom"); //ui-widget-content
    dmsPanels.css({paddingBottom:'1em', borderTop:0});
    pageStatus.selectedPanel = showCenterPanel('DMS_welcomePanel');

    function showCenterPanel(panelId) {
        dmsPanels.each(function (index, element) {
            if ($(this).attr('id') == panelId) {
                pageStatus.selectedPanel = $(this).show();
            } else {
                $(this).hide();
            }
        });
        return pageStatus.selectedPanel;
    }

    $(".header-footer").hover(
        function () {
            $(this).addClass('ui-state-hover');
        }
        , function () {
            $(this).removeClass('ui-state-hover');
        }
    );

//
    //load init treeData from this url
//            var url= 'json/tree.json';
    var url = 'rest/products';
    url += '?nocache=' + new Date().getTime();

    $.getJSON(url, {}, function (data, textStatus, jqXHR) {
        //data is the tree initialize data from server
        //appTree
        $('#appTree').jstree({
            json_data:{
                data:data
            },
            plugins:[ "themes", "json_data", "ui", "core"]
        }).bind("select_node.jstree", function (event, data) {
                var appTree = $.jstree._reference("#appTree");
                var text = appTree.get_text(data.rslt.obj);
                var parent = appTree._get_parent(data.rslt.obj);

                var id = data.rslt.obj.attr("id");
                pageStatus.selectedProductBase = {id:id, name:text};
                if (-1 == parent) { //it is a Product
                    pageStatus.selectedPanel = showCenterPanel('DMS_productPanel');
                    $('#dispProductName', pageStatus.selectedPanel).text(text);

                    // query product version base on Product and its Id
                    $.getJSON('rest/products/' + id, {}, function (data, textStatus, jqXHR) {
                        var productVersions = $("#selVersion").get(0).options;
                        productVersions.length = 0;
                        $(data).each(function (index, product) {
                            productVersions.add(new Option(product.version, product.id));
                        });
                        pageStatus.selectedProduct = {id:productVersions[0].value, version:productVersions[0].text};

                        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

                        var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
                        jQuery("#applicationGridList").jqGrid('setCaption', newCaption);

                    });
                } else { //it is a Application
                    pageStatus.selectedPanel = showCenterPanel('DMS_applicationPanel');
                    //TODO: Initialize Application panel elements
                }
            });
    });

    // jqGrid
    $('#applicationGridList').jqGrid({
        url:'json/appgrid.json',
        datatype:'json',
        width:700,
        height:350,
        colNames:['ID', 'Application', 'Version', 'Dict. Num.'],
        colModel:[
            {name:'id', index:'id', width:55, align:'center', hidden:true},
            {name:'name', index:'name', width:100, editable:true, align:'center'},
            {name:'version', index:'version', width:90, editable:true, align:'center'},
            {name:'dictNum', index:'dictNum', width:80, align:'center'}
        ],
        pager:'#pager',
        editurl:"app/create-or-add-application",
        rowNum:10,
        rowList:[10, 20, 30],
        sortname:'name',
        sortorder:'asc',
        viewrecords:true,
        gridview:true,
        caption:'Applications for Product'
    });
    jQuery("#applicationGridList").jqGrid('navGrid', '#pager', {edit:false, add:false, del:false, search:false, view:false})
        .navButtonAdd('#pager', {
            caption:"Del",
            buttonicon:"ui-icon-trash",
            onClickButton:function () {
                alert("Deleting Row");
            },
            position:"first"
        }).navButtonAdd('#pager', {
            caption:"View",
            buttonicon:"ui-icon-document",
            onClickButton:function () {
                alert("View");
            },
            position:"first"
        }).navButtonAdd('#pager', {
            caption:"Edit",
            buttonicon:"ui-icon-pencil",
            onClickButton:function () {
                alert("Edit Row");
            },
            position:"first"
        }).navButtonAdd("#pager", {

            caption:"Add",
            buttonicon:"ui-icon-plus",
            onClickButton:function () {
                var width = jQuery("#applicationGridList").jqGrid('getGridParam', "width");
                var height = jQuery("#applicationGridList").jqGrid('getGridParam', "height");

                $("#newOrAddApplicationDialog").dialog("open");
//                jQuery("#applicationGridList").jqGrid('editGridRow',"new",{height:280,reloadAfterSubmit:false});
            },
            position:"first"
        }
    );

    $('#newOrAddApplicationDialog').dialog({
        autoOpen:false,
        height:200,
        width:400,
        modal:true,
        position:"center",
        show:{ effect:'drop', direction:"up" },
        create:function (event, ui) {
            var input = $('<input>').insertAfter($('#applicationName')).hide();
            $('#applicationName').data('myinput', input);
            input = $('<input>').insertAfter($("#version")).hide();
            $('#version').data('myinput', input);

            $("select", this).css('width', "80px");

            $("#applicationName").change(function (e) {

                var versionOptions = $("#version").get(0).options;
                versionOptions.length=0;
                versionOptions.add(new Option('new', -1));

                var appBaseId = $(this).val();
                if (-1 == appBaseId) {
                    $(this).data('myinput').show();
                    $("#version").trigger("change");
                    return;
                }
                $(this).data('myinput').hide();

                url = 'rest/applications/apps/' + appBaseId;
                $.getJSON(url, {}, function (data, textStatus, jqXHR) {
                    $(data).each(function (index, application) {
                        versionOptions.add(new Option(application.version, application.id));
                    });
                    $("#version").trigger("change");
                });
            });

            $("#version").change(function (e) {
                var appId = $(this).val();
                if (-1 == appId) {
                    $(this).data('myinput').show();
                    return;
                }
                $(this).data('myinput').hide();
            });
        },
        open:function (event, ui) {
            //update applicationBases in new ApplicationDialog Application Select according to the product id

            var productId = $("#selVersion").val();
            url = 'rest/applications/base/' + productId;
            $.getJSON(url, {}, function (data, textStatus, jqXHR) {
                var appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").get(0).options;
                appBasesOptions.length = 0;
                appBasesOptions.add(new Option('new', -1));

                $(data).each(function (index, applicationBase) {
                    appBasesOptions.add(new Option(applicationBase.name, applicationBase.id));
                });

                $('#applicationName').trigger('change');

            });


        },
        buttons:{
            'OK':function () {
                var url = 'app/create-or-add-application';
                var data = {
                    productId: $("#selVersion").val(),
                    appBaseId: $('#applicationName').val(),
                    appId: $('#version').val(),
                    appBaseName:$('#applicationName').data('myinput').val(),
                    appVersion:$('#version').data('myinput').val()
                };

                $.post(url, data, function (data, textStatus, jqXHR) {
                    if (data.status != 0) {
                        alert(data.message);
                        return;
                    }
                    //create success.
                    //trigger table.
                    $("#applicationGridList").trigger("reloadGrid");
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

//      create all the dialogs
    $('#newProductDialog').dialog({
        autoOpen:false,
        height:200,
        width:400,
        modal:true,
        buttons:{
            'OK':function () {
                url = 'app/create-product';
                var productName = {'name':$('#productName').val()};
                $.post(url, productName, function (data, textStatus, jqXHR) {
                    if (data.status != 0) {
                        alert(data.message);
                        return;
                    }
                    //create success.
                    // $("#appTree").jstree("create_node", -1, "last", {data: productName.name,attr:{id:data.id}});
                    $.jstree._reference("#appTree").create_node(-1, "last", {data:productName.name, attr:{id:data.id}});
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

    $('#newProductReleaseDialog').dialog({
        autoOpen:false,
        height:200,
        width:500,
        modal:true,
        buttons:{
            'OK':function () {
                //根据选定的Version复制操作
                url = 'app/create-product-release';
                var versionName = $('#versionName').val();
                var dupVersionId = $("#dupVersion").val();

                $.post(url, {version:versionName, dupVersionId:dupVersionId, id:pageStatus.selectedProductBase.id}, function (data, textStatus, jqXHR) {
                    if (data.status != 0) {
                        alert(data.message);
                        return;
                    }
                    var newOption = new Option(versionName, data.id);
                    newOption.selected = true;
                    $("#selVersion")[0].options.add(newOption);
                    pageStatus.selectedProduct = {version:newOption.text, id:newOption.value};
                    $("#selVersion").trigger("change");
                });

                $(this).dialog("close");
            },
            'Cancel':function () {
                $(this).dialog("close");
            }
        }
    });

    //  create all the buttons;
    $("#newProduct").button().click(function () {
        $("#newProductDialog").dialog("open");
    });

    $("#newApp").button().click(function () {
        $("<input>").insertAfter($(this));
        alert("to be implemented.");
    });
    $("#addApp").button().click(function () {
        alert("test trigger");
    });
    $("#removeApp").button().click(function () {
        alert("to be implemented.");
    });
    $("#download").button().click(function () {
        alert("to be implemented.");
        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"http://localhost:2000/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

    });

    $("#selVersion").change(function (event) {
        pageStatus.selectedProduct = {version:$(this).find("option:selected").text(), id:$(this).val()};
//        log(pageStatus);
        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");
        var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
        jQuery("#applicationGridList").jqGrid('setCaption', newCaption);
    });

    $("#newVersion").button({
        text:false,
        icons:{
            primary:"ui-icon-plus"
        }
    }).click(function () {
            $("#newProductReleaseDialog").dialog("open");
            var productVersions = $("#selVersion")[0].options;
//            log("productVersions length="+productVersions.length+", productVersion="+productVersions);
            var newVersions = $("#dupVersion")[0].options;

            newVersions.length = 0;
            newVersions.add(new Option("", -1));
            var opt, newOpt;
            for (var i = 0; i < productVersions.length; ++i) {
                opt = productVersions[i];
                newOpt = new Option(opt.text, opt.value);
                if (i == productVersions.length - 1) {
                    newOpt.selected = true;
                }
                newVersions.add(newOpt);
            }
        });

//    $("#applicationName").combobox({
//        selected:function (e, data) {
//            var appBaseId = data.value;
//            url = 'rest/applications/apps/' + appBaseId;
//            $.getJSON(url, {}, function (data, textStatus, jqXHR) {
//                var version = $("#newOrAddApplicationDialog").find("#version");
//                var appBasesOptions = version.get(0).options;
//                appBasesOptions.length = 0;
//                $(data).each(function (index, application) {
//                    var opt = new Option(application.version, application.id);
//                    appBasesOptions.add(opt);
//                });
//            });
//        }
//    });
//    $("#version").combobox({
//        selected:function (e, data) {
////            alert('Hi, I am selected: '+JSON.stringify(data));
//        }
//    });


});

