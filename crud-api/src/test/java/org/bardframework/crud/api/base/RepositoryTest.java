package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.crud.api.filter.Filter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Sama-PC on 09/05/2017.
 */
public interface RepositoryTest<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, R extends BaseRepository<M, C, I, U>, P extends DataProviderRepository<M, C, R, I, U>, I extends Comparable<? super I>, U> {

    Logger LOGGER = LoggerFactory.getLogger(RepositoryTest.class);

    R getRepository();

    P getDataProvider();

    @Test
    default void testGetByIdValid() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        LOGGER.debug("test get by id '{}'.", id);
        M result = this.getRepository().get(id, this.getDataProvider().getUser());
        LOGGER.debug("get by id '{}', result is '{}'.", id, result);
        assertThat(result).isNotNull();
        assertThat(id).isEqualTo(result.getId());
    }

    @Test
    default void testGetByIdInvalid() {
        I invalidId = this.getDataProvider().getInvalidId();
        LOGGER.debug("test get by invalid id '{}'.", invalidId);
        M result = this.getRepository().get(invalidId, this.getDataProvider().getUser());
        LOGGER.debug("get by invalid id '{}', result is '{}'.", invalidId, result);
        assertThat(result).isNull();
    }

    @Test
    default void testGetByIdNull() {
        LOGGER.debug("test get by null id'.");
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().get((I) null, this.getDataProvider().getUser());
            LOGGER.error("get by null id, expect exception but result is '{}'.", result);
        });
    }

    @Test
    default void testGetByIdsDuplicate() {
        I id = this.getDataProvider().getId(this.getDataProvider().getUser());
        List<I> duplicateIds = Arrays.asList(id, id, id, id, id);
        LOGGER.debug("test get by duplicate ids '{}'.", duplicateIds);
        List<M> result = this.getRepository().get(duplicateIds, this.getDataProvider().getUser());
        LOGGER.debug("get by duplicate ids '{}', result  is '{}'.", duplicateIds, result);
        assertThat(result).hasSize(1).map(BaseModel::getId).containsOnly(id);
    }

    @Test
    default void testGetByIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = this.getRepository().get((List<I>) null, this.getDataProvider().getUser());
            LOGGER.error("get by null ids, expect exception but result is'{}'.", result);
        });
    }

    @Test
    default void testGetByIdsEmpty() {
        assertThatThrownBy(() -> this.getRepository().get(List.of(), this.getDataProvider().getUser()));
    }

    @Test
    default void testGetByCriteria() {
        /*
          to be sure at least one entity exist.
         */
        this.getDataProvider().getModel(this.getDataProvider().getUser());
        C criteria = this.getDataProvider().getEmptyCriteria();
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> result = this.getRepository().get(criteria, this.getDataProvider().getUser());
        LOGGER.debug("get db by criteria '{}', result is '{}'.", criteria, result);
        assertThat(result).size().isGreaterThan(0);
    }

    @Test
    default void testGetByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = this.getRepository().get((C) null, this.getDataProvider().getUser());
            LOGGER.error("get by null criteria, expect exception but result is '{}'.", result);
        });
    }

    /**
     * Check whether notIn field is correctly working in the case of get(Criteria c).
     */
    @Test
    default void testGetByCriteriaNotIn() {
        M model = this.getDataProvider().getModel(this.getDataProvider().getUser());
        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for get(Criteria c) method. */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotIn(ids));
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> foundEntities = this.getRepository().get(criteria, this.getDataProvider().getUser());
        LOGGER.debug("get by criteria '{}', result is '{}'.", criteria, foundEntities);
        assertThat(foundEntities).map(BaseModel::getId).doesNotContainAnyElementsOf(ids);
    }

    @Test
    default void testGetOneNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getOne(null, this.getDataProvider().getUser()));
    }

    /*----------------------- Delete ---------------------*/
    @Test
    default void testDeleteByCriteriaNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().delete((C) null, this.getDataProvider().getUser()));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    /*----------------------- Save ---------------------*/
    @Test
    default void testSave() {
        M model = this.getDataProvider().getUnsavedModel();
        LOGGER.debug("saving '{}'", model);
        M result = this.getRepository().save(model, this.getDataProvider().getUser());
        LOGGER.debug("save '{}', result is '{}'.", model, result);
        this.getDataProvider().assertEqualSave(model, result);
    }

    @Test
    default void testSaveInvalid() {
        M model = this.getDataProvider().getUnsavedInvalidModel();
        LOGGER.debug("saving '{}'", model);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().save(model, this.getDataProvider().getUser());
            LOGGER.debug("save invalid model '{}', expect exception but result is '{}'.", model, result);
        });
    }

    @Test
    default void testSaveNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().save((M) null, this.getDataProvider().getUser());
            LOGGER.debug("save null model, expect exception but result is '{}'.", result);
        });
    }

    @Test
    default void testSaveCollection() {
        List<M> list = this.getDataProvider().getUnsavedModels(RandomUtils.nextInt(1, 10), this.getDataProvider().getUser());
        Collection<M> result = this.getRepository().save(list, this.getDataProvider().getUser());
        assertThat(result.size()).isEqualTo(list.size());
        for (M savedEntity : result) {
            M entity = list.get(list.indexOf(savedEntity));
            this.getDataProvider().assertEqualSave(entity, savedEntity);
        }
    }

    @Test
    default void testSaveCollectionEmpty() {
        List<M> list = new ArrayList<>();
        List<M> saved = this.getRepository().save(list, this.getDataProvider().getUser());
        /*
          i.e no model has been saved to the database.
          */
        assertThat(saved).hasSize(0);
    }

    @Test
    default void testSaveCollectionInvalidMember() {
        final int count = RandomUtils.nextInt(1, 10);
        List<M> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(this.getDataProvider().getUnsavedModel());
        }
        // insert several invalid models in random positions
        for (int i = 0; i < RandomUtils.nextInt(1, 3); i++) {
            list.add(RandomUtils.nextInt(0, list.size() - 1), this.getDataProvider().getUnsavedInvalidModel());
        }
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().save(list, this.getDataProvider().getUser()));
    }

    @Test
    default void testSaveCollectionNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().save((List<M>) null, this.getDataProvider().getUser()));
    }
    /*----------------------- Update ---------------------*/

    @Test
    default void testUpdate() {
        M sourceModel = this.getDataProvider().getModel(this.getDataProvider().getUser());
        M updatedModel = this.getRepository().update(sourceModel, this.getDataProvider().getUser());
        this.getDataProvider().assertEqualUpdate(sourceModel, updatedModel);
    }

    @Test
    default void testUpdateInvalidModel() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update(this.getDataProvider().getInvalidModel(this.getDataProvider().getUser()), this.getDataProvider().getUser()));
    }

    @Test
    default void testUpdateNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update(null, this.getDataProvider().getUser()));
    }

    @Test
    default void testUpdateUnsavedModel() {
        M model = this.getDataProvider().getUnsavedModel();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update(model, this.getDataProvider().getUser()));
    }

    /*---------------------- Filter ------------------------*/
    @Test
    default void testFilter() {
        int dataCount = RandomUtils.nextInt(1, 3);
        this.getDataProvider().getModels(dataCount, this.getDataProvider().getUser());
        C validFilter = this.getDataProvider().getEmptyCriteria();
        PagedData<M> pagedData = this.getRepository().get(validFilter, PageRequest.of(0, dataCount), this.getDataProvider().getUser());
        assertThat(pagedData.getTotal()).isGreaterThanOrEqualTo(dataCount);
        assertThat(pagedData.getData()).isNotEmpty();
    }

    @Test
    default void testFilterNull() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> this.getRepository().get(null, PageRequest.of(1, Integer.MAX_VALUE), this.getDataProvider().getUser()));
    }

    @Test
    default void testFilterIds() {
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(this.getDataProvider().getUser());
        assertThat(this.getRepository().getIds(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser())).size().isGreaterThanOrEqualTo(1);
    }

    @Test
    default void testFilterIdsNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getIds(null, this.getDataProvider().getUser()));
    }

    /*
      testFilterIdsInvalid():  does not consider page & size. It uses just other fields of criteria.
      So, We can't have invalid criteria for this method in api class.
     */


    /**
     * Check whether notIn field is correctly working in the case of filterIds().
     */
    @Test
    default void testFilterIdsNotIn() {
        M model = this.getDataProvider().getModel(this.getDataProvider().getUser());

        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for filterIds(). */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setId(new Filter<I>().setNotIn(ids));
        List<I> list = this.getRepository().getIds(criteria, this.getDataProvider().getUser());
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    default void testFilterCount() {
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(this.getDataProvider().getUser());
        assertThat(this.getRepository().getCount(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser())).isGreaterThanOrEqualTo(1);
    }

    @Test
    default void testFilterCountNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getCount(null, this.getDataProvider().getUser()));
    }

    /*
      testFilterCountInvalid():  does not consider page & size. It uses just other fields of criteria.
      So, We can't have invalid criteria for this method in api class.
     */

    @Test
    default void testIsExist() {
        this.getDataProvider().getModel(this.getDataProvider().getUser());
        boolean result = this.getRepository().isExist(this.getDataProvider().getEmptyCriteria(), this.getDataProvider().getUser());
        assertThat(result).isTrue();
    }

    @Test
    default void testIsExistNull() {
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().isExist(null, this.getDataProvider().getUser()));
    }
}
