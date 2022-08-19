package org.bardframework.crud.impl.querydsl.utils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bardframework.form.model.filter.Filter;
import org.bardframework.form.model.filter.NumberRangeFilter;
import org.bardframework.form.model.filter.RangeFilter;
import org.bardframework.form.model.filter.StringFilter;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/30/17.
 */
public final class QueryDslUtils {
    private QueryDslUtils() {
        /*
            prevent instantiation
        */
    }

    public static <T> Expression<T> bean(String alias, Class<T> type, Expression<?>... expressions) {
        return ExpressionUtils.as(Projections.bean(type, expressions).skipNulls(), alias);
    }

    public static <T> QBean<T> bean(Class<T> type, Expression<?>... expressions) {
        return Projections.bean(type, expressions);
    }

    public static Predicate getPredicate(StringFilter filter, StringPath path) {
        if (null == filter) {
            return null;
        }
        BooleanBuilder builder = new BooleanBuilder(QueryDslUtils.getPredicate((Filter<String, ?>) filter, path));
        if (StringUtils.isNotBlank(filter.getContains())) {
            builder.and(path.likeIgnoreCase("%" + filter.getContains() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getDoesNotContain())) {
            builder.and(path.notLike("%" + filter.getDoesNotContain() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getStartWith())) {
            builder.and(path.likeIgnoreCase(filter.getStartWith() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getEndWith())) {
            builder.and(path.notLike("%" + filter.getDoesNotContain()));
        }
        return builder;
    }

    public static <T extends Number & Comparable<T>, F extends NumberRangeFilter<T, F>> Predicate getPredicate(NumberRangeFilter<T, F> filter, NumberExpression<T> path) {
        if (null == filter) {
            return null;
        }
        BooleanBuilder builder = new BooleanBuilder(QueryDslUtils.getPredicate((Filter<T, F>) filter, path));
        if (filter.getFrom() != null) {
            builder.and(path.goe(filter.getFrom()));
        }
        if (filter.getTo() != null) {
            builder.and(path.loe(filter.getTo()));
        }
        return builder;
    }

    public static <T extends Comparable<? super T>> Predicate getPredicate(RangeFilter<T, ?> filter, ComparableExpression<T> path) {
        if (null == filter) {
            return null;
        }
        BooleanBuilder builder = new BooleanBuilder(QueryDslUtils.getPredicate((Filter<T, ?>) filter, path));
        if (filter.getFrom() != null) {
            builder.and(path.goe(filter.getFrom()));
        }
        if (filter.getTo() != null) {
            builder.and(path.loe(filter.getTo()));
        }
        return builder;
    }

    public static <T extends Comparable<? super T>> Predicate getPredicate(Filter<T, ?> filter, SimpleExpression<T> path) {
        if (null == filter) {
            return null;
        }
        BooleanBuilder builder = new BooleanBuilder();
        if (null != filter.getEquals()) {
            builder.and(path.eq(filter.getEquals()));
        }
        if (null != filter.getNotEquals()) {
            builder.and(path.ne(filter.getNotEquals()));
        }
        if (CollectionUtils.isNotEmpty(filter.getIn())) {
            builder.and(path.in(filter.getIn()));
        }
        if (CollectionUtils.isNotEmpty(filter.getNotIn())) {
            builder.and(path.notIn(filter.getNotIn()));
        }
        if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                builder.and(path.isNotNull());
            } else {
                builder.and(path.isNull());
            }
        }
        return builder;
    }
}
