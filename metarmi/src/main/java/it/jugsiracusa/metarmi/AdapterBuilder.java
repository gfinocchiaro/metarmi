package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteMethod;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;

public class AdapterBuilder implements IMemberAppender, IInterfaceBuilder {

	private CtClass ra;
	private String adapterClassName;

	private Set<Class<?>> interfaces = new HashSet<Class<?>>();

	public AdapterBuilder(CtClass ra, String adapterClassName) {
		this.ra = ra;
		this.adapterClassName = adapterClassName;
	}

	public IClassBuilder setConstructor(Class<?> remoteObjectClass) {
		try {
			CtField[] declaredFields = ra.getDeclaredFields();
			String fieldName = "";
			for (CtField ctField : declaredFields) {
				if (ctField.getType().getName().equals(
						remoteObjectClass.getName())) {
					fieldName = ctField.getName();
				}
			}
			String name = adapterClassName.substring(adapterClassName
					.lastIndexOf('.') + 1);
			String src = "public " + name + "(" + remoteObjectClass.getName()
					+ " remoteObject) { this." + fieldName
					+ "  = remoteObject;}";
			ra.addConstructor(CtNewConstructor.make(src, ra));
			return this;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public IMemberAppender addInterface(Class<?> interfaceClass) {
		interfaces.add(interfaceClass);
		return this;
	}

	public IMemberAppender addField(Class<?> fieldType, String fieldName) {
		try {
			String src = String.format("private %s %s;", fieldType.getName(),
					fieldName);
			ra.addField(CtField.make(src, ra));
			return this;
		} catch (CannotCompileException e) {
			throw new RuntimeException(e);
		}
	}

	public IMemberAppender addMethod(Method method) {
		try {
			RemoteMethod metaMethod = method.getAnnotation(RemoteMethod.class);
			if (metaMethod != null) {
				for (Class<?> interfaceClass : metaMethod.targetInterface()) {
					addInterface(interfaceClass);
				}
				String methodSrc = makeDelegatingMethod(method, metaMethod);
				ra.addMethod(CtMethod.make(methodSrc, ra));
			}

			return this;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String makeDelegatingMethod(Method method,
			RemoteMethod annotation) throws CannotCompileException {
		String implementingMethod = method.getName();
		String declaredName = annotation.name();
		String interfaceMethod = "".equals(declaredName) ? implementingMethod
				: declaredName;
		String returnType = method.getReturnType().getName();
		Class<?> targetType = method.getDeclaringClass();
		String methodSrc = String.format(
				"public %s %s() { return ((%s)remoteObject).%s(); }",
				returnType, interfaceMethod, targetType.getName(),
				implementingMethod);

		return methodSrc;
	}

	public Class<?> toClass() {
		try {
			Class<?>[] interfaceClassList = interfaces.toArray(new Class<?>[0]);
			for (Class<?> interfaceClass : interfaceClassList) {
				ra.addInterface(ClassPool.getDefault().get(
						interfaceClass.getName()));
			}
			return ra.toClass();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
