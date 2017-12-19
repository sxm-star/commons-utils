package com.songxm.commons;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author songxm
 */
@Slf4j
public class BaseDateUtils {
    private static final TimeZone CHINA_TIMEZONE = TimeZone.getTimeZone("GMT+8");
    private static final FastDateFormat DATE_FORMAT;
    private static final FastDateFormat TIME_FORMAT;
    private static final FastDateFormat STANDARD_FORMAT;
    private static final Map<String, FastDateFormat> defaultFormats;
    private static final Map<Pattern, FastDateFormat> defaultPatterns;
    private static final Long millsPerDay;
    private static Pattern TIME_PATTERN;

    public BaseDateUtils() {
    }

    public static Date parseDate(String dateStr, String... patterns) {
        Preconditions.checkArgument(StringUtils.isNotBlank(dateStr), "无法将空字符串转换为Date对象");
        Preconditions.checkArgument(patterns.length > 0, "parseDate时必须指定至少一种格式");
        return (Date)Stream.of(patterns).map((pattern) -> {
            FastDateFormat format;
            if(defaultFormats.containsKey(pattern)) {
                format = (FastDateFormat)defaultFormats.get(pattern);
            } else {
                format = FastDateFormat.getInstance(pattern, CHINA_TIMEZONE);
            }

            try {
                return format.parse(dateStr);
            } catch (Exception ex) {
            	log.warn("时间解析异常:{},casuse:{}",ex.getMessage(),ex.getCause());
                return null;
            }
        }).filter((date) -> {
            return date != null;
        }).findFirst().orElse(null);
    }

    public static Date parseDate(String dateStr) {
        if(StringUtils.isBlank(dateStr)) {
            return null;
        } else {
            Pattern pattern = (Pattern)defaultPatterns.keySet().stream().filter((ptn) -> {
                return ptn.matcher(dateStr).matches();
            }).findAny().orElse(null);
            if(pattern != null) {
                try {
                    return ((FastDateFormat)defaultPatterns.get(pattern)).parse(dateStr);
                } catch (Exception var3) {
                    log.error("使用pattern[{}]解析时间串异常", pattern.pattern(), dateStr);
                    return null;
                }
            } else if(NumberUtils.isDigits(dateStr)) {
                if(dateStr.length() == 10) {
                    return new Date(Long.parseLong(dateStr) * 1000L);
                } else if(dateStr.length() == 13) {
                    return new Date(Long.parseLong(dateStr));
                } else {
                    log.error("无法将{}转换为时间", dateStr);
                    return null;
                }
            } else {
                log.error("无法找到合适的pattern解析时间串[{}]", dateStr);
                return null;
            }
        }
    }

    public static String format(Date date) {
        Preconditions.checkArgument(date != null);
        return STANDARD_FORMAT.format(date);
    }

    public static String format(Date date, String pattern) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(pattern), "pattern不能为空");
        return DateFormatUtils.format(date, pattern, CHINA_TIMEZONE);
    }

    public static Date add(Date date, int calendarField, int amount) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        if(amount == 0) {
            return date;
        } else {
            switch(calendarField) {
            case 1:
                return DateUtils.addYears(date, amount);
            case 2:
                return DateUtils.addMonths(date, amount);
            case 3:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            default:
                throw new IllegalArgumentException("不支持的calendar类型");
            case 5:
                return DateUtils.addDays(date, amount);
            case 10:
                return DateUtils.addHours(date, amount);
            case 12:
                return DateUtils.addMinutes(date, amount);
            case 13:
                return DateUtils.addSeconds(date, amount);
            case 14:
                return DateUtils.addMilliseconds(date, amount);
            }
        }
    }

    public static Date add(Date date, String timeSpan) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(timeSpan), "时间间隔不能为空");
        timeSpan = timeSpan.replaceAll("[\\s\\u00A0]{1,}", "");
        Matcher matcher = TIME_PATTERN.matcher(timeSpan);
        if(matcher.matches()) {
            String var3 = matcher.group(2);
            byte var4 = -1;
            switch(var3.hashCode()) {
            case 77:
                if(var3.equals("M")) {
                    var4 = 1;
                }
                break;
            case 100:
                if(var3.equals("d")) {
                    var4 = 2;
                }
                break;
            case 104:
                if(var3.equals("h")) {
                    var4 = 3;
                }
                break;
            case 109:
                if(var3.equals("m")) {
                    var4 = 4;
                }
                break;
            case 115:
                if(var3.equals("s")) {
                    var4 = 5;
                }
                break;
            case 121:
                if(var3.equals("y")) {
                    var4 = 0;
                }
                break;
            case 3494:
                if(var3.equals("ms")) {
                    var4 = 6;
                }
            }

            switch(var4) {
            case 0:
                return add(date, 1, Integer.valueOf(matcher.group(1)).intValue());
            case 1:
                return add(date, 2, Integer.valueOf(matcher.group(1)).intValue());
            case 2:
                return add(date, 5, Integer.valueOf(matcher.group(1)).intValue());
            case 3:
                return add(date, 10, Integer.valueOf(matcher.group(1)).intValue());
            case 4:
                return add(date, 12, Integer.valueOf(matcher.group(1)).intValue());
            case 5:
                return add(date, 13, Integer.valueOf(matcher.group(1)).intValue());
            case 6:
                return add(date, 14, Integer.valueOf(matcher.group(1)).intValue());
            default:
                return date;
            }
        } else {
            throw new IllegalArgumentException("timeSpan不符合格式:" + TIME_PATTERN.toString());
        }
    }

    public static String toDateFormat(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        return DATE_FORMAT.format(date);
    }

    public static String toDateFormat(String s) {
        return StringUtils.isBlank(s)?null:toDateFormat(parseDate(s));
    }

    public static Date fromDateFormat(String s) {
        if(StringUtils.isBlank(s)) {
            return null;
        } else {
            try {
                return DATE_FORMAT.parse(s);
            } catch (Exception var2) {
                throw new IllegalArgumentException("格式不符合yyyy-MM-dd");
            }
        }
    }

    public static String toTimeFormat(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        return TIME_FORMAT.format(date);
    }

    public static Date fromTimeFormat(String s) {
        if(StringUtils.isBlank(s)) {
            return null;
        } else {
            try {
                return TIME_FORMAT.parse(s);
            } catch (Exception var2) {
                throw new IllegalArgumentException("格式不符合yyy-MM-dd HH:mm:ss");
            }
        }
    }

    public static Date getMonthStart() {
        return getMonthStart(new Date());
    }

    public static Date getMonthStart(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        String chinaDate = toDateFormat(date);
        chinaDate = chinaDate.substring(0, 8) + "01";
        return fromDateFormat(chinaDate);
    }

    public static Date getMonthEnd() {
        return getMonthEnd(new Date());
    }

    public static Date getMonthEnd(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        Date nextMonthStart = getNextMonthStart(date);
        return DateUtils.addSeconds(nextMonthStart, -1);
    }

    public static Date getNextMonthStart() {
        return getNextMonthStart(new Date());
    }

    public static Date getNextMonthStart(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        String chinaDate = toDateFormat(date);
        int month = Integer.parseInt(chinaDate.substring(5, 7)) + 1;
        String sMonth = month + "";
        if(month < 10) {
            sMonth = "0" + sMonth;
        }

        return fromDateFormat(chinaDate.substring(0, 5) + sMonth + "-01");
    }

    public static Date getDayStart() {
        return getDayStart(new Date());
    }

    public static Date getDayStart(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        return parseDate(format(date, "yyyy-MM-dd"), new String[]{"yyyy-MM-dd"});
    }

    public static Date getDayEnd(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        return parseDate(format(date, "yyyy-MM-dd") + " 23:59:59", new String[]{"yyyy-MM-dd HH:mm:ss"});
    }

    public static boolean isSameYear(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar calendar1 = DateUtils.toCalendar(date1);
            Calendar calendar2 = DateUtils.toCalendar(date2);
            return calendar1.get(1) == calendar2.get(1);
        } else {
            return false;
        }
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar calendar1 = DateUtils.toCalendar(date1);
            Calendar calendar2 = DateUtils.toCalendar(date2);
            return calendar1.get(1) == calendar2.get(1) && calendar1.get(2) == calendar2.get(2);
        } else {
            return false;
        }
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar calendar1 = DateUtils.toCalendar(date1);
            Calendar calendar2 = DateUtils.toCalendar(date2);
            return calendar1.get(5) == calendar2.get(5);
        } else {
            return false;
        }
    }

    public static int getDayOfMonth(Date date) {
        Preconditions.checkArgument(date != null, "时间对象不能为空");
        return DateUtils.toCalendar(date).get(5);
    }

    public static int compareDay(Date date1, Date date2) {
        Preconditions.checkArgument(date1 != null && date2 != null, "时间对象不能为空");
        date1 = getDayStart(date1);
        date2 = getDayStart(date2);
        return (int)((date1.getTime() - date2.getTime()) / millsPerDay.longValue());
    }

    public static int compareMonth(Date date1, Date date2) {
        Preconditions.checkArgument(date1 != null && date2 != null, "时间对象不能为空");
        Calendar calendar1 = DateUtils.toCalendar(date1);
        Calendar calendar2 = DateUtils.toCalendar(date2);
        int year1 = calendar1.get(1);
        int year2 = calendar2.get(1);
        int month1 = calendar1.get(2);
        int month2 = calendar2.get(2);
        return month1 - month2 + (year1 - year2) * 12;
    }

    public static int compareYear(Date date1, Date date2) {
        Preconditions.checkArgument(date1 != null && date2 != null, "时间对象不能为空");
        int year1 = DateUtils.toCalendar(date1).get(1);
        int year2 = DateUtils.toCalendar(date2).get(1);
        return year1 - year2;
    }

    static {
        DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd", CHINA_TIMEZONE);
        TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", CHINA_TIMEZONE);
        STANDARD_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ", CHINA_TIMEZONE);
       
        defaultFormats = new ImmutableMap.Builder<String, FastDateFormat>().put("yyyy-MM-dd", DATE_FORMAT).put("yyyy-MM-dd HH:mm:ss", TIME_FORMAT).put("yyyy-MM-dd\'T\'HH:mm:ss.SSSZ", STANDARD_FORMAT).put("yyyyMMdd", FastDateFormat.getInstance("yyyyMMdd", CHINA_TIMEZONE)).put("yyyyMMddHHmmss", FastDateFormat.getInstance("yyyyMMddHHmmss", CHINA_TIMEZONE)).put("yyyyMMddHHmmssSSS", FastDateFormat.getInstance("yyyyMMddHHmmssSSS", CHINA_TIMEZONE)).put("yyyy-M-dd", FastDateFormat.getInstance("yyyy-M-dd", CHINA_TIMEZONE)).put("yyyy-MM", FastDateFormat.getInstance("yyyy-MM", CHINA_TIMEZONE)).put("yyyy-M", FastDateFormat.getInstance("yyyy-M", CHINA_TIMEZONE)).put("yyyyMddHHmmss", FastDateFormat.getInstance("yyyyMddHHmmss", CHINA_TIMEZONE)).put("yyyyMM", FastDateFormat.getInstance("yyyyMM", CHINA_TIMEZONE)).put("yyyyM", FastDateFormat.getInstance("yyyyM", CHINA_TIMEZONE)).build();
        defaultPatterns =new ImmutableMap.Builder<Pattern, FastDateFormat>().put(Pattern.compile("\\d{4}-\\d{2}-\\d{2}"), DATE_FORMAT).put(Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), TIME_FORMAT).put(Pattern.compile("\\d{4}-\\d{2}-\\d{2}\'T\'\\d{2}:\\d{2}:\\d{2}.\\d{3}.*"), STANDARD_FORMAT).put(Pattern.compile("\\d{8}"), defaultFormats.get("yyyyMMdd")).put(Pattern.compile("\\d{14}"), defaultFormats.get("yyyyMMddHHmmss")).put(Pattern.compile("\\d{17}"), defaultFormats.get("yyyyMMddHHmmssSSS")).put(Pattern.compile("\\d{4}-\\d{1,2}-\\d{2}"), defaultFormats.get("yyyy-M-dd")).put(Pattern.compile("\\d{4}-\\d{2}"), defaultFormats.get("yyyy-MM")).put(Pattern.compile("\\d{4}-\\d{1,2}"), defaultFormats.get("yyyy-M")).put(Pattern.compile("\\d{13,14}"), defaultFormats.get("yyyyMddHHmmss")).put(Pattern.compile("\\d{6}"), defaultFormats.get("yyyyMM")).put(Pattern.compile("\\d{5,6}"), defaultFormats.get("yyyyM")).build();
        millsPerDay = Long.valueOf(86400000L);
        TIME_PATTERN = Pattern.compile("(-?\\d+)(y|M|d|h|m|s|ms)");
    }
}
