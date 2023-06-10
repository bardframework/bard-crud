package org.bardframework.crud.api.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PagedData<M> {

    private List<M> data = new ArrayList<>();
    private long total;

    public PagedData(List<M> data, long total) {
        if (null != data) {
            this.data = data;
        }
        this.total = total;
    }
}
