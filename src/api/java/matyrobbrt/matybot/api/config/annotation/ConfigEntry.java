package matyrobbrt.matybot.api.config.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.electronwill.nightconfig.core.conversion.Converter;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigEntry {

	String name();

	String category() default "";

	String[] comments() default {};

	boolean commentDefaultValue() default true;

	Class<? extends Converter<?, ?>> converter() default GenericConverter.class;

	static abstract class GenericConverter implements Converter<String, String> {
	}
}
