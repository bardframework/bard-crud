package org.bardframework.crud.impl.querydsl.utils;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.*;
import org.bardframework.crud.api.filter.Filter;
import org.bardframework.crud.api.filter.RangeFilter;
import org.bardframework.crud.api.filter.StringFilter;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/30/17.
 */
public final class QueryDslUtils {

    public static <T> Expression<T> bean(String alias, Class<T> type, Expression<?>... exprs) {
        return ExpressionUtils.as(Projections.bean(type, exprs).skipNulls(), alias);
    }

    public static <T> QBean<T> bean(Class<T> type, Expression<?>... exprs) {
        return Projections.bean(type, exprs);
    }


    public static BooleanExpression buildQuery(StringFilter filter, StringExpression expression) {
        if (filter.getEquals() != null) {
            return expression.eq(filter.getEquals());
        } else if (filter.getIn() != null) {
            return expression.in(filter.getIn());
        } else if (filter.getContains() != null) {
//            return likeUpperSpecification(metaclassFunction, filter.getContains());
            return expression.likeIgnoreCase("%" + filter.getContains() + "%");
        } else if (filter.getDoesNotContain() != null) {
            return expression.notLike("%" + filter.getDoesNotContain() + "%");
        } else if (filter.getNotEquals() != null) {
            return expression.ne(filter.getNotEquals());
        } else if (filter.getNotIn() != null) {
            return expression.notIn(filter.getNotIn());
        } else if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                return expression.isNotNull();
            } else {
                return expression.isNull();
            }
        }
        return Expressions.asBoolean(true).isTrue();
    }

    public static <X extends Comparable<? super X>> BooleanExpression buildQuery(RangeFilter<X> filter, ComparableExpression<X> expression) {
        BooleanExpression expr = QueryDslUtils.buildQueryInternal(filter, expression);

        if (filter.getGreaterThan() != null) {
            expr = expr.and(expression.gt(filter.getGreaterThan()));
        }
        if (filter.getGreaterThanOrEqual() != null) {
            expr = expr.and(expression.goe(filter.getGreaterThanOrEqual()));
        }
        if (filter.getLessThan() != null) {
            expr = expr.and(expression.lt(filter.getLessThan()));
        }
        if (filter.getLessThanOrEqual() != null) {
            expr = expr.and(expression.loe(filter.getLessThanOrEqual()));
        }
        return expr;
    }

    public static <X extends Comparable<? super X>> BooleanExpression buildQueryInternal(RangeFilter<X> filter, ComparableExpressionBase<X> expression) {
        if (filter.getEquals() != null) {
            return expression.eq(filter.getEquals());
        } else if (filter.getIn() != null) {
            return expression.in(filter.getIn());
        }

        BooleanExpression expr = Expressions.asBoolean(true);
        if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                expr.and(expression.isNotNull());
            } else {
                expr.and(expression.isNull());
            }
        }
        if (filter.getNotEquals() != null) {
            expr.and(expression.ne(filter.getNotEquals()));
        } else if (filter.getNotIn() != null) {
            expr.and(expression.notIn(filter.getNotIn()));
        }
        return expr;
    }

    public static <X extends Number & Comparable<? super X>> BooleanExpression buildQuery(RangeFilter<X> filter, NumberExpression<X> expression) {
        BooleanExpression expr = QueryDslUtils.buildQueryInternal(filter, expression);

        if (filter.getGreaterThan() != null) {
            expr.and(expression.gt(filter.getGreaterThan()));
        }
        if (filter.getGreaterThanOrEqual() != null) {
            expr.and(expression.goe(filter.getGreaterThanOrEqual()));
        }
        if (filter.getLessThan() != null) {
            expr.and(expression.lt(filter.getLessThan()));
        }
        if (filter.getLessThanOrEqual() != null) {
            expr.and(expression.loe(filter.getLessThanOrEqual()));
        }
        return expr;
    }

    public static <X extends Comparable<? super X>> BooleanExpression buildQuery(Filter<X> filter, ComparableExpression<X> expression) {
        if (filter.getEquals() != null) {
            return expression.eq(filter.getEquals());
        } else if (filter.getIn() != null) {
            return expression.in(filter.getIn());
        } else if (filter.getNotEquals() != null) {
            return expression.ne(filter.getNotEquals());
        } else if (filter.getNotIn() != null) {
            return expression.notIn(filter.getNotIn());
        } else if (filter.getSpecified() != null) {
            if (filter.getSpecified()) {
                return expression.isNotNull();
            } else {
                return expression.isNull();
            }
        }
        return Expressions.asBoolean(true).isTrue();
    }

}
