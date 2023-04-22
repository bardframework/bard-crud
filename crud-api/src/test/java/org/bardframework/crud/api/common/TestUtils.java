package org.bardframework.crud.api.common;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.form.model.filter.Filter;
import org.bardframework.form.model.filter.RangeFilter;
import org.bardframework.form.model.filter.StringFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/30/17.
 */
@Slf4j
@UtilityClass
public final class TestUtils {

    public static <C extends BaseCriteria<?>> String toQueryParam(C criteria)
            throws ReflectiveOperationException {
        if (null == criteria) {
            return "";
        }
        List<String> params = new ArrayList<>();
        for (Field field : criteria.getClass().getDeclaredFields()) {
            Object value = ReflectionUtils.getPropertyValue(criteria, field.getName());
            if (value instanceof Filter<?, ?> filter) {
                params.addAll(TestUtils.toQueryParam(filter, field.getName()));
            }
            if (value instanceof RangeFilter<?, ?> rangeFilter) {
                params.addAll(TestUtils.toQueryParam(rangeFilter, field.getName()));
            }
            if (value instanceof StringFilter stringFilter) {
                params.addAll(TestUtils.toQueryParam(stringFilter, field.getName()));
            }
        }
        return String.join("&", params);
    }

    public static List<String> toQueryParam(StringFilter filter, String name) {
        if (null == filter) {
            return Collections.emptyList();
        }
        List<String> params = new ArrayList<>();
        if (null != filter.getContains()) {
            params.add("%s.contains=%s".formatted(name, filter.getEquals()));
        }
        if (null != filter.getDoesNotContain()) {
            params.add("%s.doesNotContain=%s".formatted(name, filter.getEquals()));
        }
        if (null != filter.getStartWith()) {
            params.add("%s.startWith=%s".formatted(name, filter.getEquals()));
        }
        if (null != filter.getEndWith()) {
            params.add("%s.endWith=%s".formatted(name, filter.getEquals()));
        }
        return params;
    }

    public static List<String> toQueryParam(RangeFilter<?, ?> filter, String name) {
        if (null == filter) {
            return Collections.emptyList();
        }
        List<String> params = new ArrayList<>();
        if (null != filter.getFrom()) {
            params.add("%s.from=%s".formatted(name, filter.getEquals()));
        }
        if (null != filter.getTo()) {
            params.add("%s.to=%s".formatted(name, filter.getEquals()));
        }
        return params;
    }

    public static List<String> toQueryParam(Filter<?, ?> filter, String name) {
        if (null == filter) {
            return Collections.emptyList();
        }
        List<String> params = new ArrayList<>();
        if (null != filter.getEquals()) {
            params.add("%s.equals=%s".formatted(name, filter.getEquals()));
        }
        if (null != filter.getNotEquals()) {
            params.add("%s.notEquals=%s".formatted(name, filter.getNotEquals()));
        }
        if (null != filter.getSpecified()) {
            params.add("%s.specified=%s".formatted(name, filter.getSpecified()));
        }
        if (null != filter.getIn()) {
            for (Object id : filter.getIn()) {
                params.add("%s.in=%s".formatted(name, id));
            }
        }
        if (null != filter.getNotIn()) {
            for (Object id : filter.getNotIn()) {
                params.add("%s.notIn=%s".formatted(name, id));
            }
        }
        return params;
    }
}
