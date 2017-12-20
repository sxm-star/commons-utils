
package com.songxm.commons;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class BaseNumberUtils {
    private static final Logger log = LoggerFactory.getLogger(BaseNumberUtils.class);

    public BaseNumberUtils() {
    }

    public static Long yuanToFen(BigDecimal amount) {
        return amount == null?null:Long.valueOf(amount.multiply(new BigDecimal(100)).longValue());
    }

    public static Long yuanToFen(String amount) {
        return !StringUtils.isBlank(amount) && NumberUtils.isNumber(amount)?Long.valueOf((new BigDecimal(amount)).multiply(new BigDecimal(100)).longValue()):null;
    }

    public static Long yuanToFenNotNull(BigDecimal amount) {
        return amount == null?Long.valueOf(0L):Long.valueOf(amount.multiply(new BigDecimal(100)).longValue());
    }

    public static int compare(BigDecimal num1, BigDecimal num2) {
        return num1 == null && num2 == null?0:(num1 == null?-1:(num2 == null?1:num1.compareTo(num2)));
    }

    public static String numToString(Integer num, Integer len, char fillChar) {
        Preconditions.checkArgument(num != null, "num不能为null");
        String result = num + "";
        return result.length() > len.intValue()?result.substring(0, len.intValue()):StringUtils.repeat(fillChar, len.intValue() - result.length()) + result;
    }
}
