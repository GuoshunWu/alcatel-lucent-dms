<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="DMS_applicationPanel">

    <table border="0">
        <tr>
            <td><s:text name="product"/>&nbsp;
                <span id="appDispProductName"></span></td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width: 70px"><s:text name="application"/></td>
                        <td style="width: 70px"><span id="appDispAppName"></span></td>
                        <td><s:text name="version"/><select id="selAppVersion"></select></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <style>
                            #uploadSpan {
                                font-size: 12px;
                                overflow: hidden;
                                position: absolute;
                            }

                                /*#file{ position:static; z-index:100; margin-left:-180px; font-size:60px;opacity:0;filter:alpha(opacity=100); margin-top:-5px;}*/
                            #dctFileUpload {
                                position: absolute;
                                z-index: 100;
                                font-size: 60px;
                                margin-left: -10px;
                                margin-top: -5px;
                                opacity: 0;
                                filter: alpha(opacity = 0);
                            }

                        </style>
                        <%--<td style="width: 120px"><label for="dctFileUpload"></label>--%>
                        <%--</td>--%>
                        <td style="width: 120px;">
                            <label for="dctFileUpload"><s:text name="appmng.deliverapp"/></label>
                        </td>
                        <td style="vertical-align: text-top;padding-top: 6px">
                            <span id="uploadSpan">
                                <input id="dctFileUpload" type="file" name="upload"/>
                                <span id="uploadStatus">Choose file...</span>
                            </span>
                        </td>
                        <td style="width:60%">
                            <div id="progressbar"></div>
                        </td>
                    </tr>
                </table>

                <!-- The file upload form used as target for the file upload widget -->
                <%--<form id="fileupload" action="app/deliver-app-dict" method="POST" enctype="multipart/form-data">--%>
                <%--<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->--%>
                <%--<div class="row fileupload-buttonbar">--%>
                <%--<div class="span7">--%>
                <%--<!-- The fileinput-button span is used to style the file input field as button -->--%>
                <%--<span class="btn btn-success fileinput-button">--%>
                <%--<i class="icon-plus icon-white"></i>--%>
                <%--<span>Add files...</span>--%>
                <%--<input type="file" name="files[]" multiple>--%>
                <%--</span>--%>
                <%--<button type="submit" class="btn btn-primary start">--%>
                <%--<i class="icon-upload icon-white"></i>--%>
                <%--<span>Start upload</span>--%>
                <%--</button>--%>
                <%--<button type="reset" class="btn btn-warning cancel">--%>
                <%--<i class="icon-ban-circle icon-white"></i>--%>
                <%--<span>Cancel upload</span>--%>
                <%--</button>--%>
                <%--<button type="button" class="btn btn-danger delete">--%>
                <%--<i class="icon-trash icon-white"></i>--%>
                <%--<span>Delete</span>--%>
                <%--</button>--%>
                <%--<input type="checkbox" class="toggle">--%>
                <%--</div>--%>
                <%--<!-- The global progress information -->--%>
                <%--<div class="span5 fileupload-progress fade">--%>
                <%--<!-- The global progress bar -->--%>
                <%--<div class="progress progress-success progress-striped active" role="progressbar"--%>
                <%--aria-valuemin="0" aria-valuemax="100">--%>
                <%--<div class="bar" style="width:0%;"></div>--%>
                <%--</div>--%>
                <%--<!-- The extended global progress information -->--%>
                <%--<div class="progress-extended">&nbsp;</div>--%>
                <%--</div>--%>
                <%--</div>--%>
                <%--<!-- The loading indicator is shown during file processing -->--%>
                <%--<div class="fileupload-loading"></div>--%>
                <%--<br>--%>
                <%--<!-- The table listing the files available for upload/download -->--%>
                <%--<table role="presentation" class="table table-striped">--%>
                <%--<tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody>--%>
                <%--</table>--%>
                <%--</form>--%>
            </td>
        </tr>
        <tr>
            <td>
                <div id="dictionaryGrid">
                    <table id="dictionaryGridList">
                        <tr>
                            <td/>
                        </tr>
                    </table>
                    <div id="dictPager"></div>
                </div>
            </td>
        </tr>

    </table>
</div>
