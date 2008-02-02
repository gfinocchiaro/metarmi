package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteMethod;
import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class RmiExporter {

	public static void export(Object remoteObject, Registry registry) {
		Class<? extends Object> remoteClass = remoteObject.getClass();
		RemoteService rsAnnotation = remoteClass
				.getAnnotation(RemoteService.class);

		if (rsAnnotation == null) {
			throw new IllegalArgumentException(
					"Cannot export a non RemoteService annotated class!");
		}

		String bindName = rsAnnotation.bind();
		try {
			ClassPool cp = ClassPool.getDefault();
			CtClass ra = cp
					.makeClass("it.jugsiracusa.rmi.metadata.RemoteAdapter");

			addTargetField(remoteClass, ra);

			Method[] m = remoteClass.getDeclaredMethods();
			Set<String> interfaces = new HashSet<String>();
			for (Method method : m) {
				RemoteMethod metaMethod = method
						.getAnnotation(RemoteMethod.class);
				if (metaMethod != null) {
					for (Class<?> interaceClass : metaMethod.targetInterface()) {
						interfaces.add(interaceClass.getName());
					}
					addDelegatingMethod(ra, method, metaMethod);
				}
			}
			for (Iterator<String> iterator = interfaces.iterator(); iterator
					.hasNext();) {
				String interfaceClassName = (String) iterator.next();
				ra.addInterface(cp.get(interfaceClassName));
			}

			Class<?> adapterClass = ra.toClass();
			Object adapter = adapterClass.newInstance();
			adapterClass.getDeclaredField("target").set(adapter, remoteObject);

			/* Exporting object */
			Remote stub = UnicastRemoteObject.exportObject((Remote) adapter, 0);

			/* Binding object into the registry */
			registry.bind(bindName, stub);
		} catch (Exception e) {
			throw new RuntimeException(e);

		}

	}

	private static void addDelegatingMethod(CtClass ra, Method method,
			RemoteMethod annotation) throws CannotCompileException {
		String implementingMethod = method.getName();
		String declaredName = annotation.name();
		String interfaceMethod = "".equals(declaredName) ? implementingMethod
				: declaredName;
		String returnType = method.getReturnType().getName();
		String methodSrc = String.format(
				"public %s %s() { return target.%s(); }", returnType,
				interfaceMethod, implementingMethod);
		ra.addMethod(CtMethod.make(methodSrc, ra));
	}

	private static void addTargetField(Class<? extends Object> remoteClass,
			CtClass ra) throws CannotCompileException {
		CtField target = CtField.make("public " + remoteClass.getName()
				+ "  target;", ra);
		ra.addField(target);
	}

}
