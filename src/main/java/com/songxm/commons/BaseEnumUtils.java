
package com.songxm.commons;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
@SuppressWarnings("unchecked")
public class BaseEnumUtils {
    public BaseEnumUtils() {
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
        return getEnum(enumClass, name, null);
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name, T defaultEnum) {
        if(StringUtils.isBlank(name)) {
            return defaultEnum;
        } else {
            Enum[] var3 = (Enum[])enumClass.getEnumConstants();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Enum t = var3[var5];
                if(t.name().equalsIgnoreCase(name)) {
                    return (T)t;
                }
            }

            return defaultEnum;
        }
    }
}
