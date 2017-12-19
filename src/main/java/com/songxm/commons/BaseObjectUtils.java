package com.songxm.commons;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.songxm.commons.annotation.JsonMosaic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class BaseObjectUtils {
	private static final List<Class<?>> PRIMITIVE = new ArrayList<>();
	private static final Map<String, Class<?>> EXTRA_NAME_TO_CLASS = ImmutableMap.of("decimal", BigDecimal.class, "bool",
			Boolean.class, "time", Date.class, "datetime", Date.class);

	public BaseObjectUtils() {
	}

	public static boolean isPrimitive(Type type) {
		return type instanceof ParameterizedType ? isPrimitive(((ParameterizedType) type).getActualTypeArguments()[0])
				: PRIMITIVE.contains(type);
	}

	public static Map<String, String> toMap(String name, Object value) {
		Map<String, String> map = new HashMap<>();
		if (value != null) {
			if (!PRIMITIVE.contains(value.getClass()) && !value.getClass().isPrimitive()) {
				if (value instanceof Enum) {
					map.put(name, ((Enum) value).name());
				} else if (!(value instanceof Collection) && !(value instanceof Map)) {
					Field[] fields = value.getClass().getDeclaredFields();
					if (fields != null) {
						Stream.of(fields).forEach((field) -> {
							Object fieldValue = null;

							try {
								field.setAccessible(true);
								fieldValue = field.get(value);
							} catch (Throwable var7) {
								log.warn("无法访问类[{}]成员变量[{}]", field.getClass().getSimpleName(), field.getName());
							}

							if (fieldValue != null) {
								if (isPrimitive(fieldValue.getClass())
										&& field.getAnnotation(JsonMosaic.class) != null) {
									JsonMosaic jsonMosaic = field.getAnnotation(JsonMosaic.class);
									String text = BaseJsonUtils.writeValue(fieldValue);
									fieldValue = BaseStringUtils.mosaic(text, jsonMosaic, '*');
								}

								map.putAll(toMap(
										StringUtils.isBlank(name) ? field.getName() : name + "." + field.getName(),
										fieldValue));
							}

						});
					}
				}
			} else {
				map.put(name, value.toString());
			}
		}

		return map;
	}

	public static Class forName(String name) {
		return forName(name, false);
	}

	public static Class<?> forName(String name, boolean returnWrapper) {
		if (StringUtils.isBlank(name)) {
			return null;
		} else if (EXTRA_NAME_TO_CLASS.containsKey(name.toLowerCase())) {
			return EXTRA_NAME_TO_CLASS.get(name.toLowerCase());
		} else {
			Class<?> result = PRIMITIVE.stream().filter((cls) -> {
				return cls.getSimpleName().equalsIgnoreCase(name);
			}).findAny().orElse(null);
			if (result != null) {
				return result.isPrimitive() && returnWrapper ? Primitives.wrap(result) : result;
			} else {
				try {
					return Class.forName(name);
				} catch (Throwable e) {
					return null;
				}
			}
		}
	}

	static {
		PRIMITIVE.add(Character.class);
		PRIMITIVE.add(String.class);
		PRIMITIVE.add(Boolean.class);
		PRIMITIVE.add(Byte.class);
		PRIMITIVE.add(Short.class);
		PRIMITIVE.add(Integer.class);
		PRIMITIVE.add(Long.class);
		PRIMITIVE.add(Float.class);
		PRIMITIVE.add(Double.class);
		PRIMITIVE.add(BigDecimal.class);
		PRIMITIVE.add(Date.class);
		PRIMITIVE.add(Boolean.TYPE);
		PRIMITIVE.add(Byte.TYPE);
		PRIMITIVE.add(Short.TYPE);
		PRIMITIVE.add(Integer.TYPE);
		PRIMITIVE.add(Long.TYPE);
		PRIMITIVE.add(Float.TYPE);
		PRIMITIVE.add(Double.TYPE);
		PRIMITIVE.add(Character.TYPE);
	}
}
