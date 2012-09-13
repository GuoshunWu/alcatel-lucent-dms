/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-3
 * Time: 上午10:11
 */

$(function () {
    var pageLayout = $('#optional-container').layout({
        resizable:true, closable:true
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
        $.jstree._themes = "css/jstree/themes/";
        $('#appTree').jstree({
            json_data:{
                data:data
            },
            core:{
//                animation:500
            },
            ui:{
                select_limit:1
            },
            themes:{
//                theme:'default-rtl'
//                theme:'apple'
//                theme:'classic'
            },
            plugins:[ "themes", "json_data", "ui", "core"]
        }).bind("select_node.jstree", function (event, data) {
                var appTree = $.jstree._reference("#appTree");
                var text = appTree.get_text(data.rslt.obj);
                var parent = appTree._get_parent(data.rslt.obj);
                var id = data.rslt.obj.attr("id");

                if (-1 == parent) { //it is a Product
                    pageStatus.selectedPanel = showCenterPanel('DMS_productPanel');
                    pageStatus.selectedProductBase = {id:id, name:text};
                    $('#dispProductName', pageStatus.selectedPanel).text(text);

                    // query product version
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
                    pageStatus.selectedProductBase = {id:parent.attr("id"), name:appTree.get_text(parent)};
                    //TODO: Initialize Application panel elements
                    $('#appDispProductName', pageStatus.selectedPanel).text(pageStatus.selectedProductBase.name);
                    $('#appDispAppName', pageStatus.selectedPanel).text(text);

                    // query application version
                    var url = 'rest/applications/apps/' + id;
                    $.getJSON(url, {}, function (data, textStatus, jqXHR) {
                        var productVersions = $("#selAppVersion").get(0).options;
                        productVersions.length = 0;
                        $(data).each(function (index, product) {
                            productVersions.add(new Option(product.version, product.id));
                        });
                        // update dictGridList
//                        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");
//
//                        var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
//                        jQuery("#applicationGridList").jqGrid('setCaption', newCaption);

                    });
                }
            });

    });

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
                $("#version").empty();
                $("#version").append(new Option('new', -1));

                var appBaseId = $(this).val();
                if (-1 == appBaseId) {
                    $(this).data('myinput').show();
                    $("#version").trigger("change");
                    return;
                }
                $(this).data('myinput').hide();

                url = 'rest/applications/apps/' + appBaseId;
                $.getJSON(url, {}, function (data, textStatus, jqXHR) {
                    $(data).map(
                        function () {
                            return  new Option(this.version, this.id);
                        }).appendTo($("#version"));
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
            $.getJSON(url, {}, function (data) {
                var appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").empty().append(new Option('new', -1));

                $(data).map(
                    function () {
                        return new Option(this.name, this.id);
                    }).appendTo(appBasesOptions);

                $('#applicationName').trigger('change');

            });


        },
        buttons:{
            'OK':function () {
                var url = 'app/create-or-add-application';
                var params = {
                    productId:$("#selVersion").val(),
                    appBaseId:$('#applicationName').val(),
                    appId:$('#version').val(),
                    appBaseName:$('#applicationName').data('myinput').val(),
                    appVersion:$('#version').data('myinput').val()
                };

                $.post(url, params, function (data, textStatus, jqXHR) {
                    if (data.status != 0) {
                        $.msgBox(data.message, null, {
                            width:'auto',
                            height:'auto'
                        });
                        return false;
                    }
                    //create success

                    if (-1 == params.appBaseId) {
                        // create new applicationBase, the appTree need to be updated
                        var selectedNode = $('#appTree').jstree("get_selected");
                        $("#appTree").jstree("create_node", selectedNode, "last", {data:params.appBaseName, attr:{id:data.appBaseId}});
                    }
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
                    //todo mytest:
                    $("#appTree").jstree("create_node", -1, "last", {data:productName.name, attr:{id:data.id}});
//                    $("#appTree").jstree("refresh", -1);
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

    $("#dictUpload").button().click(function (e) {
        alert("to be implemented.");
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

    // jqGrid
    $('#applicationGridList').jqGrid({
        url:'json/appgrid.json',
        datatype:'json',
        width:700,
        height:350,
        colNames:['ID', 'Application', 'Version', 'Dict. Num.'],
        colModel:[
            {name:'id', index:'id', width:55, align:'center', editable:false, hidden:true},
            {name:'name', index:'name', width:100, editable:false, align:'center'},
            {name:'version', index:'version', width:90, editable:true, align:'center', edittype:'select', editoptions:{value:{}}},
            {name:'dictNum', index:'dictNum', width:80, editable:false, align:'center'}
        ],
        pager:'#pager',
        editurl:"app/create-or-add-application",
        cellsubmit:'remote',
        cellurl:'app/change-application-version',
        rowNum:10,
        afterEditCell:function (id, name, val, iRow, iCol) {
            if (name = 'version') {
//                $("#" + iRow + "_version", "#applicationGridList").datepicker({dateFormat:"yy-mm-dd"});
                var select = $("#" + iRow + "_version", "#applicationGridList").empty();
                $.getJSON('rest/applications/appssamebase/' + id, {}, function (json) {
                    $(json).map(
                        function () {
                            var opt = new Option(this.version, this.id);
                            opt.selected = (this.version == val);
                            return opt;
                        }).appendTo(select);
                });
            }
        },
        beforeSubmitCell:function (rowid, cellname, value, iRow, iCol) {
            var select = $("#" + iRow + "_version", "#applicationGridList");
//            log("value="+value+",select value="+value);
//            log(select.children(":selected"));
            return {productId:$("#selVersion").val(), newAppId:select.val()};
        },
        afterSubmitCell:function (serverresponse, rowid, cellname, value, iRow, iCol) {
            var jsonFromServer = eval('(' + serverresponse.responseText + ')');
//            log(jsonFromServer);
            return [0 == jsonFromServer.status, jsonFromServer.message];
        },
        viewrecords:true,
        cellEdit:true,
        rowList:[10, 20, 30],
        sortname:'name',
        sortorder:'asc',
        viewrecords:true,
        gridview:true,
        altRows:true,
        ajaxSelectOptions:'json/selecttest.json',
        caption:'Applications for Product'
    }).jqGrid('navGrid', '#pager', {edit:false, add:false, del:false, search:false, view:false})
        .navButtonAdd('#pager', {
            caption:"",
            buttonicon:"ui-icon-trash",
            onClickButton:function () {
                //todo: add delete application business logic
                var gr = jQuery("#applicationGridList").jqGrid('getGridParam', 'selrow');
                if (null == gr) {
                    $.msgBox("Please Select Row to delete!", null, {
                        title:'Select Row',
                        width:300,
                        height:'auto'
                    });
                    return false;
                }


                jQuery("#applicationGridList").jqGrid('delGridRow', gr, {
                    url:'app/remove-application',
                    mtype:'post',
                    editData:[],
                    recreateForm:false,
                    topinfo:"Test for top info",
                    bottominfo:'Test for bottom info',
                    modal:true,
                    jqModal:true,
                    reloadAfterSubmit:false,
                    beforeShowForm:function (form) {
                        var permanent = $('#permanentDeleteSignId', form);
                        if (0 == permanent.length) {
                            $("<tr><td>Delete permanently</td><td><input align='left' type='checkbox' id='permanentDeleteSignId'></td></tr>").appendTo($("tbody", form));
                        } else {
                            permanent.removeAttr("checked");
                        }
                    },
                    onclickSubmit:(function (params, posdata) {
                        return {productId:$("#selVersion").val(), permanent:Boolean(($('#permanentDeleteSignId').attr("checked")))};
                    }),
                    afterSubmit:function (response, postdata) {
                        var jsonFromServer = eval('(' + response.responseText + ')');
                        if (jsonFromServer.id) {//appbase is deleted
                            //remove appbase node from apptree.
                            var appTree = $.jstree._reference("#appTree");
                            appTree._get_children(appTree.get_selected()).each(function (index, app) {
                                if (app.id == jsonFromServer.id) {
                                    appTree.delete_node(app);
                                }
                            });


                        }
                        log(jsonFromServer);
                        return [0 == jsonFromServer.status, jsonFromServer.message];
                    }
                });
            },
            position:"first"
        }).navButtonAdd("#pager", {
            caption:"",
            buttonicon:"ui-icon-plus",
            onClickButton:function () {
                $("#newOrAddApplicationDialog").dialog("open");
            },
            position:"first"
        }
    );


    // dictionary grid.
    $('#dictionaryGridList').jqGrid({
        url:'',
        datatype:'json',
        width:700,
        height:350,
        pager:'#dictPager',
        editurl:"app/create-or-add-application",
        rowNum:10,
        rowList:[10, 20, 30],
        sortname:'name',
        sortorder:'asc',
        viewrecords:true,
        gridview:true,
        caption:'Dictionary for Application',
        colNames:['ID', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action'],
        colModel:[
            {name:'id', index:'id', width:55, align:'center', hidden:true},
            {name:'name', index:'name', width:100, editable:true, align:'center'},
            {name:'version', index:'version', width:90, editable:true, align:'center'},
            {name:'format', index:'format', width:90, editable:true, align:'center'},
            {name:'encoding', index:'encoding', width:90, editable:true, align:'center'},
            {name:'labelNum', index:'labelNum', width:80, align:'center'},
            {name:'action', index:'action', width:90, editable:true, align:'center'}
        ]
    }).jqGrid('navGrid', '#dictPager', {edit:true, add:true, del:true, search:true});
});

