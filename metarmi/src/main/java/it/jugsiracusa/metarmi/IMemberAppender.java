package it.jugsiracusa.metarmi;

import java.lang.reflect.Method;

public interface IMemberAppender  extends IClassBuilder {
	
	IMemberAppender addField(Class<?> fieldType, String fieldName);
	
	IMemberAppender addMethod(Method method);
	
	IClassBuilder setConstructor(Class<?> cl);

}
