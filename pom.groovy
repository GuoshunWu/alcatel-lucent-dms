project {
  modelVersion '4.0.0'
  groupId 'com.alcatel_lucent.dms'
  artifactId 'dms'
  version '2.15.8'
  packaging 'war'
  name 'Dictionary Management System'
  url 'http://localhost:8080/dms'
  developers {
    developer {
      id 'allany'
      name 'Allan Yang'
      email 'Allany.YANG@alcatel-lucent.com'
      roles {
        role 'architect'
        role 'developer'
      }
      timezone '+8'
    }
    developer {
      id 'guoshunw'
      name 'Guoshun Wu'
      email 'Guoshun.WU@alcatel-sbell.com.cn'
      roles {
        role 'developer'
      }
      timezone '+8'
    }
  }
  issueManagement {
    system 'JIRA'
    url 'http://aww.rdcsbu.bsf.alcatel.fr/tools/dms'
  }
  distributionManagement {
    repository {
      id 'releases'
      name 'Internal Releases'
      url 'file://d:/test/release'
    }
    snapshotRepository {
      id 'snapshots'
      name 'Internal Snapshots'
      url 'file://d:/test/snapshot'
    }
  }
  properties {
    'db.hbm2ddl.auto' 'update'
    'db.password' 'alcatel123'
    'spring.version' '4.0.4.RELEASE'
    'db.urlparam' 'AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS DMS;SCHEMA=DMS;'
    'logger.filter' 'WARN'
    'hibernate.version' '3.6.10.Final'
    'dms.generate.dir' '${dms.temp}/dms/generate'
    'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/h2'
    'dms.temp'
    'project.build.sourceEncoding' 'UTF-8'
    jerseyVersion '1.17.1'
    'maven.build.timestamp.format' 'yyyyMMddHHmmss'
    'db.house-keeping-test-sql' 'select CURRENT_DATE from dual'
    'db.user' 'dms'
    'db.driver' 'org.h2.Driver'
    'ldap.password' 'Pass_123'
    'dms.deliver.dir' '${dms.temp}/dms/deliver'
    'project.reporting.outputEncoding' 'UTF-8'
    'db.url' 'jdbc:h2:~/dms;${db.urlparam}'
    'ldap.bind' 'uid=reader.Web_SHA,dc=Web_SHA,dc=apps,dc=root'
    gmavenVersion '1.5'
    buildNumber '${maven.build.timestamp}'
    'ldap.dirbase' 'dc=internal,dc=users,dc=root'
    'dms.send.dir' '${dms.temp}/dms/send'
    'dms.receive.dir' '${dms.temp}/dms/receive'
    'db.dialect' 'org.hibernate.dialect.H2Dialect'
    'ldap.url2' 'ldaps://ldap-emea.app.alcatel-lucent.com:2793'
    'ldap.url' 'ldap://ldap-emea.app.alcatel-lucent.com:2791'
  }
  dependencyManagement {
    dependencies {
      dependency {
        groupId 'org.springframework'
        artifactId 'spring-framework-bom'
        version '${spring.version}'
        type 'pom'
        scope 'import'
      }
    }
  }
  dependencies {
    dependency {
      groupId 'com.carrotgarden.jwrapper'
      artifactId 'jwrapper-7zip-jbinding'
      version '1.0.0'
    }
    dependency {
      groupId 'org.apache.commons'
      artifactId 'commons-compress'
      version '1.8.1'
    }
    dependency {
      groupId 'org.tukaani'
      artifactId 'xz'
      version '1.5'
    }
    dependency {
      groupId 'com.google.guava'
      artifactId 'guava'
      version '16.0.1'
    }
    dependency {
      groupId 'org.jetbrains'
      artifactId 'annotations'
      version '13.0'
    }
    dependency {
      groupId 'xml-apis'
      artifactId 'xml-apis'
      version '1.4.01'
    }
    dependency {
      groupId 'org.seleniumhq.selenium'
      artifactId 'selenium-java'
      version '2.39.0'
      scope 'test'
    }
    dependency {
      groupId 'org.mozilla'
      artifactId 'rhino'
      version '1.7R4'
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-context'
      exclusions {
        exclusion {
          artifactId 'commons-logging'
          groupId 'commons-logging'
        }
      }
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-test'
      scope 'test'
    }
    dependency {
      groupId 'org.apache.tomcat'
      artifactId 'juli'
      version '6.0.39'
      scope 'provided'
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-websocket'
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-webmvc'
    }
    dependency {
      groupId 'org.aspectj'
      artifactId 'aspectjweaver'
      version '1.7.0'
    }
    dependency {
      groupId 'org.apache.velocity'
      artifactId 'velocity'
      version '1.7'
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-context-support'
      exclusions {
        exclusion {
          artifactId 'commons-logging'
          groupId 'commons-logging'
        }
      }
    }
    dependency {
      groupId 'org.springframework'
      artifactId 'spring-orm'
    }
    dependency {
      groupId 'org.springframework.ldap'
      artifactId 'spring-ldap-core'
      version '1.3.2.RELEASE'
    }
    dependency {
      groupId 'org.springframework.ldap'
      artifactId 'spring-ldap-core-tiger'
      version '1.3.2.RELEASE'
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-proxool'
      version '${hibernate.version}'
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-core'
      version '${hibernate.version}'
      exclusions {
        exclusion {
          artifactId 'commons-collections'
          groupId 'commons-collections'
        }
      }
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-validator'
      version '4.3.0.Final'
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-oscache'
      version '3.6.9.Final'
    }
    dependency {
      groupId 'org.apache.lucene'
      artifactId 'lucene-queryparser'
      version '3.1.0'
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-search'
      version '3.4.2.Final'
    }
    dependency {
      groupId 'org.hibernate'
      artifactId 'hibernate-entitymanager'
      version '${hibernate.version}'
    }
    dependency {
      groupId 'org.slf4j'
      artifactId 'jcl-over-slf4j'
      version '1.7.5'
    }
    dependency {
      groupId 'org.slf4j'
      artifactId 'jul-to-slf4j'
      version '1.7.5'
    }
    dependency {
      groupId 'org.slf4j'
      artifactId 'slf4j-api'
      version '1.7.5'
    }
    dependency {
      groupId 'ch.qos.logback'
      artifactId 'logback-classic'
      version '1.0.13'
    }
    dependency {
      groupId 'cglib'
      artifactId 'cglib'
      version '2.2'
    }
    dependency {
      groupId 'mysql'
      artifactId 'mysql-connector-java'
      version '5.1.20'
      scope 'runtime'
    }
    dependency {
      groupId 'postgresql'
      artifactId 'postgresql'
      version '9.1-901.jdbc4'
      scope 'runtime'
    }
    dependency {
      groupId 'junit'
      artifactId 'junit'
      version '4.11'
      scope 'test'
    }
    dependency {
      groupId 'org.hamcrest'
      artifactId 'hamcrest-all'
      version '1.0'
      scope 'test'
    }
    dependency {
      groupId 'net.sf.json-lib'
      artifactId 'json-lib'
      version '2.4'
      classifier 'jdk15'
    }
    dependency {
      groupId 'com.h2database'
      artifactId 'h2'
      version '1.3.167'
      scope 'runtime'
    }
    dependency {
      groupId 'net.sourceforge.jchardet'
      artifactId 'jchardet'
      version '1.0'
    }
    dependency {
      groupId 'dom4j'
      artifactId 'dom4j'
      version '1.6.1'
    }
    dependency {
      groupId 'jaxen'
      artifactId 'jaxen'
      version '1.1.6'
    }
    dependency {
      groupId 'com.sun.jersey.contribs'
      artifactId 'jersey-spring'
      version '${jerseyVersion}'
      exclusions {
        exclusion {
          artifactId 'spring-aop'
          groupId 'org.springframework'
        }
        exclusion {
          artifactId 'spring-context'
          groupId 'org.springframework'
        }
        exclusion {
          artifactId 'spring-beans'
          groupId 'org.springframework'
        }
        exclusion {
          artifactId 'spring-core'
          groupId 'org.springframework'
        }
        exclusion {
          artifactId 'spring-web'
          groupId 'org.springframework'
        }
      }
    }
    dependency {
      groupId 'com.sun.jersey'
      artifactId 'jersey-bundle'
      version '1.9'
    }
    dependency {
      groupId 'org.apache.poi'
      artifactId 'poi'
      version '3.9'
    }
    dependency {
      groupId 'org.apache.poi'
      artifactId 'poi-ooxml'
      version '3.9'
    }
    dependency {
      groupId 'org.apache.struts'
      artifactId 'struts2-core'
      version '2.3.4.1'
    }
    dependency {
      groupId 'org.apache.struts'
      artifactId 'struts2-convention-plugin'
      version '2.3.4.1'
    }
    dependency {
      groupId 'org.apache.struts'
      artifactId 'struts2-json-plugin'
      version '2.3.4.1'
    }
    dependency {
      groupId 'org.apache.struts'
      artifactId 'struts2-spring-plugin'
      version '2.3.4.1'
    }
    dependency {
      groupId 'com.sun'
      artifactId 'tools'
      version '1.5.0'
      scope 'system'
      systemPath '${java.home}/../lib/tools.jar'
    }
    dependency {
      groupId 'javax.servlet'
      artifactId 'javax.servlet-api'
      version '3.0.1'
      scope 'provided'
    }
    dependency {
      groupId 'javax.servlet.jsp'
      artifactId 'jsp-api'
      version '2.2'
      scope 'provided'
    }
    dependency {
      groupId 'javax.servlet'
      artifactId 'jstl'
      version '1.2'
    }
    dependency {
      groupId 'org.codehaus.gmaven.runtime'
      artifactId 'gmaven-runtime-2.0'
      version '${gmavenVersion}'
      exclusions {
        exclusion {
          artifactId 'gossip'
          groupId 'org.sonatype.gossip'
        }
        exclusion {
          artifactId 'ant'
          groupId 'org.apache.ant'
        }
      }
    }
    dependency {
      groupId 'org.codehaus.groovy'
      artifactId 'groovy-all'
      version '2.3.0'
      exclusions {
        exclusion {
          artifactId 'jline'
          groupId 'jline'
        }
      }
    }
    dependency {
      groupId 'org.apache.tomcat.embed'
      artifactId 'tomcat-embed-core'
      version '7.0.47'
      scope 'provided'
    }
  }
  repositories {
    repository {
      releases {
        enabled 'true'
        updatePolicy 'always'
        checksumPolicy 'fail'
      }
      snapshots {
        enabled 'false'
        updatePolicy 'never'
      }
      id 'ShanghaiRepo'
      name 'Alcatel lucent repository in Shanghai'
      url 'http://repository.all.alcatel-lucent.com/nexus/content/groups/reference-cache'
    }
    repository {
      releases {
        enabled 'true'
        updatePolicy 'always'
        checksumPolicy 'fail'
      }
      snapshots {
        enabled 'false'
        updatePolicy 'never'
      }
      id 'GlobalRepo'
      name 'Alcatel lucent repository for global'
      url 'http://shrdwin014.cn.alcatel-lucent.com:8200/nexus/content/groups/reference-cache'
    }
  }
  build {
    defaultGoal 'org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run'
    resources {
      resource {
        filtering 'true'
        directory 'src/main/resources'
        includes {
          include '**/*.xml'
          include '**/*.properties'
          include '**/*.js'
          include '**/*.groovy'
        }
      }
      resource {
        filtering 'false'
        directory 'src/main/resources'
        excludes {
          exclude '**/*.xml'
          exclude '**/*.properties'
          exclude '**/*.js'
          exclude '**/*.groovy'
        }
      }
    }
    plugins {
      plugin {
        artifactId 'maven-compiler-plugin'
        version '2.3.2'
        configuration {
          source '1.6'
          target '1.6'
          encoding 'UTF-8'
          verbose 'false'
        }
      }
      plugin {
        groupId 'org.codehaus.gmaven'
        artifactId 'gmaven-plugin'
        version '${gmavenVersion}'
        executions {
          execution {
            goals {
              goal 'generateStubs'
              goal 'compile'
              goal 'generateTestStubs'
              goal 'testCompile'
            }
          }
        }
        dependencies {
          dependency {
            groupId 'org.sonatype.jline'
            artifactId 'jline'
            version '2.5'
          }
        }
        configuration {
          providerSelection '1.8'
          sourceEncoding 'UTF-8'
          source
        }
      }
      plugin {
        artifactId 'maven-war-plugin'
        version '2.4'
        executions {
          execution {
            phase 'prepare-package'
            goals {
              goal 'exploded'
            }
          }
        }
        configuration {
          packagingExcludes '%regex[js/(?!(?:(?:login)?[Ee]ntry\\.js|lib)).*$]'
          warSourceExcludes '%regex[(js/.*\\.(coffee|map|cmd|orig)$|tools/.*)]'
          useCache 'true'
          failOnMissingWebXml 'false'
        }
      }
      plugin {
        groupId 'org.mortbay.jetty'
        artifactId 'jetty-maven-plugin'
        version '8.1.5.v20120716'
        configuration {
          stopKey 'foo'
          stopPort '9999'
          scanIntervalSeconds '0'
          systemProperties {
            systemProperty {
              name 'org.eclipse.jetty.server.Request.queryEncoding'
              value 'utf-8'
            }
          }
        }
      }
      plugin {
        groupId 'org.codehaus.mojo'
        artifactId 'exec-maven-plugin'
        version '1.2.1'
        executions {
          execution {
            phase 'prepare-package'
            goals {
              goal 'exec'
              goal 'java'
            }
          }
        }
        configuration {
          mainClass 'com.alcatel_lucent.dms.service.DeployAdjust'
          executable 'node'
          arguments {
            argument 'src/main/webapp/tools/r.min.js'
            argument '-o'
            argument 'src/main/webapp/js/app.build.js'
            argument 'dir=${project.build.directory}/${project.build.finalName}'
          }
          systemProperties {
            systemProperty {
              key 'main.jsp'
              value '${project.build.directory}/${project.build.finalName}/main.jsp'
            }
            systemProperty {
              key 'login.jsp'
              value '${project.build.directory}/${project.build.finalName}/login.jsp'
            }
            systemProperty {
              key 'buildNumber'
              value '${maven.build.timestamp}'
            }
          }
        }
      }
      plugin {
        artifactId 'maven-failsafe-plugin'
        version '2.17'
        executions {
          execution {
            id 'integration-test'
            goals {
              goal 'integration-test'
            }
          }
          execution {
            id 'verify'
            goals {
              goal 'verify'
            }
          }
        }
      }
      plugin {
        groupId 'org.apache.tomcat.maven'
        artifactId 'tomcat7-maven-plugin'
        version '2.2'
        executions {
          execution {
            id 'tomcat-run'
            phase 'pre-integration-test'
            goals {
              goal 'run'
            }
            configuration {
              fork 'true'
            }
          }
          execution {
            id 'tomcat-shutdown'
            phase 'post-integration-test'
            goals {
              goal 'shutdown'
            }
          }
        }
        configuration {
          uriEncoding 'utf-8'
          url 'http://localhost:8888/manager/text'
          port '8888'
          contextReloadable 'true'
          path '/'
          username 'admin'
          password 'alcatel123'
          httpsPort '8443'
          keystoreFile 'conf/.keystore'
          keystorePass 'changeit'
          systemProperties {
            'java.util.logging.config.file' 'conf/logging.properties'
            'java.util.logging.SimpleFormatter.format' '[%4$s] %1$tH:%1$tM:%1$tS.%1$tL %2$s - %5$s %n'
          }
        }
      }
      plugin {
        artifactId 'maven-antrun-plugin'
        version '1.7'
        dependencies {
          dependency {
            groupId 'org.apache.ant'
            artifactId 'ant-jsch'
            version '1.9.2'
          }
          dependency {
            groupId 'com.jcraft'
            artifactId 'jsch'
            version '0.1.50'
          }
        }
        inherited 'false'
        configuration {
          target {
            echo(message:'Stopping test server ...')
            sshexec(command:'ls -la', host:'172.24.190.236', password:'alcatel123', port:'22', trust:'true', username:'dmsadmin')
          }
        }
      }
    }
  }
  profiles {
    profile {
      id '127.0.0.1_PostGreSQL'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://127.0.0.1:5432/dms'
        'db.hbm2ddl.auto' 'create'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/Local_PostGreSQL'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'MySQL199'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.MySQL5InnoDBDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:MySQL://172.24.191.199:3306/dms?characterEncoding=utf8&useCursorFetch=true&useUnicode=true'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/MySQL199'
        'db.driver' 'com.mysql.jdbc.Driver'
      }
    }
    profile {
      id 'qa-db'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://135.117.180.165:5432/dms_qa'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/qa_db'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'prod-db'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://135.117.180.165:5432/dms_prod'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/prod_db'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'local-qa3-db'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://172.24.190.236:5432/dms_qa3'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/qa3_db'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'local-qa-db'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://172.24.190.236:5432/dms_qa'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/local_qa_db'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'local-qa2-db'
      properties {
        'db.house-keeping-test-sql' 'select CURRENT_DATE'
        'db.password' 'alcatel123'
        'db.dialect' 'org.hibernate.dialect.PostgreSQLDialect'
        'db.user' 'dms'
        'db.url' 'jdbc:postgresql://172.24.190.236:5432/dms_qa2'
        'db.hbm2ddl.auto' 'update'
        'dms.luceneindex.dir' '${dms.temp}/dms/lucene/indexes/local_qa2_db'
        'db.driver' 'org.postgresql.Driver'
      }
    }
    profile {
      id 'dev'
      activation {
        activeByDefault 'true'
      }
      build {
        defaultGoal 'tomcat7:run'
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              path '/dms'
            }
          }
        }
      }
      properties {
        buildNumber '(new Date()).getTime()'
        httpsPort '8443'
        'logger.filter' 'INFO'
        'dms.temp' 'D:/tmp'
        httpPort '8888'
      }
    }
    profile {
      id 'qa'
      build {
        defaultGoal 'tomcat7:deploy'
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              url 'http://135.117.180.165:8888/manager/text'
            }
          }
        }
      }
      properties {
        httpsPort '8443'
        httpPort '8888'
      }
    }
    profile {
      id 'local-qa'
      build {
        defaultGoal 'tomcat7:deploy'
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              url 'http://172.24.190.236:8888/manager/text'
            }
          }
        }
      }
      properties {
        httpsPort '8443'
        httpPort '8888'
      }
    }
    profile {
      id 'local-qa2'
      build {
        defaultGoal 'tomcat7:deploy'
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              url 'http://172.24.190.236:9999/manager/text'
            }
          }
        }
      }
      properties {
        httpsPort '9443'
        httpPort '9999'
      }
    }
    profile {
      id 'temp-qa'
      build {
        defaultGoal 'tomcat7:deploy'
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              url 'http://frcolv10lin237.pqa-collab.fr.alcatel-lucent.com:7777/manager/text'
            }
          }
        }
      }
      properties {
        httpsPort '7443'
        httpPort '7777'
      }
    }
    profile {
      id 'prod'
      build {
        plugins {
          plugin {
            groupId 'org.apache.tomcat.maven'
            artifactId 'tomcat7-maven-plugin'
            configuration {
              url 'http://135.117.180.165:80/manager/text'
            }
          }
        }
      }
      properties {
        httpsPort '443'
        httpPort '80'
      }
    }
  }
}
