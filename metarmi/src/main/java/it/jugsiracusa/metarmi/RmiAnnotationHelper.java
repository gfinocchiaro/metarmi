package it.jugsiracusa.metarmi;

import it.jugsiracusa.metarmi.metadata.RemoteMethod;
import it.jugsiracusa.metarmi.metadata.RemoteService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import example.hello.server.MyGreetings;

public class RmiAnnotationHelper {

	private NamingStrategy ns;

	public RmiAnnotationHelper(NamingStrategy ns) {
		this.ns = ns;
	}
	
	public Class<?> createRMIAdapter(Class<?> remoteClass)
			throws NotFoundException, CannotCompileException {
		ClassPool pool = ClassPool.getDefault();

		CtClass remoteInterface = makeRemoteInterface(remoteClass, pool);

		CtClass adapterClass = pool.makeClass(ns.getRMIAdapterClassName(remoteClass));
		adapterClass.addField(new CtField(pool.get(remoteClass.getName()), "remoteObject", adapterClass));
		adapterClass.addInterface(remoteInterface);

		Method[] methods = remoteClass.getDeclaredMethods();
		for (Method method : methods) {
			RemoteMethod remoteAnnotation = method
					.getAnnotation(RemoteMethod.class);
			if (remoteAnnotation == null) {
				continue;
			}

			/* Extracting parameter types */
			Class<?>[] parameterTypes = method.getParameterTypes();
			Collection<CtClass> ctParameterTypes = new ArrayList<CtClass>();
			for (Class<?> clazz : parameterTypes) {
				ctParameterTypes.add(pool.get(clazz.getName()));
			}
			CtClass[] ctParameterTypesArr = new CtClass[ctParameterTypes.size()];
			ctParameterTypes.toArray(ctParameterTypesArr);

			/* Extracting exception types */
			Class<?>[] exceptionTypes = method.getExceptionTypes();
			Collection<CtClass> ctExceptionTypes = new ArrayList<CtClass>();
			for (Class<?> clazz : exceptionTypes) {
				ctExceptionTypes.add(pool.get(clazz.getName()));
			}
			ctExceptionTypes.add(pool.get(RemoteException.class.getName()));
			CtClass[] ctExceptionTypesArr = new CtClass[ctExceptionTypes.size()];
			ctExceptionTypes.toArray(ctExceptionTypesArr);

			/* Adding remote method to interface as abstract method */
			String remoteMethod = remoteAnnotation.name() == null ? method.getName() : remoteAnnotation.name();
			CtClass returnType = pool.get(method.getReturnType().getName());
			CtMethod abstractMethod = CtNewMethod.abstractMethod(returnType,
					remoteMethod, ctParameterTypesArr, ctExceptionTypesArr,
					remoteInterface);
			remoteInterface.addMethod(abstractMethod);

			/*
			 * Implementing interface method on adapter class, as a delegating
			 * method to concrete remote class method
			 */
			String body = "{ return remoteObject." + method.getName() + "(); }";
//			String body = "{ return \"Hello!\"; }";
			CtMethod concreteMethod = CtNewMethod.make(returnType, remoteMethod, ctParameterTypesArr, ctExceptionTypesArr, body,
					adapterClass);
			
			adapterClass.addMethod(concreteMethod);
		}
		
		CtMethod toStringMethod =CtNewMethod.make(pool.get(java.lang.String.class.getName()), "toString", new CtClass[0], new CtClass[0], "return \"Hello!\";", adapterClass);
		adapterClass.addMethod(toStringMethod);
		

		try {
			remoteInterface.writeFile("generated");
			adapterClass.writeFile("generated");
		} catch (IOException e) {
			e.printStackTrace();
		}
		adapterClass.freeze();
		remoteInterface.toClass();

		return adapterClass.toClass();
	}

	private CtClass makeRemoteInterface(Class<?> remoteClass, ClassPool pool)
			throws CannotCompileException, NotFoundException {
		CtClass remoteInterface = pool.makeInterface(remoteClass.getAnnotation(RemoteService.class).target());

		/* Setting java.rmi.Remote interface superclass */
		remoteInterface.setSuperclass(pool.get(Remote.class.getName()));
		return remoteInterface;
	}

//	private CtClass makeRemoteInterface(Class<?> remoteClass, ClassPool pool)
//			throws CannotCompileException, NotFoundException {
//		return pool.get(Remote.class.getName());
//	}
	
	public String toCode(Class<?> sourceClass) {
		/*
		 * if (!sourceClass.isInterface()) { throw new
		 * IllegalArgumentException(sourceClass.getName() + " is not an
		 * interface"); }
		 */
		StringBuilder template = new StringBuilder();
		Package pkg = sourceClass.getPackage();

		/* Package declaration */
		if (pkg != null) {
			template.append("package ");
			template.append(pkg.getName());
			template.append(";\n\n");
		}

		/* Start interface */
		template.append("public ");
		boolean anInterface = sourceClass.isInterface();
		template.append(anInterface ? "interface" : "class");
		template.append(" ");
		template.append(sourceClass.getSimpleName());
		template.append(" ");

		if (!anInterface) {
			Class<?> superClass = sourceClass.getSuperclass();
			if (superClass != null
					&& !superClass.getName().equals("java.lang.Object")) {
				template.append("extends ");
				template.append(superClass.getName());
				template.append(" ");
			}
		}

		Class[] interfaces = sourceClass.getInterfaces();
		if (interfaces.length != 0) {
			template.append(anInterface ? "extends" : "implements");
			template.append(" ");
			int inCount = 0;
			for (Class inter : interfaces) {
				if (inCount != 0) {
					template.append(", ");
				}
				template.append(inter.getName());
				inCount++;
			}

		}
		template.append(" {\n");
		template.append("\n");

		/* Start methods enumeration */
		Method[] methods = sourceClass.getDeclaredMethods();
		for (Method method : methods) {

			/* Start method declaration */
			template.append("\t");
			Class<?> returnType = method.getReturnType();

			/* Return type */
			template.append(returnType.getName());
			template.append(" ");

			/* Method name */
			String name = method.getName();
			template.append(name);
			template.append("(");

			/* Parameters enumeration */
			Class<?>[] parameterTypes = method.getParameterTypes();
			int formalParameterCounteri = 0;
			for (Class<?> parType : parameterTypes) {
				if (formalParameterCounteri != 0) {
					template.append(", ");
				}
				template.append(parType.getName());
				template.append(" ");
				template.append("p");
				template.append(++formalParameterCounteri);
			}
			template.append(")");

			/* Exception enumeration */
			Class<?>[] exceptionTypes = method.getExceptionTypes();
			if (exceptionTypes.length != 0) {
				int exceptionCounter = 0;
				template.append(" throws ");
				for (Class<?> exType : exceptionTypes) {
					if (exceptionCounter != 0) {
						template.append(", ");
					}
					template.append(exType.getName());
					exceptionCounter++;
				}
				template.append(";");
			}

			/* End method declaration */
			template.append("\n\n");
		}

		/* End interface */
		template.append("}");

		return template.toString();
	}

	public static void main(String[] args) throws Exception {
		RmiAnnotationHelper rmiAnnotationHelper = new RmiAnnotationHelper(new NamingStrategy());
		Class<?> adapter = rmiAnnotationHelper
				.createRMIAdapter(MyGreetings.class);
		System.out.println(rmiAnnotationHelper.toCode(adapter));
		;

		Class<?>[] interfaces = adapter.getInterfaces();
		for (Class<?> ctClass : interfaces) {
			System.out.println(rmiAnnotationHelper.toCode(ctClass));
		}

	}
}