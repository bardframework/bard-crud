package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.crud.api.filter.Filter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Created by Sama-PC on 10/05/2017.
 */
public interface BaseServiceTest<M extends BaseModel<I>, C extends BaseCriteria<I>, D, S extends BaseServiceAbstract<M, C, D, ?, I, U>, P extends DataProviderService<M, C, D, ?, ?, I, U>, I extends Comparable<? super I>, U> {

    Logger LOGGER = LoggerFactory.getLogger(BaseServiceTest.class);

    S getService();

    P getDataProvider();

    /**
     * Utility method to get an element in a collection which contains the given property.
     */
    default M getListElement(List<M> list, I id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    /*------------------------------- get ------------------------------*/

    @Test
    default void testGetById() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        M foundModel = this.getService().get(id, this.getDataProvider().getUser());
        assertThat(foundModel).isNotNull();
        assertThat(foundModel.getId()).isEqualTo(id);
    }

    @Test
    default void testGetByIdNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get((I) null, this.getDataProvider().getUser()));
    }

    /**
     * Invalid is an id which does not exist.
     */
    @Test
    default void testGetByIdInvalid() {
        I id = this.getDataProvider().getInvalidId();
        M model = this.getService().get(id, this.getDataProvider().getUser());
        assertThat(model).isNull();
    }

    /**
     * A given criteria should consider newly saved models.
     */
    @Test
    default void testGetByCriteria() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getDataProvider().getUser());
        List<M> models = this.getService().get(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser());
        assertThat(models).isNotNull().isNotEmpty().doesNotContainNull().size().isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    default void testGetByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get((C) null, this.getDataProvider().getUser()));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    @Test
    default void testGetByCriteriaNotIn() {
        M model = this.getDataProvider().getModel(this.getDataProvider().getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotEquals(model.getId()));
        List<M> list = this.getService().get(criteria, this.getDataProvider().getUser());
        assertThat(list.stream().map(BaseModel::getId)).doesNotContain(model.getId());
    }
    /*------------------------------- Delete ------------------------------*/

    @Test
    default void testDeleteById() {
        M savedModel = this.getDataProvider().saveNew(1, this.getDataProvider().getUser()).get(0);
        long count = this.getService().delete(savedModel.getId(), this.getDataProvider().getUser());
        /* Check that only one model is deleted. */
        assertThat(count).isEqualByComparingTo(1L);
        /* Check that deleted model cannot be found anymore. */
        M foundModel = this.getService().get(savedModel.getId(), this.getDataProvider().getUser());
        assertThat(foundModel).isNull();
    }

    @Test
    default void testDeleteByIds() {
        int count = RandomUtils.nextInt(3, 10);
        List<M> saved = this.getDataProvider().saveNew(count, this.getDataProvider().getUser());
        List<I> ids = saved.stream().map(M::getId).collect(Collectors.toList());
        long size = this.getService().delete(ids, this.getDataProvider().getUser());
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /* Make sure records are deleted from DB. */
        assertThat(this.getService().get(ids, this.getDataProvider().getUser())).isEmpty();
    }

    @Test
    default void testDeleteByIdsDuplicate() {
        int count = RandomUtils.nextInt(5, 20);
        List<M> saved = this.getDataProvider().saveNew(count, this.getDataProvider().getUser());
        List<I> ids = new ArrayList<>();
        for (M m : saved) {
            ids.add(m.getId());
            ids.add(m.getId());
            ids.add(m.getId());
        }
        long size = this.getService().delete(ids, this.getDataProvider().getUser());
        /*
          Make sure no records more than actual count, i.e save list size, are deleted.
         */
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /*
          Make sure all records are deleted from Database.
         */
        assertThat(this.getService().get(ids, this.getDataProvider().getUser())).isEmpty();
    }

    /**
     * Invalid id is an identifier which does not exist.
     */
    @Test
    default void testDeleteByIdInvalid() {
        long count = this.getService().delete(this.getDataProvider().getInvalidId(), this.getDataProvider().getUser());
        /* deleted record count should be zero. */
        assertThat(count).isEqualByComparingTo(0L);
    }

    @Test
    default void testSave() {
        D dto = this.getDataProvider().getDto();
        LOGGER.debug("saving '{}'", dto);
        M result = this.getService().save(dto, this.getDataProvider().getUser());
        LOGGER.debug("save '{}', result is '{}'.", dto, result);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        M model = this.getService().get(result.getId(), this.getDataProvider().getUser());
        assertThat(model).isNotNull();
        this.getDataProvider().assertEqualSave(result, model);
    }

    @Test
    default void testSaveInvalid() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().save(this.getDataProvider().getUnsavedInvalidDto(), this.getDataProvider().getUser()));
    }

    @Test
    default void testSaveNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().save((D) null, this.getDataProvider().getUser()));
    }
    /*------------------------------- Update ------------------------------*/

    @Test
    default void testUpdate() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        D dto = this.getDataProvider().getDto();
        LOGGER.debug("updating '{}'", dto);
        M result = this.getService().update(id, dto, this.getDataProvider().getUser());
        LOGGER.debug("update '{}', result is '{}'.", dto, result);
        assertThat(result).isNotNull();
        assertThat(id).isEqualTo(result.getId());
        M model = this.getService().get(id, this.getDataProvider().getUser());
        assertThat(model).isNotNull();
        this.getDataProvider().assertEqualUpdate(model, dto);
    }

    @Test
    default void testUpdateInvalid() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().update(id, this.getDataProvider().getInvalidDto(), this.getDataProvider().getUser()));
    }

    @Test
    default void testUpdateNull() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().update(id, null, this.getDataProvider().getUser()));
    }

    /*------------------------------- Filter ------------------------------*/

    @Test
    default void testFilterCount() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getDataProvider().getUser());
        long count = this.getService().getCount(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser());

        /* Check that count at least is in the size of savedModel. */
        assertThat(count).isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    default void testFilterCountNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().getCount(null, this.getDataProvider().getUser()));
    }

    /*
       testFilterCountInvalid(): is page & size neutral. So, invalid page and size is not checked for this method.
     */
    @Test
    default void testFilterIds() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 5), this.getDataProvider().getUser());
        List<I> savedIds = savedList.stream().map(M::getId).collect(Collectors.toList());
        List<I> ids = this.getService().getIds(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser());
        /* Saved model ids must be in the list of filtered ids. */
        assertThat(ids).isNotNull().isNotEmpty().doesNotContainNull().containsAll(savedIds);
    }

    @Test
    default void testFilterIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().getIds(null, this.getDataProvider().getUser()));
    }

    /**
     * invalid criteria for testFilterIdsInvalid() is meaningless.
     */

    @Test
    default void testFilterIdsNotIn() {
        M model = this.getDataProvider().getModel(this.getDataProvider().getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotEquals(model.getId()));
        List<I> list = this.getService().getIds(criteria, this.getDataProvider().getUser());
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    default void testFilter() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getDataProvider().getUser());
        /*
            Create a criteria that returns all the result in one page.
        */
        long count = this.getService().getCount(this.getDataProvider().getCriteria(), this.getDataProvider().getUser());
        C criteria = this.getDataProvider().getCriteria();

        PagedData<M> pagedData = this.getService().get(criteria, PageRequest.of(0, (int) count), this.getDataProvider().getUser());

        assertThat(pagedData).isNotNull();
        assertThat(pagedData.getTotal()).isGreaterThanOrEqualTo(savedList.size());
        assertThat(pagedData.getData()).isNotNull().isNotEmpty().doesNotContainNull();
        /*
            Saved model ids must be in the list of filtered ids.
        */
        List<I> savedIds = savedList.stream().map(M::getId).collect(Collectors.toList());
        assertThat(pagedData.getData()).map(BaseModel::getId).containsAll(savedIds);
    }

    @Test
    default void testFilterNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get(null, PageRequest.of(0, Integer.MAX_VALUE), this.getDataProvider().getUser()));
    }
}
