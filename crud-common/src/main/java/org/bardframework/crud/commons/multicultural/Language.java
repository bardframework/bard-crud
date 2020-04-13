package org.bardframework.crud.commons.multicultural;

/**
 * Created by Vahid Zafari on 4/21/2017.
 */
public enum Language {
    PERSIAN("fa", true), ENGLISH("en", false);
    private final String locale;
    private final boolean rtl;

    Language(String locale, boolean rtl) {
        this.locale = locale;
        this.rtl = rtl;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isRtl() {
        return rtl;
    }
}
