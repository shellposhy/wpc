package cn.com.cms.framework.esb.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a method (or all the methods on a class) can be
 * cached.
 *
 * <p>
 * The method arguments and signature are used for computing the key while the
 * returned instance is used as the cache value.
 *
 * @author shishb
 * @since 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface IgniteCacheable {
	/**
	 * Name of the caches in which the update takes place.
	 * <p>
	 * May be used to determine the target cache (or caches), matching the
	 * qualifier value (or the bean name(s)) of (a) specific bean definition.
	 */
	String[] value();

	/**
	 * Spring Expression Language (SpEL) attribute for computing the key
	 * dynamically.
	 * <p>
	 * Default is "", meaning all method parameters are considered as a key.
	 */
	String key() default "";

	/**
	 * The time default cache save time
	 * <p>
	 * Default 900s
	 */
	int timeout() default 900;
}
