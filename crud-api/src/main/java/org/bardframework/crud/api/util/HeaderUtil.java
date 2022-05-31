package org.bardframework.crud.api.util;

import org.apache.commons.lang3.StringUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for HTTP headers creation.
 */
public final class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private static final String APPLICATION_NAME = "formApp";

    private HeaderUtil() {
    }

    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-alert", message);
        headers.add("X-" + APPLICATION_NAME + "-params", param);
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
        return createAlert("A new " + entityName + " is created with identifier " + param, param);
    }

    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        return createAlert("A " + entityName + " is updated with identifier " + param, param);
    }

    public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
        return createAlert("A " + entityName + " is deleted with identifier " + param, param);
    }

    public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
        log.error("Entity processing failed, {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + APPLICATION_NAME + "-error", defaultMessage);
        headers.add("X-" + APPLICATION_NAME + "-params", entityName);
        return headers;
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
            if (StringUtils.isNotBlank(path)) {
                path = paths[i] + "." + path;
            } else {
                path = paths[i];
            }
            keys.add(0, path);
        }
        return keys;
    }
}
