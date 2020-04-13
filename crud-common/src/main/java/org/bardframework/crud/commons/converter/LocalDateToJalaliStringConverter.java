//package org.bard.commons.common.converter;
//
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
///**
// * Created by Vahid Zafari(v.zafari@chmail.ir) on 7/12/2016.
// *
// * @see Utility#convertMiladiToJalali(LocalDate)
// */
//@Component
//public class LocalDateToJalaliStringConverter implements Converter<LocalDate, String> {
//
//    @Override
//    public String convert(LocalDate value) {
//        return null == value ? null : Utility.convertMiladiToJalali(value, "/");
//    }
//}