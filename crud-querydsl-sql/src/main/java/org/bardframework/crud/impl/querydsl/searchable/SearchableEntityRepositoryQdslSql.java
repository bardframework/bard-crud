package org.bardframework.crud.impl.querydsl.searchable;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.SQLQuery;
import org.apache.commons.lang3.StringUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.searchable.SearchableEntityCriteria;
import org.bardframework.crud.api.searchable.SearchableEntityRepository;
import org.bardframework.crud.impl.querydsl.base.ReadExtendedRepositoryQdslSql;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface SearchableEntityRepositoryQdslSql<M extends BaseModel<I>, C extends BaseCriteria<I> & SearchableEntityCriteria, I, U> extends SearchableEntityRepository<M, C, I, U>, ReadExtendedRepositoryQdslSql<C, I, U> {

    StringPath[] getSearchPaths();

    M getEmptyModel();

    Path<?> getIdSelectExpression();

    <T> SQLQuery<T> setPageAndSize(SQLQuery<T> query, Pageable pageable, U user);

    SQLQuery<?> prepareQuery(C criteria, U user);

    @Transactional(readOnly = true)
    @Override
    default List<M> search(C criteria, Pageable pageable, U user) {
        List<Path<?>> selectPaths = this.getSelectOnSearchPaths();
        selectPaths.add(this.getIdSelectExpression());
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
        return new ArrayList<>(List.of(this.getSearchPaths()));
    }

    default String getStringPath(Path<?> path) {
        String stringPath = path.getMetadata().getElement().toString();
        if (!path.getRoot().equals(path.getMetadata().getParent())) {
            stringPath = Objects.requireNonNull(path.getMetadata().getParent()).getMetadata().getElement() + "." + stringPath;
        }
        return stringPath;
    }

    @Override
    default <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user) {
        if (StringUtils.isBlank(criteria.getQuery())) {
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
