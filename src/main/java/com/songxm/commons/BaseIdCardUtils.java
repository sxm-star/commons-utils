package com.songxm.commons;
import com.google.common.collect.ImmutableMap;
import com.songxm.commons.model.DistrictInfo;
import com.songxm.commons.model.IdCardInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class BaseIdCardUtils {
    private static Map<String, String> provinces = new ImmutableMap.Builder<String, String>().put("11", "北京市").put("12", "天津市").put("13", "河北省").put("14", "山西省").put("15", "内蒙古自治区").put("21", "辽宁省").put("22", "吉林省").put("23", "黑龙江省").put("31", "上海市").put("32", "江苏省").put("33", "浙江省").put("34", "安徽省").put("35", "福建省").put("36", "江西省").put("37", "山东省").put("41", "河南省").put("42", "湖北省").put("43", "湖南省").put("44", "广东省").put("45", "广西壮族自治区").put("46", "海南省").put("50", "重庆市").put("51", "四川省").put("52", "贵州省").put("53", "云南省").put("54", "西藏自治区").put("61", "陕西省").put("62", "甘肃省").put("63", "青海省").put("64", "宁夏回族自治区").put("65", "新疆维吾尔自治区").put("71", "台湾省").put("81", "香港特别行政区").put("82", "澳门特别行政区").put("91", "国外").build();
    private static int[] powers = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static Map<String, DistrictInfo> districtInfoMap = new HashMap<>(3300);

    public BaseIdCardUtils() {
    }

    public static IdCardInfo parseIdCard(String idCard) {
        return parseIdCard(idCard, Boolean.valueOf(true));
    }

    public static IdCardInfo parseIdCard(String idCard, Boolean check) {
        if(!StringUtils.isBlank(idCard) && (idCard.length() == 15 || idCard.length() == 18)) {
            if(check.booleanValue() && !Pattern.matches("(\\d{15})|(\\d{17}(\\d|x|X))", idCard)) {
                return null;
            } else {
                IdCardInfo info = new IdCardInfo();
                DistrictInfo districtInfo = districtInfo(idCard.substring(0, 6));
                info.setProvince(districtInfo.getProvince());
                info.setCity(districtInfo.getCity());
                info.setCounty(districtInfo.getCounty());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                byte index = 14;
                if(idCard.length() == 15) {
                    dateFormat = new SimpleDateFormat("yyMMdd");
                    index = 12;
                }

                String birthDate = idCard.substring(6, index);
                if(StringUtils.isNumeric(birthDate)) {
                    try {
                        Date sGender = dateFormat.parse(birthDate);
                        String cardSum = dateFormat.format(sGender);
                        if(!cardSum.equals(birthDate)) {
                            return null;
                        }

                        info.setBirthDate(BaseDateUtils.toDateFormat(sGender));
                        info.setAge(Integer.valueOf(BaseDateUtils.compareYear(new Date(), sGender)));
                    } catch (ParseException var12) {
                        return null;
                    }
                }

                String var13 = idCard.substring(index + 2, index + 3);
                if(StringUtils.isNumeric(var13)) {
                    Integer var14 = Integer.valueOf(idCard.substring(index + 2, index + 3));
                    if((var14.intValue() & 1) == 1) {
                        info.setMale(true);
                    } else {
                        info.setMale(false);
                    }
                }

                if(check.booleanValue() && idCard.length() == 18) {
                    int var15 = 0;
                    char[] cardChars = idCard.substring(0, 17).toCharArray();

                    int checkCode;
                    for(checkCode = 0; checkCode < 17; ++checkCode) {
                        var15 += Integer.parseInt(String.valueOf(cardChars[checkCode])) * powers[checkCode];
                    }

                    checkCode = (12 - var15 % 11) % 11;
                    String sCheckCode = checkCode != 10?checkCode + "":"X";
                    if(!sCheckCode.equalsIgnoreCase(idCard.substring(17))) {
                        return null;
                    }
                }

                return info;
            }
        } else {
            return null;
        }
    }

    private static DistrictInfo districtInfo(String code) {
        if(districtInfoMap.containsKey(code)) {
            return (DistrictInfo)districtInfoMap.get(code);
        } else {
            DistrictInfo districtInfo = new DistrictInfo();
            code = code.substring(0, 2);
            if(NumberUtils.isDigits(code) && provinces.containsKey(code)) {
                districtInfo.setProvince((String)provinces.get(code));
            }

            return districtInfo;
        }
    }

    public static void main(String[] args) throws Exception {
        StringBuilder buf = new StringBuilder();
        ArrayList<String> keys = new ArrayList<>(districtInfoMap.keySet());
        Collections.sort(keys);
        keys.forEach((key) -> {
            DistrictInfo info = (DistrictInfo)districtInfoMap.get(key);
            buf.append(key).append("={\"province\":\"").append(info.getProvince()).append("\"");
            if(StringUtils.isNotBlank(info.getCity()) && !"null".equals(info.getCity())) {
                buf.append(",\"city\":\"").append(info.getCity()).append("\"");
            } else {
                buf.append(",\"city\":\"").append(info.getProvince().replaceFirst("省$", "")).append("\"");
            }

            if(StringUtils.isNotBlank(info.getCounty())) {
                buf.append(",\"county\":\"").append(info.getCounty()).append("\"");
            }

            buf.append("}\n");
        });
        System.out.println(buf.toString());
    }

    static {
        Properties prop = BasePropertiesUtils.load("district-info.properties");
        prop.entrySet().forEach((entry) -> {
            DistrictInfo var10000 = (DistrictInfo)districtInfoMap.put((String)entry.getKey(), BaseJsonUtils.readValue((String)entry.getValue(), DistrictInfo.class));
        });
    }
}
