package org.bardframework.crud.impl.querydsl.type;

import com.querydsl.sql.types.EnumByNameType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public abstract class EnumByNameTypeSafe<T extends Enum<T>> extends EnumByNameType<T> {

    private final List<T> enumsList;

    public EnumByNameTypeSafe(Class<T> type) {
        super(type);
        this.enumsList = EnumUtils.getEnumList(this.getReturnedClass());
    }

    public EnumByNameTypeSafe(int jdbcType, Class<T> type) {
        super(jdbcType, type);
        this.enumsList = EnumUtils.getEnumList(this.getReturnedClass());
    }

    @Override
    public T getValue(ResultSet resultSet, int startIndex) throws SQLException {
        String value = resultSet.getString(startIndex);
        try {
            return value != null ? Enum.valueOf(this.getReturnedClass(), value) : null;
        } catch (Exception e) {
            log.error("error converting value[{}] to enum [{}], valid values are: {}", value, this.getReturnedClass(), enumsList, e);
            return this.getValueOnConvertError();
        }
    }

    protected abstract T getValueOnConvertError();
}