package org.bardframework.crud.impl.querydsl.utils;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bardframework.crud.api.filter.Filter;
import org.bardframework.crud.api.filter.RangeFilter;
import org.bardframework.crud.api.filter.StringFilter;

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

    public static void applyFilter(SQLQuery<?> query, StringFilter filter, StringPath path) {
        if (null == filter) {
            return;
        }
        QueryDslUtils.applyFilter(query, (Filter<String>) filter, path);
        if (StringUtils.isNotBlank(filter.getContains())) {
            query.where(path.likeIgnoreCase("%" + filter.getContains() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getDoesNotContain())) {
            query.where(path.notLike("%" + filter.getDoesNotContain() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getStartWith())) {
            query.where(path.likeIgnoreCase(filter.getStartWith() + "%"));
        }
        if (StringUtils.isNotBlank(filter.getEndWith())) {
            query.where(path.notLike("%" + filter.getDoesNotContain()));
        }
    }

    public static <T extends Comparable<? super T>> void applyFilter(SQLQuery<?> query, RangeFilter<T> filter, ComparableExpression<T> path) {
        if (null == filter) {
            return;
        }
        QueryDslUtils.applyFilter(query, (Filter<T>) filter, path);
        if (filter.getGreaterThan() != null) {
            query.where(path.gt(filter.getGreaterThan()));
        }
        if (filter.getGreaterThanOrEqual() != null) {
            query.where(path.goe(filter.getGreaterThanOrEqual()));
        }
        if (filter.getLessThan() != null) {
            query.where(path.lt(filter.getLessThan()));
        }
        if (filter.getLessThanOrEqual() != null) {
            query.where(path.loe(filter.getLessThanOrEqual()));
        }
    }

    public static <T extends Comparable<? super T>> void applyFilter(SQLQuery<?> query, Filter<T> filter, ComparableExpression<T> path) {
        if (null == filter) {
            return;
        }
        if (null != filter.getEquals()) {
            query.where(path.eq(filter.getEquals()));
        }
        if (null != filter.getNotEquals()) {
            query.where(path.ne(filter.getNotEquals()));
        }
        if (CollectionUtils.isNotEmpty(filter.getIn())) {
            query.where(path.in(filter.getIn()));
        }
        if (CollectionUtils.isNotEmpty(filter.getNotIn())) {
            query.where(path.notIn(filter.getNotIn()));
        }
        if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                query.where(path.isNotNull());
            } else {
                query.where(path.isNull());
            }
        }
    }
}
