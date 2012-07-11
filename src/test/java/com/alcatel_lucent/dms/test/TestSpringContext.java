package com.alcatel_lucent.dms.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.logicalcobwebs.proxool.ProxoolFacade;

//import  org.hibernate.type.descriptor.sql.BasicBinder;

public class TestSpringContext {

	public static void main(String[] args) throws Exception {
		// SpringContext.getService(DaoService.class);
		
//		String test="這是一個測試";
//		PrintStream out=new PrintStream(new FileOutputStream("Test.txt"),true,"GBK");
//		out.write(test.getBytes("GBK"));
//		out.println();
//		out.write(test.getBytes("BIG5"));
//		out.println();
//		out.write(test.getBytes("UTF8"));
//		out.close();
		
		String test="天知道";
		String errCode="gb2312";
		System.out.println("原串："+test);
		byte[] step1=test.getBytes("big5");
		System.out.print("编码为big5后字节数组："+getByteArrayHexString(step1));
		String step2=new String(step1, errCode);
		System.out.println("误以"+errCode+"解码后："+step2);
		byte[] step3=step2.getBytes(errCode);
		System.out.print("重新以误码"+errCode+"编码后数组："+getByteArrayHexString(step1));
		
		String step4=new String(step3,"big5");
		System.out.println("将解码后："+step4);
		
		String[] temp=new String[]{"a","b"};
		StringUtils.join(temp,"#");
		
		ProxoolFacade.shutdown(0);
	}
	
	public static String getByteArrayHexString(byte[] array){
		StringWriter sw=new StringWriter();
		PrintWriter out= new PrintWriter(sw);
		out.print("{");
		for(byte b: array){
			out.printf("%x ", b);
		}
		out.println("}");
		out.close();
		return sw.toString();
	}
}
