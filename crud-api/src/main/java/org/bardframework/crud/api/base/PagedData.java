package org.bardframework.crud.api.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PagedData<M> {

    @JsonProperty("result")
    private List<M> data = new ArrayList<>();
    private long total;

    public PagedData(List<M> data, long total) {
        if (null != data) {
            this.data.addAll(data);
        }
        this.total = total;
    }
}
