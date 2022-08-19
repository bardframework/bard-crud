package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Created by Sama-PC on 09/05/2017.
 */
public interface BaseRepositoryTest<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, R extends BaseRepository<M, C, I, U>, P extends DataProviderRepository<M, C, R, I, U>, I extends Comparable<? super I>, U> {

    Logger LOGGER = LoggerFactory.getLogger(BaseRepositoryTest.class);

    R getRepository();

    P getDataProvider();

    @Test
    default void testGetByIdValid() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        LOGGER.debug("test get by id '{}'.", id);
        M result = this.getRepository().get(id, user);
        LOGGER.debug("get by id '{}', result is '{}'.", id, result);
        assertThat(result).isNotNull();
        assertThat(id).isEqualTo(result.getId());
    }

    @Test
    default void testGetByIdInvalid() {
        U user = this.getDataProvider().getUser();
        I invalidId = this.getDataProvider().getInvalidId();
        LOGGER.debug("test get by invalid id '{}'.", invalidId);
        M result = this.getRepository().get(invalidId, user);
        LOGGER.debug("get by invalid id '{}', result is '{}'.", invalidId, result);
        assertThat(result).isNull();
    }

    @Test
    default void testGetByIdNull() {
        LOGGER.debug("test get by null id'.");
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().get((I) null, user);
            LOGGER.error("get by null id, expect exception but result is '{}'.", result);
        });
    }

    @Test
    default void testGetByIdsDuplicate() {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        List<I> duplicateIds = Arrays.asList(id, id, id, id, id);
        LOGGER.debug("test get by duplicate ids '{}'.", duplicateIds);
        List<M> result = this.getRepository().get(duplicateIds, user);
        LOGGER.debug("get by duplicate ids '{}', result  is '{}'.", duplicateIds, result);
        assertThat(result).hasSize(1).map(BaseModel::getId).containsOnly(id);
    }

    @Test
    default void testGetByIdsNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = this.getRepository().get((List<I>) null, user);
            LOGGER.error("get by null ids, expect exception but result is'{}'.", result);
        });
    }

    @Test
    default void testGetByIdsEmpty() {
        U user = this.getDataProvider().getUser();
        assertThatThrownBy(() -> this.getRepository().get(List.of(), user));
    }

    @Test
    default void testGetByCriteria() {
        /*
          to be sure at least one entity exist.
         */
        U user = this.getDataProvider().getUser();
        this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getEmptyCriteria();
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> result = this.getRepository().get(criteria, user);
        LOGGER.debug("get db by criteria '{}', result is '{}'.", criteria, result);
        assertThat(result).size().isGreaterThan(0);
    }

    @Test
    default void testGetByCriteriaNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            List<M> result = this.getRepository().get((C) null, user);
            LOGGER.error("get by null criteria, expect exception but result is '{}'.", result);
        });
    }

    /**
     * Check whether notIn field is correctly working in the case of get(Criteria c).
     */
    @Test
    default void testGetByCriteriaNotIn() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getModel(user);
        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for get(Criteria c) method. */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setIdFilter(new IdFilter<I>().setNotIn(ids));
        LOGGER.debug("get by criteria '{}'.", criteria);
        List<M> foundEntities = this.getRepository().get(criteria, user);
        LOGGER.debug("get by criteria '{}', result is '{}'.", criteria, foundEntities);
        assertThat(foundEntities).map(BaseModel::getId).doesNotContainAnyElementsOf(ids);
    }

    @Test
    default void testGetOneNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getOne(null, user));
    }

    /*----------------------- Delete ---------------------*/
    @Test
    default void testDeleteByCriteriaNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().delete((C) null, user));
    }

    /**
     * invalid criteria for delete is meaningless
     */

    /*----------------------- Save ---------------------*/
    @Test
    default void testSave() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getUnsavedModel();
        LOGGER.debug("saving '{}'", model);
        M result = this.getRepository().save(model, user);
        LOGGER.debug("save '{}', result is '{}'.", model, result);
        this.getDataProvider().assertEqualSave(model, result);
    }

    @Test
    default void testSaveInvalid() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getUnsavedInvalidModel();
        LOGGER.debug("saving '{}'", model);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().save(model, user);
            LOGGER.debug("save invalid model '{}', expect exception but result is '{}'.", model, result);
        });
    }

    @Test
    default void testSaveNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            M result = this.getRepository().save((M) null, user);
            LOGGER.debug("save null model, expect exception but result is '{}'.", result);
        });
    }

    @Test
    default void testSaveCollection() {
        U user = this.getDataProvider().getUser();
        List<M> list = this.getDataProvider().getUnsavedModels(RandomUtils.nextInt(1, 10), user);
        Collection<M> result = this.getRepository().save(list, user);
        assertThat(result.size()).isEqualTo(list.size());
        for (M savedEntity : result) {
            M entity = list.get(list.indexOf(savedEntity));
            this.getDataProvider().assertEqualSave(entity, savedEntity);
        }
    }

    @Test
    default void testSaveCollectionEmpty() {
        U user = this.getDataProvider().getUser();
        List<M> list = new ArrayList<>();
        List<M> saved = this.getRepository().save(list, user);
        /*
          i.e no model has been saved to the database.
          */
        assertThat(saved).hasSize(0);
    }

    @Test
    default void testSaveCollectionInvalidMember() {
        U user = this.getDataProvider().getUser();
        final int count = RandomUtils.nextInt(1, 10);
        List<M> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(this.getDataProvider().getUnsavedModel());
        }
        // insert several invalid models in random positions
        for (int i = 0; i < RandomUtils.nextInt(1, 3); i++) {
            list.add(RandomUtils.nextInt(0, list.size() - 1), this.getDataProvider().getUnsavedInvalidModel());
        }
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().save(list, user));
    }

    @Test
    default void testSaveCollectionNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().save((List<M>) null, user));
    }
    /*----------------------- Update ---------------------*/

    @Test
    default void testUpdate() {
        U user = this.getDataProvider().getUser();
        M sourceModel = this.getDataProvider().getModel(user);
        M updatedModel = this.getRepository().update(sourceModel, user);
        this.getDataProvider().assertEqualUpdate(sourceModel, updatedModel);
    }

    @Test
    default void testUpdateInvalidModel() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update(this.getDataProvider().getInvalidModel(user), user));
    }

    @Test
    default void testUpdateNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update((M) null, user));
    }

    @Test
    default void testUpdateUnsavedModel() {
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getUnsavedModel();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().update(model, user));
    }

    /*---------------------- Filter ------------------------*/
    @Test
    default void testFilter() {
        U user = this.getDataProvider().getUser();
        int dataCount = RandomUtils.nextInt(1, 3);
        this.getDataProvider().getModels(dataCount, user);
        C validFilter = this.getDataProvider().getEmptyCriteria();
        PagedData<M> pagedData = this.getRepository().get(validFilter, PageRequest.of(0, dataCount), user);
        assertThat(pagedData.getTotal()).isGreaterThanOrEqualTo(dataCount);
        assertThat(pagedData.getData()).isNotEmpty();
    }

    @Test
    default void testFilterNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> this.getRepository().get(null, PageRequest.of(1, Integer.MAX_VALUE), user));
    }

    @Test
    default void testFilterIds() {
        U user = this.getDataProvider().getUser();
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(user);
        assertThat(this.getRepository().getIds(this.getDataProvider().getEmptyCriteria(), user)).size().isGreaterThanOrEqualTo(1);
    }

    @Test
    default void testFilterIdsNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getIds(null, user));
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
        U user = this.getDataProvider().getUser();
        M model = this.getDataProvider().getModel(user);

        List<I> ids = Collections.singletonList(model.getId());
        /* Page & size are neutral for filterIds(). */
        C criteria = this.getDataProvider().getEmptyCriteria();
        criteria.setIdFilter(new IdFilter<I>().setNotIn(ids));
        List<I> list = this.getRepository().getIds(criteria, user);
        assertThat(list).doesNotContain(model.getId());
    }

    @Test
    default void testFilterCount() {
        U user = this.getDataProvider().getUser();
        /* Make sure that at least one record exists. */
        this.getDataProvider().getModel(user);
        assertThat(this.getRepository().getCount(this.getDataProvider().getEmptyCriteria(), user)).isGreaterThanOrEqualTo(1);
    }

    @Test
    default void testFilterCountNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().getCount(null, user));
    }

    /*
      testFilterCountInvalid():  does not consider page & size. It uses just other fields of criteria.
      So, We can't have invalid criteria for this method in api class.
     */

    @Test
    default void testIsExist() {
        U user = this.getDataProvider().getUser();
        this.getDataProvider().getModel(user);
        boolean result = this.getRepository().isExist(this.getDataProvider().getEmptyCriteria(), user);
        assertThat(result).isTrue();
    }

    @Test
    default void testIsExistNull() {
        U user = this.getDataProvider().getUser();
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> this.getRepository().isExist(null, user));
    }
}
