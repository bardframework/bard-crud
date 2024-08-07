package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import lombok.Getter;

@Getter
public class OrderField {
    private final ComparableExpressionBase<?> column;
    private final Order order;

    public OrderField(ComparableExpressionBase<?> column, Order order) {
        this.column = column;
        this.order = order;
    }

    public OrderSpecifier<?> getColumnDirection() {
        if (order == Order.ASC) {
            return column.asc();
        }
        return column.desc();
    }
}
