package com.appleframework.config.core.util;

public abstract class Assert {

	/**
	 * Assert that an object is not {@code null}.
	 * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
	 * @param object the object to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object is {@code null}
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Assert that the given String is not empty; that is,
	 * it must not be {@code null} and not the empty String.
	 * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
	 * @param text the String to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the text is empty
	 * @see StringUtils#hasLength
	 */
	public static void hasLength(String text, String message) {
		if (!StringUtils.isEmptyString(text)) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * <pre class="code">Assert.notEmpty(array, "The array must contain elements");</pre>
	 * @param array the array to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object array is {@code null} or contains no elements
	 */
	public static void notEmpty(Object[] array, String message) {
		if (ObjectUtils.isEmpty(array)) {
			throw new IllegalArgumentException(message);
		}
	}

}
