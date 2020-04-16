package org.bardframework.base.utils;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;

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
}