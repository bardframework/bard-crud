crud-querydsl-sql
=================

[![Maven Central](https://img.shields.io/badge/maven--central-5.6.2-blue.svg)](https://repo1.maven.org/maven2/org/bardframework/crud/crud-querydsl-sql/)
[![License](http://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The [`crud-api`](../crud-api) repository contracts implemented over **QueryDSL SQL** — typed SQL, no
JPA, no entity manager, no lazy loading.

Part of [**Bard CRUD**](../README.md) · [Bard Framework](https://github.com/bardframework)

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-querydsl-sql</artifactId>
    <version>5.6.2</version>
</dependency>
```

## Why QueryDSL SQL rather than JPA

The queries a table screen generates are dynamic: an arbitrary subset of filters, an arbitrary sort,
a projection that should not drag half the object graph along. JPA solves a different problem —
managed entities and identity — and pays for it with lazy-loading surprises and query plans you did
not write. Here every query is explicit, every projection is a `QBean` naming exactly the columns it
reads, and what runs against the database is what you wrote.

## What you implement

Extend `BaseRepositoryQdslSqlAbstract<M, C, I, U>` (or `ReadRepositoryQdslSqlAbstract` for read-only)
and supply five things:

```java
protected abstract RelationalPathBase<?> getEntity();          // the table
protected abstract Expression<M>         getSelectExpression();// the projection
protected abstract Expression<I>         getIdSelectExpression();
protected abstract Predicate             getPredicate(C criteria, U user);
protected abstract I                     generateId(M model, U user);

protected abstract <T extends StoreClause<T>> void onSave(T clause, M model, U user);
protected abstract <T extends StoreClause<T>> void onUpdate(T clause, M model, U user);
```

Everything else — paging, counting, sorting, batch insert, patch, delete by criteria — is inherited.

```java
private static final QBean<CustomerModel> Q_BEAN = QueryDslUtils.bean(CustomerModel.class,
        tbCustomer.id, tbCustomer.email, tbCustomer.firstName);

@Override
protected Predicate getPredicate(CustomerCriteria criteria, AppUser user) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(QueryDslUtils.getPredicate(criteria.getEmailFilter(), tbCustomer.email));
    builder.and(QueryDslUtils.getPredicate(criteria.getCreditFilter(), tbCustomer.credit));
    return builder;
}
```

## QueryDslUtils

Turns a [`Filter`](https://github.com/bardframework/bard-form-parent) into a QueryDSL `Predicate`,
with an overload per filter/expression pairing:

```java
QueryDslUtils.getPredicate(stringFilter, tbCustomer.email);      // StringExpression
QueryDslUtils.getPredicate(longFilter,   tbCustomer.credit);     // NumberExpression
QueryDslUtils.getPredicate(dateFilter,   tbCustomer.createdAt);  // ComparableExpression
QueryDslUtils.bean(CustomerModel.class, path1, path2, …);        // null-skipping projection
```

> **A filter that is present but empty throws `IllegalArgumentException`.** A filter with no
> restriction would match every row — harmless in a `SELECT`, catastrophic in a `DELETE`. Pass `null`
> when you mean "no restriction"; an empty filter object is treated as a bug, not as "match all".

## Extension interfaces

`ReadExtendedRepositoryQdslSql`, `SaveExtendedRepositoryQdslSql` and
`UpdateExtendedRepositoryQdslSql` let you hook the query and the insert/update clauses — joins, audit
columns, soft deletes, tenant predicates — without rewriting the base implementation.

`OrderField` maps an incoming sort field name to a column, so `?sort=fullName,desc` can order by an
expression rather than a physical column, and unknown names raise `InvalidFieldException` instead of
being silently ignored.

## Generating Q classes

The module bundles a patched `com.querydsl.sql.codegen.MetaDataExporter` and `SQLSerializer`. Generate
the `Q` types from a live schema, or check them in — the
[sample application](https://github.com/bardframework/bard-crud-sample) checks them in, which keeps
the build offline and makes schema changes visible in review.

`EnumByNameTypeSafe` maps an enum column by name and **fails loudly on an unrecognised value** rather
than returning `null`, so a bad row surfaces as an error instead of a silent gap.

## Wiring

```java
@Bean
public SQLQueryFactory queryFactory(DataSource dataSource, SQLTemplates sqlTemplates) {
    Configuration configuration = new Configuration(sqlTemplates);
    configuration.register(new LocalDateTimeType());
    configuration.register(new LocalDateType());
    configuration.register(new LocalTimeType());
    return new SQLQueryFactory(configuration, new SpringConnectionProvider(dataSource));
}
```

`SpringConnectionProvider` ties queries to the ambient Spring transaction, so `@Transactional` works
as expected. Read methods are annotated `@Transactional(readOnly = true)`.

Paging is **1-based**: `Pageable.getPageNumber() == 1` is the first page.

## License

Apache License 2.0.
