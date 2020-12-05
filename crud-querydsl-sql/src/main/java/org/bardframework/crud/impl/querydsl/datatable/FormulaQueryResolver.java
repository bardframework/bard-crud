package org.bardframework.crud.impl.querydsl.datatable;

import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vahid Zafari on 7/6/2017.
 */
public class FormulaQueryResolver {

    private static final Map<String, Map<String, SQLQuery<?>>> QUERIES = new HashMap<>();

    static {
        QUERIES.put("HRM_APPOINTMENT_REQUEST", Collections.singletonMap("status", SQLExpressions.select()));
        QUERIES.put("HRM_VIEW_ORGANIZING_EMPLOYEE_DETAIL", Collections.singletonMap("organizingStatus", SQLExpressions.select()));
        QUERIES.put("HRM_PCR", Collections.singletonMap("status", SQLExpressions.select()));
        QUERIES.put("HRM_TRIP_REQUEST", Collections.singletonMap("status", SQLExpressions.select()));
        QUERIES.put("HRM_RELOCATION_REQUEST", new HashMap<>());
        QUERIES.get("HRM_RELOCATION_REQUEST").put("status", SQLExpressions.select());
        QUERIES.put("HRM_TRIP_PERIOD", new HashMap<>());
        QUERIES.get("HRM_TRIP_PERIOD").put("status", SQLExpressions.select());
        QUERIES.get("HRM_TRIP_PERIOD").put("registrable", SQLExpressions.select());
    }

    public static SQLQuery<?> getQuery(String table, String property) {
        if (!QUERIES.containsKey(table)) {
            return null;
        }
        return QUERIES.get(table).get(property);
    }
}
