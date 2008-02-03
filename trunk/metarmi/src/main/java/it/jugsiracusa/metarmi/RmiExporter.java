package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteMethod;
import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.lang.reflect.Field;
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

		String bindName = rsAnnotation.name();
		try {
			ClassPool cp = ClassPool.getDefault();
			CtClass ra = cp.get("it.jugsiracusa.metarmi.RmiObjectAdapter");

			Method[] m = remoteClass.getDeclaredMethods();
			Set<String> interfaces = new HashSet<String>();
			for (Method method : m) {
				RemoteMethod metaMethod = method
						.getAnnotation(RemoteMethod.class);
				if (metaMethod != null) {
					for (Class<?> interaceClass : metaMethod.targetInterface()) {
						interfaces.add(interaceClass.getName());
					}
					String methodSrc = makeDelegatingMethod(method, metaMethod,
							remoteClass);
					ra.addMethod(CtMethod.make(methodSrc, ra));
				}
			}
			for (Iterator<String> iterator = interfaces.iterator(); iterator
					.hasNext();) {
				String interfaceClassName = (String) iterator.next();
				ra.addInterface(cp.get(interfaceClassName));
			}

			Class<?> adapterClass = ra.toClass();
			Object adapter = adapterClass.getConstructor(Object.class)
					.newInstance(remoteObject);
			Field remoteField = adapterClass.getDeclaredField("remoteObject");
			remoteField.setAccessible(true);
			remoteField.set(adapter, remoteObject);

			/* Exporting object */
			Remote stub = UnicastRemoteObject.exportObject((Remote) adapter, 0);

			/* Binding object into the registry */
			registry.bind(bindName, stub);
		} catch (Exception e) {
			throw new RuntimeException(e);

		}

	}

	private static String makeDelegatingMethod(Method method, RemoteMethod annotation,
			Class<?> targetType)
			throws CannotCompileException {
		String implementingMethod = method.getName();
		String declaredName = annotation.name();
		String interfaceMethod = "".equals(declaredName) ? implementingMethod
				: declaredName;
		String returnType = method.getReturnType().getName();
		String methodSrc = String.format(
				"public %s %s() { return ((%s)remoteObject).%s(); }",
				returnType, interfaceMethod, targetType.getName(),
				implementingMethod);

		return methodSrc;
	}

}
