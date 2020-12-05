package org.bardframework.crud.api;

import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.StringUtils;
import org.bardframework.crud.common.model.BaseData;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author v.zafari@chmail.ir
 */
public final class UtilityMethods {

    private static final String[] s_0_9 = {"صفر", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه"};
    private static final String[] s_10_19 = {"ده", "یا زده", "دوازده", "سیزده", "چهارده", "پا نزده", "شا نزده", "هفده", "هجده", "نوزده"};
    private static final String[] s_20_90 = {"بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود"};
    private static final String[] s_100_900 = {"صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"};
    private static final String[] s_Parts = {"هزار", "میلیون", "میلیارد", "تريليون"};
    private static final String SPLITTER = " و ";
    private static final String VERY_BIG = "تعریف نشده";
    private static final String NEGATIVE = "منفی";

    private UtilityMethods() {
    }


    /**
     * @param object
     * @return true if value of object be '1', 'on' or 'true', otherwise false.
     */
    public static boolean getBoolean(Object object) {
        if (object == null) {
            return false;
        }
        String value = object.toString().trim();
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    /**
     * تابع تبدیل عدد به حروف فارسی با پشتیبانی از اعداد منفی تا سقف تریلیون که
     * در صورت بزرگتر بودن عدد ارسالی عبارت تعریف نشده بر ‌میگردد
     *
     * @param number this number between [-999999999999999 ,999999999999999]
     * @return String
     */
    public static String numberToAlphabet(long number) {
        String tempStringNumberOrgin = String.valueOf(number);
        String tempStringNumber = tempStringNumberOrgin.replaceAll("-", StringUtils.EMPTY);

        if (number == 0) {
            return s_0_9[0];
        }

        int partCount = tempStringNumber.length() / 3;
        partCount += (tempStringNumber.length() % 3) > 0 ? 1 : 0;
        if (s_Parts.length < partCount - 1) {
            return VERY_BIG;
        }

        List<String> partFullString = new ArrayList<>();

        int lengthToSelectFirstPart = 0;
        for (int i = 0; i < partCount; i++) {
            String numberLength3;

            if (i == 0) {
                lengthToSelectFirstPart = tempStringNumber.length() - ((partCount - 1) * 3);
                numberLength3 = tempStringNumber.substring((i * 3), lengthToSelectFirstPart);
            } else {
                numberLength3 = tempStringNumber.substring(lengthToSelectFirstPart + ((i - 1) * 3)).substring(0, 3);
            }

            String numberIn3 = numberLength3;
            if (numberIn3.length() > 3) {
                return StringUtils.EMPTY;
            }

            switch (numberIn3.length()) {
                case 1:
                    numberIn3 = "00" + numberIn3;
                    break;
                case 2:
                    numberIn3 = "0" + numberIn3;
                    break;
                default:
                    numberIn3 = "";
            }

            String partInWord;

            int n1 = Integer.parseInt(numberIn3.substring(0, 1));
            int n2 = Integer.parseInt(numberIn3.substring(1, 2));
            int n3 = Integer.parseInt(numberIn3.substring(2, 3));

            partInWord = findPartInWord(n1, n2, n3);

            addPartFullString(partFullString, partInWord, partCount, i);
        }

        StringBuilder outString = new StringBuilder();

        for (String aPartFullString : partFullString) {
            outString.append(aPartFullString);
        }

        if ("-".equals(tempStringNumberOrgin.substring(0, 1))) {
            outString.append(NEGATIVE);
            outString.append(StringUtils.SPACE_CHAR);
            outString.append(outString);
        }

        return outString.toString();

    }

    private static String findPartInWord(int num1, int num2, int num3) {
        String part;
        if (num1 != 0) {
            part = partInWord(num1, num2, num3);
        } else {
            part = partInWordBig(num2, num3);
        }
        return part;
    }

    private static String partInWord(int num1, int num2, int num3) {
        String part;
        switch (num2) {
            case 0:
                if (num3 != 0) {
                    part = s_100_900[num1 - 1] + SPLITTER + s_0_9[num3];
                } else {
                    part = s_100_900[num1 - 1];
                }
                break;
            case 1:
                part = s_100_900[num1 - 1] + SPLITTER + s_10_19[num3];
                break;
            default:
                if (num3 != 0) {
                    part = s_100_900[num1 - 1] + SPLITTER + s_20_90[num2 - 2] + SPLITTER + s_0_9[num3];
                } else {
                    part = s_100_900[num1 - 1] + SPLITTER + s_20_90[num2 - 2];
                }
        }
        return part;
    }

    private static String partInWordBig(int num2, int num3) {
        String part;
        switch (num2) {
            case 0:
                if (num3 != 0) {
                    part = s_0_9[num3];
                } else {
                    part = StringUtils.EMPTY;
                }
                break;
            case 1:
                part = s_10_19[num3];
                break;
            default:
                if (num3 != 0) {
                    part = s_20_90[num2 - 2] + SPLITTER + s_0_9[num3];
                } else {
                    part = s_20_90[num2 - 2];
                }
        }
        return part;
    }

    private static void addPartFullString(List<String> partFullString, String partInWord, int partCount, int index) {
        int partIndex = (partCount - 2 - index);
        partIndex = Math.max(partIndex, 0);
        String partPreFix = s_Parts[partIndex];

        if (index == partCount - 1) {
            partPreFix = StringUtils.EMPTY;
        }
        if (index == 0) {
            if (StringUtils.EMPTY.equals(partInWord)) {
                partFullString.add(index, StringUtils.EMPTY);
            } else {
                partFullString.add(index, partInWord + " " + partPreFix);
            }
        } else {
            addParts(partFullString, partInWord, partPreFix, index);
        }
    }

    private static void addParts(List<String> partFullString, String partInWord, String partPreFix, int index) {
        if (!StringUtils.EMPTY.equals(partFullString.get(index - 1))) {
            if (!StringUtils.EMPTY.equals(partInWord)) {
                partFullString.add(index, SPLITTER + partInWord + StringUtils.SPACE + partPreFix);
            } else {
                partFullString.add(index, StringUtils.EMPTY);
            }
        } else {
            if (!StringUtils.EMPTY.equals(partInWord)) {
                partFullString.add(index, SPLITTER + partInWord + StringUtils.EMPTY + partPreFix);
            } else {
                partFullString.add(index, StringUtils.SPACE);
            }
        }
    }

    public static BaseData toModel(String id, String key, int sequence, MessageSource messageSource, Locale locale) {
        return new BaseData(id, translate(key, messageSource, locale), sequence);
    }

    public static BaseData toModel(String key, int sequence, MessageSource messageSource, Locale locale) {
        return toModel(key, key, sequence, messageSource, locale);
    }

    /**
     * convert enum to api data use class name + '.' + enum as id
     * translate class name + '.' + enum as name
     * use enum,ordinal() as sequence
     */
    public static <ENUM extends Enum<ENUM>> BaseData toModel(Enum<ENUM> anEnum, MessageSource messageSource, Locale locale) {
        return new BaseData(anEnum.getClass().getSimpleName() + "." + anEnum.name(), translate(anEnum, messageSource, locale), anEnum.ordinal());
    }

    public static BaseData toModel(boolean bool, MessageSource messageSource, Locale locale) {
        String key = String.valueOf(bool);
        return toModel(key, key, bool ? 1 : 2, messageSource, locale);
    }

    /**
     * translate class name + '.' + enum as key
     */
    public static String translate(Enum anEnum, MessageSource messageSource, Locale locale) {
        AssertionUtils.notNull(anEnum, "null enum not acceptable");
        return translate(anEnum.getClass().getSimpleName() + "." + anEnum.name(), messageSource, locale);
    }

    public static String translate(String key, MessageSource messageSource, Locale locale, Object... args) {
        AssertionUtils.hasText(key, "null key not acceptable");
        AssertionUtils.notNull(messageSource, "null messageSource not acceptable");
        AssertionUtils.notNull(locale, "null locale not acceptable");
        return messageSource.getMessage(key, args, key + "_" + locale.getLanguage(), locale);
    }

    public static String translate(List<String> keys, MessageSource messageSource, Locale locale) {
        AssertionUtils.notEmpty(keys, "null or empty keys not acceptable");
        for (String key : keys) {
            try {
                return messageSource.getMessage(key, null, locale);
            } catch (NoSuchMessageException e) {
                /*
                  do nothing, try next key.
                 */
            }
        }
        return keys.get(0) + "_" + locale.getLanguage();
    }

    public static String toMessageKey(String raw) {
        if (null == raw) {
            return null;
        }
        raw = raw.trim();
        StringBuilder builder = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (builder.length() > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * '.' separated path
     *
     * @return list of keys
     */
    public static List<String> toMessageKeys(String fullPath) {
        List<String> keys = new ArrayList<>();
        String[] paths = toMessageKey(fullPath).split("\\.");
        String path = StringUtils.EMPTY;
        for (int i = paths.length - 1; i >= 0; i--) {
            if (StringUtils.hasText(path)) {
                path = paths[i] + "." + path;
            } else {
                path = paths[i];
            }
            keys.add(0, path);
        }
        return keys;
    }

    public static String translateByteToKMGT(long length, MessageSource messageSource, Locale locale) {
        if (length < Math.pow(1024, 1)) {
            return length + " " + translate("byte", messageSource, locale);
        } else if (length < Math.pow(1024, 2)) {
            return length / Math.pow(1024, 1) + " " + translate("kilobyte", messageSource, locale);
        } else if (length < Math.pow(1024, 3)) {
            return length / Math.pow(1024, 2) + " " + translate("megabyte", messageSource, locale);
        } else if (length < Math.pow(1024, 4)) {
            return length / Math.pow(1024, 3) + " " + translate("gigabyte", messageSource, locale);
        } else if (length < Math.pow(1024, 5)) {
            return length / Math.pow(1024, 4) + " " + translate("terabyte", messageSource, locale);
        } else {
            throw new IllegalArgumentException("length is out of range, max supported range is tera byte ");
        }
    }

}
