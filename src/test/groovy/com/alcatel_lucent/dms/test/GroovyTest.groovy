package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.model.Product

class GroovyTest {
	
	
	def name="Hello";
	Map<String,String> temp=['name':'Bruce', 'age':"27"]
	static main(args){
		def a = new GroovyTest();
		println a.name

		def p= new Product()
		p.mname="测试"

		print p.mname
	}

//	@CompileStatic
	def MyTest(){
	}
}
