package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public class OrderField {
    private final ComparableExpressionBase<?> column;
    private final Order order;

    public OrderField(ComparableExpressionBase<?> column, Order order) {
        this.column = column;
        this.order = order;
    }

    public ComparableExpressionBase<?> getColumn() {
        return column;
    }

    public OrderSpecifier<?> getColumnDirection() {
        return order == Order.ASC ? column.asc() : column.desc();
    }
}
