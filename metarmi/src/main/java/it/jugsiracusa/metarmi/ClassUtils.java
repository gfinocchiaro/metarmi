package it.jugsiracusa.metarmi;

import java.lang.reflect.Constructor;

public class ClassUtils {
	
	public static Object newInstance(Class<?> cl, Object... args) {
		try {
			if (args == null) {
				return cl.newInstance();
			} else {
				Class<?>[] classArgs = new Class<?>[args.length];
				for (int i = 0; i < classArgs.length; i++) {
					classArgs[i] = args[i].getClass();
				}
				Constructor<?> cs = cl.getConstructor(classArgs);
				return cs.newInstance(args);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
