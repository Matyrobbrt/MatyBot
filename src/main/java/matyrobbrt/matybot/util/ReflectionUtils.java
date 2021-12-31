package matyrobbrt.matybot.util;

import static org.reflections.scanners.Scanners.FieldsAnnotated;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public class ReflectionUtils {

	public static final Reflections REFLECTIONS = new Reflections(
			new ConfigurationBuilder().forPackage("matyrobbrt.matybot").setScanners(TypesAnnotated, FieldsAnnotated));

	public static Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotation) {
		return REFLECTIONS.getTypesAnnotatedWith(annotation);
	}

	public static Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
		return REFLECTIONS.getFieldsAnnotatedWith(annotation);
	}
}
