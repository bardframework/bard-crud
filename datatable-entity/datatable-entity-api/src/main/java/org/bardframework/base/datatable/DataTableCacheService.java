package org.bardframework.base.datatable;

import org.bardframework.base.datatable.event.DataTableCacheStructureUpdated;
import org.bardframework.commons.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Vahid Zafari on 4/11/2017.
 */
@Service
public class DataTableCacheService {

    private static final Map<String, Map<String, DataTableCachedStructure>> cache = new ConcurrentHashMap<>();
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void setStructures(String userId, Collection<DataTableCachedStructure> cachedStructures) {
        cache.put(userId, new ConcurrentHashMap<>());
        cachedStructures.forEach(dataTableStructure -> cache.get(userId).put(dataTableStructure.getTable(), dataTableStructure));
    }

    public List<DataTableCachedStructure> getStructures(String userId) {
        if (!cache.containsKey(userId) || null == cache.get(userId)) {
            return Collections.EMPTY_LIST;
        }
        return cache.get(userId).values().stream().collect(Collectors.toList());
    }

    public void cacheFilter(String userId, String table, DataTableFilter filter) {
        cache.putIfAbsent(userId, new ConcurrentHashMap<>());
        DataTableCachedStructure cachedStructure = cache.get(userId).get(table);
        if (filter.isEmpty()) {
            if (null != cachedStructure) {
                filter.setPage(cachedStructure.getPage());
                filter.setCount(cachedStructure.getCount());
                filter.setHeaders(cachedStructure.getHeaders());
            }
        } else {
            if (null == cachedStructure) {
                cachedStructure = new DataTableCachedStructure(table);
            }
            cachedStructure.setPage(filter.getPage());
            cachedStructure.setCount(filter.getCount());
            cachedStructure.setQuery(filter.getQuery());
            cachedStructure.setHeaders(filter.getHeaders());
            cache.get(userId).put(table, cachedStructure);
        }
        eventPublisher.publishEvent(new DataTableCacheStructureUpdated(userId));
    }

    public DataTableFilter getStructure(String userId, String table, DataTableFilter<? extends DataTableFilter<?>> structure) {
        if (!cache.containsKey(userId)) {
            return structure;
        }
        DataTableCachedStructure cachedStructure = cache.get(userId).get(table);
        if (null == cachedStructure) {
            return structure;
        }
        structure.setQuery(cachedStructure.getQuery());
        structure.setPage(cachedStructure.getPage());
        structure.setCount(cachedStructure.getCount());
        if (CollectionUtils.isEmpty(cachedStructure.getHeaders())) {
            return structure;
        }
        Map<String, HeaderDto> columnMap = (Map<String, HeaderDto>) cachedStructure.getHeaders().stream().collect(Collectors.toMap(HeaderDto::getId, Function.identity()));
        structure.getHeaders().stream().filter(column -> columnMap.containsKey(column.getId())).forEach(
                column -> {
                    HeaderDto cachedColumn = columnMap.get(column.getId());
                    column.setQuery(cachedColumn.getQuery());
                    column.setMin(cachedColumn.getMin());
                    column.setMax(cachedColumn.getMax());
                    column.setSort(cachedColumn.getSort());
                    column.setVisible(cachedColumn.isVisible());
                    column.setSortSequence(cachedColumn.getSortSequence());
                    column.setSelected(cachedColumn.getSelected());
                }
        );
        return structure;
    }
}
