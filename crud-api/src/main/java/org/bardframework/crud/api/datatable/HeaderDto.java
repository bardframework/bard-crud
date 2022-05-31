package org.bardframework.crud.api.datatable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bardframework.crud.api.util.HeaderUtil;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author v.zafari@chmail.ir 04 May 2016
 */
public class HeaderDto {

    private String id;
    private String name;
    private FilteringType filteringType;
    private ResultType resultType;
    private String mask;
    private boolean sortable;
    private Boolean visible;
    private boolean searchable;
    private List<BaseData> values;
    @JsonIgnore
    private List<String> messageKeys;

    //Filter
    private String query;
    private List<String> selected;
    private String min;
    private String max;

    private Sort sort;
    private int sortSequence;

    public HeaderDto() {
    }

    public HeaderDto(HeaderAbstract header, List<BaseData> values) {
        this(header);
        this.values = values;
    }

    public HeaderDto(HeaderAbstract header) {
        this.id = header.getPath();
        this.filteringType = header.getFilterType();
        this.resultType = header.getResultType();
        this.mask = header.getMask();
        this.sortable = header.isSortable();
        this.visible = header.isVisible();
        this.searchable = header.isSearchable();
        this.sort = header.getSort();
        this.messageKeys = header.getMessageKeys();
    }

    public void resolveName(MessageSource messageSource, Locale locale) {
        this.name = HeaderUtil.translate(this.messageKeys, messageSource, locale);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public FilteringType getFilteringType() {
        return filteringType;
    }

    public void setFilteringType(FilteringType filteringType) {
        this.filteringType = filteringType;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public Boolean isVisible() {
        return visible;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public List<BaseData> getValues() {
        /**
         * produce exception in section sorting!!!
         */
        try {
            return values.stream().sorted().collect(Collectors.toList());
        } catch (Exception e) {
            return values;
        }
    }

    public void setValues(List<BaseData> values) {
        this.values = values;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getSortSequence() {
        return sortSequence;
    }

    public void setSortSequence(int sortSequence) {
        this.sortSequence = sortSequence;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "\nHeaderModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + resultType +
                ", sort=" + sort +
                ", mask='" + mask + '\'' +
                ", sortable=" + sortable +
                ", visible=" + visible +
                ", searchable=" + searchable +
                '}';
    }

    @JsonIgnore
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(selected) && StringUtils.isBlank(min) && StringUtils.isBlank(max) && StringUtils.isBlank(query) && visible != null && sort != null;
    }
}
