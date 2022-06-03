package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.crud.api.filter.Filter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Sama-PC on 09/05/2017.
 */
public abstract class RepositoryTestAbstract<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, R extends BaseRepository<M, C, I, U>, P extends DataProviderRepositoryAbstract<M, C, R, I, U>, I extends Comparable<? super I>, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected R repository;
    @Autowired
    private P dataProvider;

    @Test
    public void testGetByIdValid() {
        I id = this.getDataProvider().getId(this.getUser());
        LOGGER.debug("test get by id '{}'.", id);
        M result = repository.get(id, this.getUser());
        LOGGER.debug("get by id '{}', result is '{}'.", id, result);
        assertThat(result).isNotNull();
        assertThat(id).isEqualTo(result.getId());
    }

    @Test
    public void testGetByIdInvalid() {
        I invalidId = this.getDataProvider().getInvalidId();
        LOGGER.debug("test get by invalid id '{}'.", invalidId);
        M result = repository.get(invalidId, this.getUser());
        LOGGER.debug("get by invalid id '{}', result is '{}'.", invalidId, result);
        assertThat(result).isNull();
    }

    @Test
    public void testGetByIdNull() {
        LOGGER.debug("test get by null id'.");
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = repository.get((I) null, this.getUser());
            LOGGER.error("get by null id, expect exception but result is '{}'.", result);
        });
    }

    @Test
    public void testGetByIdsDuplicate() {
        I id = this.getDataProvider().getId(this.getUser());
        List<I> duplicateIds = Arrays.asList(id, id, id, id, id);
        LOGGER.debug("test get by duplicate ids '{}'.", duplicateIds);
        List<M> result = repository.get(duplicateIds, this.getUser());
        LOGGER.debug("get by duplicate ids '{}', result  is '{}'.", duplicateIds, result);
        assertThat(result).hasSize(1).map(BaseModel::getId).containsOnly(id);
    }

    @Test
    public void testGetByIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = repository.get((List<I>) null, this.getUser());
            LOGGER.error("get by null ids, expect exception but result is'{}'.", result);
        });
    }

    @Test
    public void testGetByIdsEmpty() {
        assertThatThrownBy(() -> repository.get(List.of(), this.getUser()));
    }

    @Test
    public void testGetByCriteria() {
        /*
          to be sure at least one entity exist.
         */
        this.getDataProvider().getModel(this.getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> result = repository.get(criteria, this.getUser());
        LOGGER.debug("get db by criteria '{}', result is '{}'.", criteria, result);
        assertThat(result).size().isGreaterThan(0);
    }

    @Test
    public void testGetByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = repository.get((C) null, this.getUser());
            LOGGER.error("get by null criteria, expect exception but result is '{}'.", result);
        });
    }

    /*
      invalid criteria for get is meaningless
     */

    /**
     * Check whether notIn field is correctly working in the case of get(Criteria c).
     */
    @Test
    public void testGetByCriteriaNotIn() {
        M model = this.getDataProvider().getModel(this.getUser());
        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for get(Criteria c) method. */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotIn(ids));
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> foundEntities = repository.get(criteria, this.getUser());
        LOGGER.debug("get by criteria '{}', result is '{}'.", criteria, foundEntities);
        assertThat(foundEntities).map(BaseModel::getId).doesNotContainAnyElementsOf(ids);
    }

    @Test
    public abstract void testGetOne();

    @Test
    public void testGetOneNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.getOne(null, this.getUser()));
    }

    /*----------------------- Delete ---------------------*/
    @Test
    public void testDeleteByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.delete((C) null, this.getUser()));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    /*----------------------- Save ---------------------*/
    @Test
    public void testSave() {
        M model = this.getDataProvider().getUnsavedModel(this.getUser());
        LOGGER.debug("saving '{}'", model);
        M result = repository.save(model, this.getUser());
        LOGGER.debug("save '{}', result is '{}'.", model, result);
        this.getDataProvider().assertEqualSave(model, result);
    }

    @Test
    public void testSaveInvalid() {
        M model = this.getDataProvider().getUnsavedInvalidModel(this.getUser());
        LOGGER.debug("saving '{}'", model);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = repository.save(model, this.getUser());
            LOGGER.debug("save invalid model '{}', expect exception but result is '{}'.", model, result);
        });
    }

    @Test
    public void testSaveNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = repository.save((M) null, this.getUser());
            LOGGER.debug("save null model, expect exception but result is '{}'.", result);
        });
    }

    @Test
    public void testSaveCollection() {
        List<M> list = this.getDataProvider().getUnsavedModels(RandomUtils.nextInt(1, 10), this.getUser());
        Collection<M> result = repository.save(list, this.getUser());
        assertThat(result.size()).isEqualTo(list.size());
        for (M savedEntity : result) {
            M entity = list.get(list.indexOf(savedEntity));
            this.getDataProvider().assertEqualSave(entity, savedEntity);
        }
    }

    @Test
    public void testSaveCollectionEmpty() {
        List<M> list = new ArrayList<>();
        List<M> saved = repository.save(list, this.getUser());
        /*
          i.e no model has been saved to the database.
          */
        assertThat(saved).hasSize(0);
    }

    @Test
    public void testSaveCollectionInvalidMember() {
        final int count = RandomUtils.nextInt(1, 10);
        List<M> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(this.getDataProvider().getUnsavedModel(this.getUser()));
        }
        // insert several invalid models in random positions
        for (int i = 0; i < RandomUtils.nextInt(1, 3); i++) {
            list.add(RandomUtils.nextInt(0, list.size() - 1), this.getDataProvider().getUnsavedInvalidModel(this.getUser()));
        }
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.save(list, this.getUser()));
    }

    @Test
    public void testSaveCollectionNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.save((List<M>) null, this.getUser()));
    }
    /*----------------------- Update ---------------------*/

    @Test
    public void testUpdate() {
        M sourceModel = this.getDataProvider().getModel(this.getUser());
        M updatedModel = repository.update(sourceModel, this.getUser());
        this.getDataProvider().assertEqualUpdate(sourceModel, updatedModel);
    }

    @Test
    public void testUpdateInvalidModel() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.update(this.getDataProvider().getInvalidModel(this.getUser()), this.getUser()));
    }

    @Test
    public void testUpdateNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.update(null, this.getUser()));
    }

    @Test
    public void testUpdateUnsavedModel() {
        M model = this.getDataProvider().getUnsavedModel(this.getUser());
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.update(model, this.getUser()));
    }

    /*---------------------- Filter ------------------------*/
    @Test
    public void testFilter() {
        int dataCount = RandomUtils.nextInt(1, 3);
        this.getDataProvider().getModels(dataCount, this.getUser());
        C validFilter = this.getDataProvider().getEmptyCriteria();
        PagedData<M> pagedData = repository.get(validFilter, PageRequest.of(0, dataCount), this.getUser());
        assertThat(pagedData.getTotal()).isGreaterThanOrEqualTo(dataCount);
        assertThat(pagedData.getData()).isNotEmpty();
    }

    @Test
    public void testFilterNull() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> repository.get(null, PageRequest.of(1, Integer.MAX_VALUE), this.getUser()));
    }

    @Test
    public void testFilterIds() {
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(this.getUser());
        assertThat(repository.getIds(this.getDataProvider().getEmptyCriteria(), this.getUser())).size().isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testFilterIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.getIds(null, this.getUser()));
    }

    /*
      testFilterIdsInvalid():  does not consider page & size. It uses just other fields of criteria.
      So, We can't have invalid criteria for this method in api class.
     */


    /**
     * Check whether notIn field is correctly working in the case of filterIds().
     */
    @Test
    public void testFilterIdsNotIn() {
        M model = this.getDataProvider().getModel(this.getUser());

        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for filterIds(). */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotIn(ids));
        List<I> list = repository.getIds(criteria, this.getUser());
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    public void testFilterCount() {
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(this.getUser());
        assertThat(repository.getCount(this.getDataProvider().getEmptyCriteria(), this.getUser())).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testFilterCountNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.getCount(null, this.getUser()));
    }

    /*
      testFilterCountInvalid():  does not consider page & size. It uses just other fields of criteria.
      So, We can't have invalid criteria for this method in api class.
     */

    @Test
    public void testIsExist() {
        this.getDataProvider().getModel(this.getUser());
        boolean result = repository.isExist(this.getDataProvider().getEmptyCriteria(), this.getUser());
        assertThat(result).isTrue();
    }

    @Test
    public void testIsExistNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> repository.isExist(null, this.getUser()));
    }

    public P getDataProvider() {
        return dataProvider;
    }

    protected abstract U getUser();
}
