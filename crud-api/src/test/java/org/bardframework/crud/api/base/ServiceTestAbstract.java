package org.bardframework.crud.api.base;

import org.bardframework.commons.utils.RandomUtils;
import org.bardframework.crud.api.filter.IdFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Created by Sama-PC on 10/05/2017.
 */
public abstract class ServiceTestAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, D, S extends BaseServiceAbstract<M, C, D, ?, I, U>, P extends DataProviderServiceAbstract<M, C, D, ?, ?, I, U>, I extends Comparable<? super I>, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected S service;
    @Autowired
    private P dataProvider;

    /**
     * Utility method to get an element in a collection which contains the given property.
     */
    public M getListElement(List<M> list, I id) {
        return list.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    /*------------------------------- get ------------------------------*/

    @Test
    public void testGetById() {
        I id = this.getDataProvider().getId(this.getUser());
        M foundModel = service.get(id, this.getUser());
        assertThat(foundModel).isNotNull();
        assertThat(foundModel.getId()).isEqualTo(id);
    }

    @Test
    public void testGetByIdNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.get((I) null, this.getUser()));
    }

    /**
     * Invalid is an id which does not exist.
     */
    @Test
    public void testGetByIdInvalid() {
        I id = this.getDataProvider().getInvalidId();
        M model = service.get(id, this.getUser());
        assertThat(model).isNull();
    }

    /**
     * A given criteria should consider newly saved models.
     */
    @Test
    public void testGetByCriteria() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getUser());
        List<M> models = service.get(this.getDataProvider().getEmptyCriteria(), this.getUser());
        assertThat(models).isNotNull().isNotEmpty().doesNotContainNull().size().isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    public void testGetByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.get((C) null, this.getUser()));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    @Test
    public void testGetByCriteriaNotIn() {
        M model = this.getDataProvider().getModel(this.getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setNotEquals(model.getId()));
        List<M> list = service.get(criteria, this.getUser());
        assertThat(list).extracting("id").doesNotContain(model.getId());
    }

    /**
     * Implementations should provide a criteria that considers only on model.
     */
    @Test
    public abstract void testGetOne();

    /*------------------------------- Delete ------------------------------*/

    @Test
    public void testDeleteById() {
        M savedModel = this.getDataProvider().saveNew(1, this.getUser()).get(0);
        long count = service.delete(savedModel.getId(), this.getUser());
        /* Check that only one model is deleted. */
        assertThat(count).isEqualByComparingTo(1L);
        /* Check that deleted model cannot be found anymore. */
        M foundModel = service.get(savedModel.getId(), this.getUser());
        assertThat(foundModel).isNull();
    }

    @Test
    public void testDeleteByIds() {
        int count = RandomUtils.nextInt(3, 10);
        List<M> saved = this.getDataProvider().saveNew(count, this.getUser());
        List<I> ids = saved.stream().map(M::getId).collect(Collectors.toList());
        long size = service.delete(ids, this.getUser());
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /* Make sure records are deleted from DB. */
        assertThat(service.get(ids, this.getUser())).isEmpty();
    }

    @Test
    public void testDeleteByIdsDuplicate() {
        int count = RandomUtils.nextInt(5, 20);
        List<M> saved = this.getDataProvider().saveNew(count, this.getUser());
        List<I> ids = new ArrayList<>();
        for (M m : saved) {
            ids.add(m.getId());
            ids.add(m.getId());
            ids.add(m.getId());
        }
        long size = service.delete(ids, this.getUser());
        /*
          Make sure no records more than actual count, i.e save list size, are deleted.
         */
        assertThat(size).isEqualByComparingTo((long) saved.size());
        /*
          Make sure all records are deleted from Database.
         */
        assertThat(service.get(ids, this.getUser())).isEmpty();
    }

    /**
     * Invalid id is an identifier which does not exist.
     */
    @Test
    public void testDeleteByIdInvalid() {
        long count = service.delete(this.getDataProvider().getInvalidId(), this.getUser());
        /* deleted record count should be zero. */
        assertThat(count).isEqualByComparingTo(0L);
    }

    /**
     * can't create test data according specific condition and delete them by criteria.
     */
    @Test
    public abstract void testDeleteByCriteria();
    /*-------------------------------  Save  ------------------------------*/

    @Test
    public void testSave() {
        D dto = this.getDataProvider().getDto(this.getUser());
        LOGGER.debug("saving '{}'", dto);
        M result = service.save(dto, this.getUser());
        LOGGER.debug("save '{}', result is '{}'.", dto, result);
        assertThat(result.getId()).isNotNull();
        this.getDataProvider().assertEqualSave(result, service.get(result.getId(), this.getUser()));
    }

    @Test
    public void testSaveInvalid() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.save(this.getDataProvider().getUnsavedInvalidDto(this.getUser()), this.getUser()));
    }

    @Test
    public void testSaveNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.save((D) null, this.getUser()));
    }
    /*------------------------------- Update ------------------------------*/

    @Test
    public void testUpdate() {
        I id = this.getDataProvider().getId(this.getUser());
        D dto = this.getDataProvider().getDto(this.getUser());
        LOGGER.debug("updating '{}'", dto);
        M result = service.update(id, dto, this.getUser());
        LOGGER.debug("update '{}', result is '{}'.", dto, result);
        assertThat(id).isEqualTo(result.getId());
        M getModel = service.get(id, this.getUser());
        this.getDataProvider().assertEqualUpdate(getModel, dto);
    }

    @Test
    public void testUpdateInvalid() {
        I id = this.getDataProvider().getId(this.getUser());
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.update(id, this.getDataProvider().getInvalidDto(this.getUser()), this.getUser()));
    }

    @Test
    public void testUpdateNull() {
        I id = this.getDataProvider().getId(this.getUser());
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.update(id, null, this.getUser()));
    }

    /*------------------------------- Filter ------------------------------*/

    @Test
    public void testFilterCount() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getUser());
        long count = service.getCount(this.getDataProvider().getEmptyCriteria(), this.getUser());

        /* Check that count at least is in the size of savedModel. */
        assertThat(count).isGreaterThanOrEqualTo(savedList.size());
    }

    @Test
    public void testFilterCountNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.getCount(null, this.getUser()));
    }

    /*
       testFilterCountInvalid(): is page & size neutral. So, invalid page and size is not checked for this method.
     */

    @Test
    public void testFilterIds() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 5), this.getUser());
        List<I> savedIds = savedList.stream().map(M::getId).collect(Collectors.toList());
        List<I> ids = service.getIds(this.getDataProvider().getEmptyCriteria(), this.getUser());
        /* Saved model ids must be in the list of filtered ids. */
        assertThat(ids).isNotNull().isNotEmpty().doesNotContainNull().containsAll(savedIds);
    }

    @Test
    public void testFilterIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.getIds(null, this.getUser()));
    }

    /**
     * invalid criteria for testFilterIdsInvalid() is meaningless.
     */

    @Test
    public void testFilterIdsNotIn() {
        M model = this.getDataProvider().getModel(this.getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setNotEquals(model.getId()));
        List<I> list = service.getIds(criteria, this.getUser());
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    public void testFilter() {
        List<M> savedList = this.getDataProvider().getModels(RandomUtils.nextInt(1, 10), this.getUser());
        /* Create a criteria that returns all the result in one page. */
        long count = service.getCount(this.getDataProvider().getCriteria(), this.getUser());
        C onePageCriteria = this.getDataProvider().getCriteria();

        Page<M> dataTable = service.get(onePageCriteria, PageRequest.of(0, (int) count), this.getUser());

        assertThat(dataTable).isNotNull();
        assertThat(dataTable.getTotalElements()).isGreaterThanOrEqualTo(savedList.size());
        assertThat(dataTable.getContent()).isNotNull().isNotEmpty().doesNotContainNull();
        /*
          Saved model ids must be in the list of filtered ids.
          */
        List<I> savedIds = savedList.stream().map(M::getId).collect(Collectors.toList());
        assertThat(dataTable.getContent()).extracting("id").containsAll(savedIds);
    }

    @Test
    public void testFilterNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> service.get(null, PageRequest.of(0, Integer.MAX_VALUE), this.getUser()));
    }

    public P getDataProvider() {
        return dataProvider;
    }

    protected abstract U getUser();
}
