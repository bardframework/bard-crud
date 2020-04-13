package org.bardframework.base.datatable;

import org.bardframework.commons.reflection.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Vahid Zafari on 8/12/2016.
 */
@Component
public class DataTableUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTableUtils.class);
    //    private final Map<String, Map<String, Path<?>>> TABLE_COLUMN_MAP = new ConcurrentHashMap<>();
    private final Map<Class<?>, DataTableStructure> structureMap = new ConcurrentHashMap<>();
//    private static final DefaultNamingStrategy NAMING_STRATEGY = new DefaultNamingStrategy();

    @Autowired
    private List<DataTableStructure> structures;

    @PostConstruct
    private void init()
            throws NoSuchFieldException, IllegalAccessException {
        initTableMap();
        Field field;
        Method method;
        String tableName, columnName, fieldName;
//        SimpleExpression<?> queryPath;
        Class<?> valueType, containerClazz;
        for (DataTableStructure structure : this.structures) {
            structureMap.put(structure.getClazz(), structure);
            for (HeaderAbstract header : structure.getHeaders()) {
                columnName = null;
                //                queryPath = null;
                try {
                    containerClazz = ReflectionUtils.getContainerClassByGetter(structure.getClazz(), header.getPath());
                } catch (NoSuchMethodException e) {
                    LOGGER.error("error fetching getters {} from {}.", header.getPath(), structure.getClazz(), e);
                    throw new IllegalArgumentException("can't find field in path " + header.getPath() + " from class " + structure.getClazz().getSimpleName());
                }
//                tableName = this.getTableName(containerClazz);
                field = ReflectionUtils.getField(structure.getClazz(), header.getPath());
                if (null == field) {
                    try {
                        method = ReflectionUtils.getGetterMethod(structure.getClazz(), header.getPath());
                    } catch (NoSuchMethodException e) {
                        LOGGER.error("error fetching getters {} from {}.", header.getPath(), structure.getClazz(), e);
                        throw new IllegalArgumentException("can't find field in path " + header.getPath() + " from class " + structure.getClazz().getSimpleName());
                    }
                    fieldName = ReflectionUtils.methodToFieldName(method);
                } else {
                    //                    columnName = this.getColumnName(field);
                    /*
                    column name in @Transient fields is null
                     */
                    if (null != columnName) {
//                        columnName = NAMING_STRATEGY.getPropertyName(columnName, null);
//                        queryPath = (SimpleExpression<?>) this.get(tableName, columnName);
                    }
                }
//                header.init(structure.getClazz(), valueType, fieldName, tableName, queryPath);
            }
        }
    }

    private void initTableMap()
            throws NoSuchFieldException, IllegalAccessException {
//        Reflections reflections = new Reflections("org.bardframework.sama.entity");
//        Set<Class<? extends RelationalPathBase>> classes = reflections.getSubTypesOf(RelationalPathBase.class);
//        for (Class<? extends RelationalPathBase> aClass : classes) {
//            Field field = aClass.getField(ReflectionUtils.lowerCaseFirstLetter(aClass.getSimpleName().substring(1)));
//            RelationalPathBase<?> value = (RelationalPathBase) field.get(null);
//            TABLE_COLUMN_MAP.putIfAbsent(value.getTableName(), new HashMap<>());
//            for (Path<?> path : value.getColumns()) {
//                TABLE_COLUMN_MAP.get(value.getTableName()).putIfAbsent(path.getMetadata().getName(), path);
//            }
//        }
    }

//    public Path<?> get(String table, String column) {
//        if (null == TABLE_COLUMN_MAP) {
//            try {
//                initTableMap();
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                throw new IllegalStateException("can't create table column map from query dsl generated entities", e);
//            }
//        }
//        if (!TABLE_COLUMN_MAP.containsKey(table) || null == TABLE_COLUMN_MAP.get(table).get(column)) {
//            throw new NotExistException("column of table", table, column);
//        }
//        return TABLE_COLUMN_MAP.get(table).get(column);
//    }

    public List<HeaderAbstract> getHeaders(Class<?> clazz) {
        if (!structureMap.containsKey(clazz)) {
            LOGGER.error("no '{}' found with key '{}'.", DataTableStructure.class.getSimpleName(), clazz.getSimpleName());
            throw new IllegalArgumentException("no structure found with key: " + clazz.getSimpleName());
        }
        return structureMap.get(clazz).getHeaders();
    }

//    private String getTableName(Class<?> clazz) {
//        if (clazz.isAnnotationPresent(JsonAutoDetect.class)) {
//            return null;
//        }
//        Table table = clazz.getAnnotation(Table.class);
//        if (null == table || StringUtils.hasNotText(table.name())) {
//            throw new NotExistException("table name", clazz);
//        }
//        return table.name();
//    }
//
//    private String getColumnName(Field field) {
//        if (null != field.getAnnotation(Transient.class)) {
//            return null;
//        }
//        javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
//        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
//        if ((null == column || StringUtils.hasNotText(column.name())) && (null == joinColumn || StringUtils.hasNotText(joinColumn.name()))) {
//            throw new IllegalStateException("can't determine column name from field, each field present in headers and has DB_COLUMN query type must annotated with @Column or @JoinColumn with not empty name\n" + field);
//        }
//        return null != column && StringUtils.hasText(column.name()) ? column.name() : joinColumn.name();
//    }
}
