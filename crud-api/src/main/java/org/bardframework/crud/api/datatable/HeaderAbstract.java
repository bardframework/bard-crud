package org.bardframework.crud.api.datatable;

import org.bardframework.commons.jackson.converter.*;
import org.bardframework.crud.common.converter.StringToEnumConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.bardframework.crud.api.datatable.FilteringType.*;

/**
 * Created by Vahid Zafari on 8/12/2016.
 */
public abstract class HeaderAbstract {

    protected static final Logger LOGGER = LoggerFactory.getLogger(HeaderAbstract.class);

    protected String path;
    protected QueryType queryType = QueryType.NONE;
    protected FilteringType filterType = FilteringType.NONE;
    protected ResultType resultType = ResultType.PRIMITIVE;
    protected boolean visible = true;
    protected boolean searchable = false;
    protected boolean sortable = false;
    protected Sort sort;
    protected String mask;
    protected Converter<?, ?> inputConverter;
    protected Converter<?, ?> outputConverter;
    protected List<String> messageKeys;

    public HeaderAbstract() {
    }

    public void validate(Class<?> mainClazz) {
        /*
          check HeaderAbstract parameters are valid and consistent
         */
        if (this.filterType == FilteringType.NONE) {
//            if (this.queryType != QueryType.NONE) {
//                LOGGER.error("invalid HeaderAbstract definition on '{}'@'{}', when filtering type is {}, query type must be {}", path, mainClazz.getSimpleName(), NONE, QueryType.NONE);
//                throw new IllegalStateException("filtering and query type must be NONE or not NONE together");
//            }
        } else if (this.filterType == MULTI_SELECT || this.filterType == SINGLE_SELECT || this.filterType == TREE_SELECT || this.filterType == MIN_MAX) {
            if (this.searchable) {
                LOGGER.error("invalid HeaderAbstract definition on '{}'@'{}' class,searchable can'reflection apply for (SINGLE|MULTI|TREE)_SELECT, MIN_MAX", path, mainClazz.getSimpleName());
                throw new IllegalArgumentException("searchable can'reflection apply for (SINGLE|MULTI|TREE)_SELECT");
            }
            if (this.sortable) {
                LOGGER.error("invalid HeaderAbstract definition on '{}'@'{}' class,sortable can'reflection apply for (SINGLE|MULTI|TREE)_SELECT, MIN_MAX", path, mainClazz.getSimpleName());
                throw new IllegalArgumentException("sortable can'reflection apply for (SINGLE|MULTI|TREE)_SELECT");
            }
        }
    }

    public void determineConverters(Class<?> valueType) {
        /*
          set outputConverter according field type
         */
        Class<? extends Converter> inputConverterClazz = null, outputConverterClazz = null;
        if (Enum.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToEnumConverter.class;
//            outputConverterClazz = EnumToBaseDataConverter.class;
        } else if (LocalDate.class.isAssignableFrom(valueType)) {
//            inputConverterClazz = JalaliDateStringToLocalDateConverter.class;
//            outputConverterClazz = LocalDateToStringConverter.class;
        } else if (LocalDateTime.class.isAssignableFrom(valueType)) {
//            inputConverterClazz = JalaliDateTimeStringToLocalDateTimeConverter.class;
//            outputConverterClazz = LocalDateTimeToStringConverter.class;
        } else if (Boolean.class.isAssignableFrom(valueType) || boolean.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToBooleanConverter.class;
//            outputConverterClazz = BooleanToBaseDataConverter.class;
        } else if (Long.class.isAssignableFrom(valueType) || long.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToLongConverter.class;
            outputConverterClazz = null;
        } else if (Integer.class.isAssignableFrom(valueType) || int.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToIntegerConverter.class;
            outputConverterClazz = null;
        } else if (Short.class.isAssignableFrom(valueType) || short.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToShortConverter.class;
            outputConverterClazz = null;
        } else if (Byte.class.isAssignableFrom(valueType) || byte.class.isAssignableFrom(valueType)) {
            inputConverterClazz = StringToByteConverter.class;
            outputConverterClazz = null;
        }
        if (null != inputConverterClazz) {
            try {
                if (inputConverterClazz.equals(StringToEnumConverter.class)) {
                    inputConverter = new StringToEnumConverter(valueType);
                } else {
                    this.inputConverter = inputConverterClazz.newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("can't instantiate input converter using empty constructor {}", inputConverterClazz, e);
                throw new IllegalArgumentException("can't instantiate input converter using empty constructor" + inputConverterClazz, e);
            }
        }
        if (null != outputConverterClazz) {
            try {
                this.outputConverter = outputConverterClazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("can't instantiate output converter using empty constructor {}", outputConverterClazz, e);
                throw new IllegalArgumentException("can't instantiate output converter using empty constructor" + outputConverterClazz, e);
            }
        }
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public FilteringType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilteringType filterType) {
        this.filterType = filterType;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public Converter<?, ?> getInputConverter() {
        return inputConverter;
    }

    public void setInputConverter(Converter<?, ?> inputConverter) {
        this.inputConverter = inputConverter;
    }

    public Converter<?, ?> getOutputConverter() {
        return outputConverter;
    }

    public void setOutputConverter(Converter<?, ?> outputConverter) {
        this.outputConverter = outputConverter;
    }

    public List<String> getMessageKeys() {
        return messageKeys;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getMask() {
        return mask;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "HeaderAbstract{" +
                "path='" + this.getPath() + '\'' +
                '}';
    }
}
