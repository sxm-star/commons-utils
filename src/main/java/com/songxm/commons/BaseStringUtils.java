
package com.songxm.commons;

import com.google.common.base.Preconditions;
import com.netflix.astyanax.util.TimeUUIDUtils;
import com.songxm.commons.annotation.JsonMosaic;
import com.songxm.commons.model.Inflector;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseStringUtils {
    private static final String IGNORE_CHARS = "*#";
    private static final Integer MACHINE_MIN_LEN = Integer.valueOf(3);
    private static final Integer MACHINE_MAX_LEN = Integer.valueOf(6);
    private static final Date START_DATE = BaseDateUtils.fromDateFormat("2010-01-01");
    private static final AtomicLong SMALL_RANDOM = new AtomicLong(0L);
    private static List<String> SINGULARS = Arrays.asList(new String[]{"goods", "Goods", "penny", "Penny", "sms", "Sms", "data", "Data"});
    private static List<String> PLURALS = Arrays.asList(new String[]{"goods", "Goods", "pence", "Pence", "smses", "Smses", "data", "Data"});

    public static final String PATTERN = "[A-Z]{2,}";
    public BaseStringUtils() {
    }

    public static int compare(String s1, String s2, String ignoredChars, int matchCount) {
        if(ignoredChars == null) {
            return s1.equals(s2)?1:-1;
        } else if(length(s1, ignoredChars) != 0 && length(s2, ignoredChars) != 0) {
            if(s1.length() != s2.length()) {
                return -1;
            } else {
                int count = 0;

                for(int i = 0; i < s1.length(); ++i) {
                    if(!StringUtils.contains(ignoredChars, s1.charAt(i)) && !StringUtils.contains(ignoredChars, s2.charAt(i))) {
                        if(s1.charAt(i) != s2.charAt(i)) {
                            return -1;
                        }

                        ++count;
                    }
                }

                if(matchCount == -1) {
                    matchCount = s1.length();
                }

                if(count >= matchCount) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public static int compare(String s1, String s2) {
        return compare(s1, s2, -1);
    }

    public static int compare(String s1, String s2, int matchCount) {
        return compare(s1, s2, "*#", matchCount);
    }

    public static String merge(String s1, String s2, String ignoredChars) {
        Preconditions.checkArgument(StringUtils.isNotBlank(ignoredChars), "ignoredChars不能为空");
        if(length(s1, ignoredChars) == 0) {
            return s2;
        } else if(length(s2, ignoredChars) == 0) {
            return s1;
        } else if(s1.length() != s2.length()) {
            return null;
        } else {
            StringBuilder buf = new StringBuilder();

            for(int i = 0; i < s1.length(); ++i) {
                if(!StringUtils.contains(ignoredChars, s1.charAt(i)) && !StringUtils.contains(ignoredChars, s2.charAt(i))) {
                    if(s1.charAt(i) != s2.charAt(i)) {
                        return null;
                    }

                    buf.append(s1.charAt(i));
                } else {
                    buf.append(StringUtils.contains(ignoredChars, s1.charAt(i))?s2.charAt(i):s1.charAt(i));
                }
            }

            return buf.toString();
        }
    }

    public static String merge(String s1, String s2) {
        return merge(s1, s2, "*#");
    }

    public static String mergeSave(String s1, String s2) {
        return mergeSave(s1, s2, "*#");
    }

    public static String mergeSave(String s1, String s2, String ignoredChars) {
        String result = s2;
        if(length(s1, ignoredChars) != 0) {
            if(length(s2, ignoredChars) != 0) {
                String merged = merge(s1, s2, "*#");
                if(StringUtils.isNotBlank(merged)) {
                    result = merged;
                } else if(length(s1, ignoredChars) > length(s2, ignoredChars)) {
                    result = s1;
                }
            } else {
                result = s1;
            }
        } else if(s1 != null && s2 == null) {
            result = s1;
        }

        return result;
    }

    public static int length(String src, String ignoreChars) {
        return StringUtils.isBlank(src)?0:src.length() - ignoreChars.chars().map((c) -> {
            return StringUtils.countMatches(src, (char)c);
        }).sum();
    }

    public static String uuid() {
        return TimeUUIDUtils.getUniqueTimeUUIDinMillis().toString();
    }

    public static String digitalUUID() {
        return digitalUUID(Integer.valueOf(20), (Long)null);
    }

    public static String digitalUUID(Integer len) {
        return digitalUUID(len, (Long)null);
    }

    public static String digitalUUID(Integer len, Long suffix) {
        Preconditions.checkArgument(len.intValue() > 16, "len必须在17位或以上");
        StringBuilder buf = new StringBuilder(System.currentTimeMillis() + "");
        int machineLen = MACHINE_MIN_LEN.intValue();
        int randomLen = len.intValue() - 13 - machineLen;
        if(randomLen > 4) {
            int hostName = randomLen - 4;
            machineLen = 4 + hostName / 2;
            if(machineLen > MACHINE_MAX_LEN.intValue()) {
                machineLen = MACHINE_MAX_LEN.intValue();
            }

            randomLen = len.intValue() - 13 - machineLen;
        }

        String var7 = BaseMachineUtils.getHostName();
        if(StringUtils.isBlank(var7)) {
            buf.append(StringUtils.repeat("0", machineLen));
        } else {
            buf.append(BaseSecurityUtils.md5(true, new Object[]{var7}).substring(0, machineLen));
        }

        if(randomLen >= 16) {
            for(int i = 0; i < randomLen / 16; ++i) {
                buf.append(RandomUtils.nextLong(1000000000000000L, 10000000000000000L));
            }

            randomLen %= 16;
        }

        if(randomLen > 0) {
            buf.append(innerRandom(randomLen));
        }

        if(suffix != null) {
            buf.append(suffix);
        }

        return buf.toString();
    }

    public static Date timeFromDigitalUUID(String uuid) {
        Preconditions.checkArgument(StringUtils.isNotBlank(uuid), "uuid不能为空");
        Preconditions.checkArgument(uuid.length() >= 13, "uuid长度不能小于13");
        String timeStr = uuid.substring(0, 13);
        if(!NumberUtils.isDigits(timeStr)) {
            throw new RuntimeException("uuid[" + uuid + "]格式不正确");
        } else {
            return new Date(Long.parseLong(timeStr));
        }
    }

    public static long longUid() {
        StringBuilder buf = new StringBuilder(System.currentTimeMillis() - START_DATE.getTime() + "");
        if(buf.length() <= 12 && buf.toString().compareTo("922337203684") <= 0) {
            String hostName = BaseMachineUtils.getHostName();
            if(StringUtils.isBlank(hostName)) {
                buf.append(StringUtils.repeat('0', MACHINE_MIN_LEN.intValue()));
            } else {
                buf.append(BaseSecurityUtils.md5(true, new Object[]{hostName}).substring(0, MACHINE_MIN_LEN.intValue()));
            }

            int randomLen = 7 - MACHINE_MIN_LEN.intValue();
            buf.append(innerRandom(randomLen));
            return Long.parseLong(buf.toString());
        } else {
            throw new RuntimeException("起始时间太小, 要更新了!");
        }
    }

    public static long longUid(long gene, int len, String... args) {
        long uid = longUid(args);
        uid = uid >> len << len;
        BitSet set = BitSet.valueOf(new long[]{gene});
        set.clear(len, 64);
        set.or(BitSet.valueOf(new long[]{uid}));
        return set.toLongArray()[0];
    }

    public static long longUid(String... args) {
        StringBuilder buf = new StringBuilder(System.currentTimeMillis() - START_DATE.getTime() + "");
        if(buf.length() <= 12 && buf.toString().compareTo("922337203684") <= 0) {
            String argsHash = BaseSecurityUtils.md5(true, args);
            if(StringUtils.isBlank(argsHash)) {
                buf.append(innerRandom(7));
            } else {
                buf.append(argsHash.substring(0, 7));
            }

            return Long.parseLong(buf.toString());
        } else {
            throw new RuntimeException("起始时间太小, 要更新了!");
        }
    }

    public static Date timeFromLongUid(long uid) {
        Long diff = Long.valueOf(Long.parseLong((uid + "").substring(0, 12)));
        return new Date(START_DATE.getTime() + diff.longValue());
    }

    public static String uuidSimple() {
        return uuid().replaceAll("-", "");
    }

    public static long timeFromUUID(String uuid) {
        if(StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("uuid为空");
        } else {
            if(!uuid.contains("-")) {
                StringBuilder buf = new StringBuilder();
                buf.append(uuid.substring(0, 8)).append("-").append(uuid.substring(8, 12)).append("-").append(uuid.substring(12, 16)).append("-").append(uuid.substring(16, 20)).append("-").append(uuid.substring(20));
                uuid = buf.toString();
            }

            return TimeUUIDUtils.getTimeFromUUID(UUID.fromString(uuid));
        }
    }

    public static boolean contains(String src, String contained) {
        return contains(src, contained, "*#");
    }

    public static boolean contains(String src, String contained, String ignoreChars) {
        if(StringUtils.isBlank(ignoreChars)) {
            return src.contains(contained);
        } else {
            StringBuilder buf = new StringBuilder();

            for(int matcher = 0; matcher < contained.length(); ++matcher) {
                if(StringUtils.contains(ignoreChars, contained.charAt(matcher))) {
                    buf.append(".");
                } else {
                    buf.append(contained.charAt(matcher));
                }
            }

            Matcher var5 = Pattern.compile(buf.toString()).matcher(src);
            if(var5.find()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static String collapseWhiteSpace(String src) {
        return src == null?null:src.replaceAll("[\\s\\u00A0]{2,}", " ");
    }

    public static String substringAfter(String src, String separator) {
        String result = StringUtils.substringAfter(src, separator);
        return (String)StringUtils.defaultIfBlank(result, src);
    }

    public static String underScoreToCamel(String src, boolean firstLetterUpperCase) {
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            src = src.replaceAll("_\\d{1,}$", "");
            StringBuilder buf = new StringBuilder();
            Arrays.stream(src.split("_")).forEach((s) -> {
                buf.append(toCamel(s));
            });
            String result = buf.toString();
            if(!StringUtils.isBlank(result) && result.length() >= 2) {
                if(firstLetterUpperCase) {
                    result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
                } else {
                    result = Character.toLowerCase(result.charAt(0)) + result.substring(1);
                }

                return result;
            } else {
                return result;
            }
        }
    }

    public static String mosaic(String text, JsonMosaic mosaic, char mosaicChar) {
        if(!StringUtils.isBlank(text) && mosaic.start() < text.length()) {
            if(mosaic.end() != 0) {
                if(mosaic.end() < 0) {
                    text = mosaic(text, mosaic.start(), text.length() + mosaic.end(), '*');
                } else {
                    text = mosaic(text, mosaic.start(), mosaic.end(), '*');
                }
            } else if(mosaic.length() != 0) {
                text = mosaic(text, mosaic.start(), mosaic.start() + mosaic.length(), '*');
            } else {
                text = mosaic(text, mosaic.start(), mosaic.start() + text.length(), '*');
            }

            return text;
        } else {
            return text;
        }
    }

    public static String mosaic(String src, int startInclusive, int endExclusive, char mosaicChar) {
        if(StringUtils.isBlank(src)) {
            return src;
        } else {
            if(endExclusive > src.length()) {
                endExclusive = src.length();
            }

            Preconditions.checkArgument(endExclusive >= startInclusive, "endExclusive必须大于startInclusive");
            Preconditions.checkArgument(startInclusive >= 0, "startInclusive不能小于0");
            StringBuilder buf = new StringBuilder();
            if(startInclusive > 0) {
                buf.append(src.substring(0, startInclusive));
            }

            for(int i = startInclusive; i < endExclusive; ++i) {
                buf.append(mosaicChar);
            }

            buf.append(src.substring(endExclusive));
            return buf.toString();
        }
    }

    public static String joinIgnoreBlank(String sep, String... args) {
        Preconditions.checkArgument(sep != null, "分隔符不能为null");
        StringBuilder buf = new StringBuilder();
        int index = 0;
        String[] var4 = args;
        int var5 = args.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String arg = var4[var6];
            if(StringUtils.isNotBlank(arg)) {
                if(index != 0) {
                    buf.append(sep);
                }

                buf.append(arg);
                ++index;
            }
        }

        return buf.toString();
    }

    public static String singularize(String plural) {
        return PLURALS.contains(plural)?(String)SINGULARS.get(PLURALS.indexOf(plural)):Inflector.getInstance().singularize(plural);
    }

    public static String pluralize(String singular) {
        return SINGULARS.contains(singular)?(String)PLURALS.get(SINGULARS.indexOf(singular)):Inflector.getInstance().pluralize(singular);
    }

    private static String toCamel(String word) {
        if(StringUtils.isBlank(word)) {
            return word;
        } else {
            word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            StringBuilder buf = new StringBuilder();
            Matcher matcher = Pattern.compile(PATTERN).matcher(word);
            int end = 0;

            while(matcher.find()) {
                int result = matcher.start();
                end = matcher.end();
                buf.append(word.substring(0, result + 1));
                buf.append(matcher.group().substring(1).toLowerCase());
            }

            buf.append(word.substring(end));
            String result1 = buf.toString();
            return result1.substring(0, result1.length() - 1) + Character.toLowerCase(result1.charAt(result1.length() - 1));
        }
    }

    private static String innerRandom(int len) {
        long randomLong = SMALL_RANDOM.getAndIncrement();
        if(randomLong > 4611686018427387903L) {
            SMALL_RANDOM.set(0L);
        }

        String random = String.valueOf(randomLong);
        return random.length() < len?StringUtils.leftPad(random, len, "0"):random.substring(0, len);
    }
}
