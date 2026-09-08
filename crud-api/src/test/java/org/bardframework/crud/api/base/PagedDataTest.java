package org.bardframework.crud.api.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedDataTest {

    @Test
    void emptyPagedDataHasNoRowsAndZeroTotal() {
        PagedData<String> paged = new PagedData<>();

        assertThat(paged.getData()).isEmpty();
        assertThat(paged.getTotal()).isZero();
    }

    @Test
    @DisplayName("total is independent of the page size — it counts the whole result set")
    void totalIsIndependentOfPageContent() {
        PagedData<String> paged = new PagedData<>(List.of("a", "b"), 57);

        assertThat(paged.getData()).containsExactly("a", "b");
        assertThat(paged.getTotal()).isEqualTo(57);
    }

    @Test
    @DisplayName("a null row list is treated as empty rather than propagating null")
    void nullDataBecomesEmptyList() {
        PagedData<String> paged = new PagedData<>(null, 0);

        assertThat(paged.getData()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("the constructor copies the list, so later changes to the source do not leak in")
    void constructorCopiesGivenList() {
        List<String> source = new java.util.ArrayList<>(List.of("a"));
        PagedData<String> paged = new PagedData<>(source, 1);

        source.add("b");

        assertThat(paged.getData()).containsExactly("a");
    }
}
