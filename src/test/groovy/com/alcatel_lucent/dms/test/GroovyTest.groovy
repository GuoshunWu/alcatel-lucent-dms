package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.model.Product

import groovy.sql.Sql
import javax.ws.rs.core.UriBuilder
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.core.PackagesResourceConfig
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory
import org.glassfish.grizzly.http.server.HttpServer

class GroovyTest {

    static void main(args) {
//		sqlValidate()
//        fileEncoding()
//        testJersey()
        switchModelClass('groovy')
    }

    static void testJersey() {
        final URI BASE_URI = UriBuilder.fromUri('http://localhost/').port(9998).build()
//        start server
        println 'Starting grizzly...'
        ResourceConfig rc = new PackagesResourceConfig('com.alcatel_lucent.dms.test')
        HttpServer httpServer=GrizzlyServerFactory.createHttpServer(BASE_URI, rc)

        println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...",
                BASE_URI, BASE_URI));

        System.in.read()
        httpServer.stop()
    }


    static void sqlValidate() {
        String dbUrl = 'jdbc:h2:tcp://localhost/mem:dms;MODE=MYSQL;INIT=create schema if not exists dms;SCHEMA=dms;DB_CLOSE_DELAY=-1'
//        DBUrl='jdbc:MySQL://localhost:3306/dms'
        String user = 'dms'
        String password = 'alcatel123'
        String driverClass = 'org.h2.Driver'
//        driverClass='com.mysql.jdbc.Driver'
        Sql sql = Sql.newInstance(dbUrl, user, password, driverClass)
        String querySQL = '''SELECT distinct code FROM DMS.ALCATEL_LANGUAGE_CODE
                        union
                        SELECT distinct code FROM DMS.ISO_LANGUAGE_CODE''';
//        querySQL=''''''

        List<String> langCodes = []
        sql.eachRow(querySQL) {row ->
            langCodes << row.code
        }

        List<String> fileLangCodes = [
                'ca-ES', 'cs-CZ', 'da-DK', 'de-AT', 'de-CH', 'de-DE', 'el-GR', 'en-AU', 'en-CA', 'en-CN',
                'en-GB', 'en-GR', 'en-MA', 'en-RU', 'en-TW', 'en-US', 'es-AR', 'es-ES', 'es-MX', 'et-EE',
                'fi-FI', 'fr-CA', 'fr-CH', 'fr-FR', 'fr-MA', 'hr-HR', 'hu-HU', 'it-CH', 'it-IT', 'ja-JP',
                'ko-KR', 'lt-LT', 'lv-LV', 'nl-BE', 'nl-NL', 'no-NO', 'pl-PL', 'pt-BR', 'pt-PT', 'ro-RO',
                'ru-RU', 'sk-SK', 'sl-SI', 'sr-CS', 'sv-SE', 'tr-TR', 'zh-CN', 'zh-TW']
        langCodes.sort()
        fileLangCodes.sort()
        println fileLangCodes - langCodes
        println langCodes - fileLangCodes
    }

    static void fileEncoding() {
        String outputFile = 'target/testEncoding.java'
        String encoding = 'UTF-8'
        encoding = 'UTF-16LE'
        PrintStream fw = new PrintStream(new FileOutputStream(outputFile), true, encoding)

        byte[] bom = [-1, -2]    //FF FE, java的byte用的是补码, 验证: b=127, b+=1, 而b=-128

        fw.write bom
        fw.println('Hello，这测试！')
        fw.println('Why, 下一行！')
        fw.close()
        println 'Done...'
    }

//	@CompileStatic
    static void switchModelClass(String to) {
        //restoreTo
        println "restore $to model classes..."
        File dir=new File("src/main/$to/com/alcatel_lucent/dms/model")
        dir.eachFile {file->
            if(file.name.endsWith('.bak')){
                file.renameTo(file.absolutePath.replace('.bak',''))
            }
        }
        
        //switchBackup
        String backUp=(to=='groovy')?'java':'groovy'
             //switchToJava
        println "backup $backUp model classes..."

        dir=new File("src/main/$backUp/com/alcatel_lucent/dms/model")
        dir.eachFile {file->
            if(!file.name.endsWith('.bak')){
                file.renameTo "${file.absolutePath}.bak"
            }
        }
    }
}
