package org.bardframework.crud.impl.querydsl.datatable;

import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.bardframework.crud.api.datatable.HeaderAbstract;
import org.bardframework.crud.api.datatable.QueryType;
import org.bardframework.crud.api.util.HeaderUtil;

import static org.bardframework.crud.api.datatable.FilteringType.NONE;
import static org.bardframework.crud.api.datatable.FilteringType.*;
import static org.bardframework.crud.api.datatable.QueryType.*;

/**
 * Created by Vahid Zafari on 8/12/2016.
 */
public class HeaderQdsl extends HeaderAbstract {

    private SimpleExpression<?> queryPath;
    private DataLimitationChecker checker;

    public void setQueryPath(Class<?> mainClazz, Class<?> valueType, String fieldName, String tableName, SimpleExpression<?> queryPath) {
        if (this.queryType == QueryType.NONE) {
            if (this.filterType != NONE) {
                LOGGER.error("invalid HeaderAbstract definition on '{}'@'{}', when query type is {}, filtering type must be {}", path, mainClazz.getSimpleName(), QueryType.NONE, NONE);
                throw new IllegalStateException("filtering and query type must be NONE or not NONE together");
            }
            this.queryPath = null;
        } else if (this.queryType == DB_COLUMN || this.queryType == HIERARCHY) {
            this.queryPath = queryPath;
        } else if (this.queryType == FORMULA) {
            if (null == FormulaQueryResolver.getQuery(tableName, fieldName)) {
                LOGGER.error("'{}' in '{}' annotated with @HeaderAbstract with '{}' query type, but queryResolver not set.", path, mainClazz, FORMULA);
                throw new IllegalArgumentException("filed that annotated with @HeaderAbstract with  'FORMULA' query type must set queryResolver.");
            }
            if (!(this.filterType == MULTI_SELECT || this.filterType == SINGLE_SELECT)) {
                LOGGER.error("when choose query type as '{}', only '{}' and '{}' are valid for filtering type.", FORMULA, MULTI_SELECT, SINGLE_SELECT);
                throw new IllegalArgumentException("invalid combination of query type and filtering type.");
            }
            if (this.searchable || this.sortable) {
                LOGGER.error("'{}' in '{}' annotated with @HeaderAbstract with '{}' type, can't be searchable, sortable", path, mainClazz, FORMULA);
                throw new IllegalArgumentException("filed annotated with @HeaderAbstract with 'ENUM' type, can't be searchable or sortable");
            }
            this.checker = new DataLimitationChecker(valueType);
            this.queryPath = null;
        }
    }

    public void init(Class<?> mainClazz, Class<?> valueType, String fieldName, String tableName, SimpleExpression<?> queryPath) {
        this.validate(mainClazz);
        this.determineConverters(valueType);
        this.setQueryPath(mainClazz, valueType, fieldName, tableName, queryPath);
        this.messageKeys = HeaderUtil.toMessageKeys(mainClazz.getSimpleName() + "." + path);
    }

    public <T extends Comparable> ComparableExpression<T> getMinMaxPath() {
        return (ComparableExpression<T>) queryPath;
    }

    public SimpleExpression<?> getQueryPath() {
        return queryPath;
    }

    public StringPath getSearchPath() {
        return (StringPath) queryPath;
    }

    public DataLimitationChecker getChecker() {
        return checker;
    }
}
