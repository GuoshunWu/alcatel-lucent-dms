<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

var Text = {

<s:i18n name="js">
<s:iterator value="keys" id="key">
	<s:property value="key"/>: "<s:property value="getText(#key)"/>",
</s:iterator>
	"": ""
</s:i18n>
};

//alert(Text.select_product);
