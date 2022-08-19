package org.bardframework.crud.impl.querydsl.utils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bardframework.form.model.filter.*;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static <I extends Serializable> Predicate getPredicate(IdFilter<I> filter, Function<I, Predicate> equals, Supplier<Predicate> isNotNull, Supplier<Predicate> isNull) {
        if (null == filter) {
            return null;
        }
        BooleanBuilder builder = new BooleanBuilder();
        if (null != filter.getEquals()) {
            builder.and(equals.apply(filter.getEquals()));
        }
        if (null != filter.getNotEquals()) {
            builder.and(equals.apply(filter.getNotEquals()).not());
        }
        if (CollectionUtils.isNotEmpty(filter.getIn())) {
            BooleanBuilder inBuilder = new BooleanBuilder();
            for (I id : filter.getIn()) {
                inBuilder.or(equals.apply(id));
            }
            builder.and(inBuilder);
        }
        if (CollectionUtils.isNotEmpty(filter.getNotIn())) {
            BooleanBuilder notInBuilder = new BooleanBuilder();
            for (I id : filter.getNotIn()) {
                notInBuilder.or(equals.apply(id));
            }
            builder.and(notInBuilder.not());
        }
        if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                builder.and(isNotNull.get());
            } else {
                builder.and(isNull.get());
            }
        }
        return builder;
    }
}
