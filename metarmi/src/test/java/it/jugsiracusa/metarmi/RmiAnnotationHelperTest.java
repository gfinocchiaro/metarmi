package it.jugsiracusa.metarmi;

import static org.junit.Assert.assertEquals;

import java.rmi.Remote;

import org.junit.BeforeClass;
import org.junit.Test;

interface MyClass extends Remote {

	void foo();

	void bar(String a1, Integer a2);

	Integer toInteger(String value) throws NullPointerException,
			IllegalArgumentException;

}

public class RmiAnnotationHelperTest {

	private static RmiAnnotationHelper helper;
	
	@BeforeClass
	public static void  setup() {
		helper  = new  RmiAnnotationHelper(new NamingStrategy());
		
	}
	@Test
	public void testToCode() {
		String sourceInterface = helper.toCode(MyClass.class);
		StringBuilder sb = new StringBuilder();
		sb.append("package it.jugsiracusa.metarmi;\n\n");
		sb.append("public interface MyClass extends java.rmi.Remote {\n\n");
		sb.append("\tvoid foo()\n\n");
		sb.append("\tvoid bar(java.lang.String p1, java.lang.Integer p2)\n\n");
		sb.append("\tjava.lang.Integer toInteger(java.lang.String p1) ");
		sb.append("throws java.lang.NullPointerException, ");
		sb.append("java.lang.IllegalArgumentException;\n\n");
		sb.append("}");
		System.out.println(sourceInterface);
		assertEquals(sb.toString(), sourceInterface);
	}

}
