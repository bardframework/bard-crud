package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.form.filter.IdFilter;
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
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        M foundModel = this.getService().get(id, user);
        assertThat(foundModel).isNotNull();
        assertThat(foundModel.getId()).isEqualTo(id);
    }

    @Test
    default void testGetByIdNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get((I) null, user));
    }

    /**
     * Invalid is an id which does not exist.
     */
    @Test
    default void testGetByIdInvalid() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getInvalidId();
        M model = this.getService().get(id, user);
        assertThat(model).isNull();
    }

    /**
     * A given criteria should consider newly saved models.
     */
    @Test
    default void testGetByCriteria() {
        U user = this.getDataProvider().getUser();
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), user);
        List<M> models = this.getService().get(this.getDataProvider().getEmptyCriteria(), user);
        assertThat(models).isNotNull().isNotEmpty().doesNotContainNull().size().isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    default void testGetByCriteriaNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get((C) null, user));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    @Test
    default void testGetByCriteriaNotIn() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new IdFilter<I>().setNotEquals(model.getId()));
        List<M> list = this.getService().get(criteria, user);
        assertThat(list.stream().map(BaseModel::getId)).doesNotContain(model.getId());
    }
    /*------------------------------- Delete ------------------------------*/

    @Test
    default void testDeleteById() {
        U user = this.getDataProvider().getUser();
        M savedModel = this.getDataProvider().saveNew(1, user).get(0);
        long count = this.getService().delete(savedModel.getId(), user);
        /* Check that only one model is deleted. */
        assertThat(count).isEqualByComparingTo(1L);
        /* Check that deleted model cannot be found anymore. */
        M foundModel = this.getService().get(savedModel.getId(), user);
        assertThat(foundModel).isNull();
    }

    @Test
    default void testDeleteByIds() {
        U user = this.getDataProvider().getUser();
        int count = RandomUtils.nextInt(3, 10);
        List<M> saved = this.getDataProvider().saveNew(count, user);
        List<I> ids = saved.stream().map(M::getId).collect(Collectors.toList());
        long size = this.getService().delete(ids, user);
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /* Make sure records are deleted from DB. */
        assertThat(this.getService().get(ids, user)).isEmpty();
    }

    @Test
    default void testDeleteByIdsDuplicate() {
        U user = this.getDataProvider().getUser();
        int count = RandomUtils.nextInt(5, 20);
        List<M> saved = this.getDataProvider().saveNew(count, user);
        List<I> ids = new ArrayList<>();
        for (M m : saved) {
            ids.add(m.getId());
            ids.add(m.getId());
            ids.add(m.getId());
        }
        long size = this.getService().delete(ids, user);
        /*
          Make sure no records more than actual count, i.e save list size, are deleted.
         */
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /*
          Make sure all records are deleted from Database.
         */
        assertThat(this.getService().get(ids, user)).isEmpty();
    }

    /**
     * Invalid id is an identifier which does not exist.
     */
    @Test
    default void testDeleteByIdInvalid() {
        U user = this.getDataProvider().getUser();
        I invalidId = this.getDataProvider().getInvalidId();
        long count = this.getService().delete(invalidId, user);
        /* deleted record count should be zero. */
        assertThat(count).isEqualByComparingTo(0L);
    }

    @Test
    default void testSave() {
        U user = this.getDataProvider().getUser();
        D dto = this.getDataProvider().getDto();
        LOGGER.debug("saving '{}'", dto);
        M result = this.getService().save(dto, user);
        LOGGER.debug("save '{}', result is '{}'.", dto, result);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        M model = this.getService().get(result.getId(), user);
        assertThat(model).isNotNull();
        this.getDataProvider().assertEqualSave(result, model);
    }

    @Test
    default void testSaveInvalid() {
        U user = this.getDataProvider().getUser();
        D invalidDto = this.getDataProvider().getInvalidDto();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().save(invalidDto, user));
    }

    @Test
    default void testSaveNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().save((D) null, user));
    }
    /*------------------------------- Update ------------------------------*/

    @Test
    default void testUpdate() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D dto = this.getDataProvider().getDto();
        LOGGER.debug("updating '{}'", dto);
        M result = this.getService().update(id, dto, user);
        LOGGER.debug("update '{}', result is '{}'.", dto, result);
        assertThat(result).isNotNull();
        assertThat(id).isEqualTo(result.getId());
        M model = this.getService().get(id, user);
        assertThat(model).isNotNull();
        this.getDataProvider().assertEqualUpdate(model, dto);
    }

    @Test
    default void testUpdateInvalid() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D invalidDto = this.getDataProvider().getInvalidDto();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().update(id, invalidDto, user));
    }

    @Test
    default void testUpdateNull() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().update(id, null, user));
    }

    /*------------------------------- Filter ------------------------------*/

    @Test
    default void testFilterCount() {
        U user = this.getDataProvider().getUser();
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), user);
        long count = this.getService().getCount(this.getDataProvider().getEmptyCriteria(), user);

        /* Check that count at least is in the size of savedModel. */
        assertThat(count).isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    default void testFilterCountNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().getCount(null, user));
    }

    /*
       testFilterCountInvalid(): is page & size neutral. So, invalid page and size is not checked for this method.
     */
    @Test
    default void testFilterIds() {
        U user = this.getDataProvider().getUser();
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 5), user);
        List<I> savedIds = savedList.stream().map(M::getId).collect(Collectors.toList());
        List<I> ids = this.getService().getIds(this.getDataProvider().getEmptyCriteria(), user);
        /* Saved model ids must be in the list of filtered ids. */
        assertThat(ids).isNotNull().isNotEmpty().doesNotContainNull().containsAll(savedIds);
    }

    @Test
    default void testFilterIdsNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().getIds(null, user));
    }

    /**
     * invalid criteria for testFilterIdsInvalid() is meaningless.
     */

    @Test
    default void testFilterIdsNotIn() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new IdFilter<I>().setNotEquals(model.getId()));
        List<I> list = this.getService().getIds(criteria, user);
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    default void testFilter() {
        U user = this.getDataProvider().getUser();
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), user);
        /*
            Create a criteria that returns all the result in one page.
        */
        C criteria = this.getDataProvider().getFilterCriteria();
        long count = this.getService().getCount(criteria, user);

        PagedData<M> pagedData = this.getService().get(criteria, PageRequest.of(0, (int) count), user);

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
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getService().get(null, PageRequest.of(0, Integer.MAX_VALUE), user));
    }
}
