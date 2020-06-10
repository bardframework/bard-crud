package org.bardframework.base.searchable;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQuery;
import org.bardframework.base.crud.BaseCriteria;
import org.bardframework.base.crud.BaseModelAbstract;
import org.bardframework.base.crud.ReadExtendedRepositoryQdslSql;
import org.bardframework.commons.reflection.ReflectionUtils;
import org.bardframework.commons.utils.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface SearchableEntityRepositoryQdslSql<M extends BaseModelAbstract<I>, C extends BaseCriteria<I> & SearchableCriteria, I extends Comparable<? super I>, U> extends SearchableEntityRepository<M, C, I, U>, ReadExtendedRepositoryQdslSql<C, I, U> {

    StringPath[] getSearchPaths();

    M getEmptyModel();

    Path<?> getIdentifierPath();

    <T> SQLQuery<T> setPageAndSize(SQLQuery<T> query, Pageable pageable, U user);

    SQLQuery<?> prepareQuery(C criteria, U user);

    @Transactional(readOnly = true)
    @Override
    default List<M> search(C criteria, Pageable pageable, U user) {
        List<Path<?>> selectPaths = this.getSelectOnSearchPaths();
        selectPaths.add(this.getIdentifierPath());
        SQLQuery<Tuple> query = this.prepareQuery(criteria, user).select(selectPaths.toArray(new Path<?>[0]));
        this.setPageAndSize(query, pageable, user);
        List<M> models = new ArrayList<>();
        M model;
        for (Tuple tuple : query.fetch()) {
            model = this.getEmptyModel();
            for (Path<?> searchPath : selectPaths) {
                try {
                    ReflectionUtils.setValue(model, this.getStringPath(searchPath), tuple.get(searchPath));
                } catch (Exception e) {
                    throw new IllegalStateException("can't set value", e);
                }
            }
            models.add(model);
        }
        return models;
    }

    default List<Path<?>> getSelectOnSearchPaths() {
        return new ArrayList<>(Arrays.asList(this.getSearchPaths()));
    }

    default String getStringPath(Path<?> path) {
        String stringPath = path.getMetadata().getElement().toString();
        if (!path.getRoot().equals(path.getMetadata().getParent())) {
            stringPath = path.getMetadata().getParent().getMetadata().getElement() + "." + stringPath;
        }
        return stringPath;
    }

    @Override
    default <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user) {
        if (StringUtils.hasNotText(criteria.getQuery())) {
            return query;
        }
        BooleanExpression searchExpression = this.getSearchPaths()[0].like("%" + criteria.getQuery() + "%", ' ');
        for (int i = 1; i < this.getSearchPaths().length; i++) {
            searchExpression = searchExpression.or(this.getSearchPaths()[i].like("%" + criteria.getQuery() + "%", ' '));
        }
        query.where(searchExpression);
        return query;
    }
}