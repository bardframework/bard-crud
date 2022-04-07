package org.bardframework.crud.api.datatable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by v.zafari on 11/14/2015.
 */
public class DataTableFilter<F extends DataTableFilter<F>> {

    protected long page;
    protected long count;
    protected String query;
    protected List<HeaderDto> headers;

    public DataTableFilter() {
    }

    public DataTableFilter(List<HeaderDto> headers) {
        this.headers = headers;
    }

    public List<HeaderDto> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderDto> headers) {
        this.headers = headers;
    }

    public long getCount() {
        return count;
    }

    /**
     * @return <code>this</code> for method chaining
     */
    public F setCount(long count) {
        this.count = count;
        return (F) this;
    }

    public long getPage() {
        return page;
    }

    /**
     * @return <code>this</code> for method chaining
     */
    public F setPage(long page) {
        this.page = page;
        return (F) this;
    }

    public String getQuery() {
        return query;
    }

    public F setQuery(String query) {
        this.query = query;
        return (F) this;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (0 == count || 10 == count) && page < 2 && !StringUtils.isNotBlank(query) && (CollectionUtils.isEmpty(headers) || headers.stream().allMatch(HeaderDto::isEmpty));
    }
}
