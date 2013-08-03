import groovy.json.JsonBuilder
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

def replaceMap = ["CreateProductApp": "Create Product/Application"]

response.contentType = 'text/json'
//System.out.println "${request.scheme}://${request.serverName}:${request.serverPort}${request.contextPath}/manual"
def list = FileUtils.listFiles(new File(context.getRealPath("/manual")), ["swf"] as String[], true);
CollectionUtils.transform(list, { File input ->
    String fileName = FilenameUtils.getBaseName(input.name)
    String title = null != replaceMap[fileName] ? replaceMap[fileName] : fileName.replaceAll("([A-Z])", " \$1").trim()
    [filename: fileName, title: title]
} as Transformer);
println new JsonBuilder(list).toPrettyString()

