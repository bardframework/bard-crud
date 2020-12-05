//package org.bard.common.common.converter;
//
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//
///**
// * Created by Vahid Zafari(v.zafari@chmail.ir) on 7/12/2016.
// *
// * @see Utility#getFormatedTime(LocalTime)
// */
//@Component
//public class LocalDateTimeToTimeStringConverter implements Converter<LocalDateTime, String> {
//
//    @Override
//    public String convert(LocalDateTime value) {
//        return null == value ? null : Utility.getFormatedTime(value.toLocalTime());
//    }
//}
