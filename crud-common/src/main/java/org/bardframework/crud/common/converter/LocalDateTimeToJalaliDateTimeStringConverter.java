//package org.bard.common.common.converter;
//
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
///**
// * Created by Vahid Zafari(v.zafari@chmail.ir) on 7/12/2016.
// */
//@Component
//public class LocalDateTimeToJalaliDateTimeStringConverter implements Converter<LocalDateTime, String> {
//
//    @Override
//    public String convert(LocalDateTime value) {
//        return null == value ? null : Utility.convertMiladiToJalali(value.toLocalDate(), "/") + " " + Utility.getFormatedTime(value.toLocalTime());
//    }
//}
