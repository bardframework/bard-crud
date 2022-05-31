//package org.bardframework.api.base;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import org.bardframework.common.utils.AssertionUtils;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * Created by Vahid Zafari on 4/08/2017.
// */
//public class DataTableModel<M> {
//
//    private final List<M> list;
//    private final long total;
//
//    public DataTableModel() {
//        this.list = Collections.emptyList();
//        this.total = 0;
//    }
//
//    @JsonCreator
//    public DataTableModel(@JsonProperty("list") List<M> list, @JsonProperty("total") long total) {
//        AssertionUtils.notNull(list, "null list not acceptable");
//        this.list = list;
//        this.total = total;
//    }
//
//    public long getTotal() {
//        return total;
//    }
//
//    public List<M> getList() {
//        return list;
//    }
//}
