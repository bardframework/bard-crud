Bard CRUD
=========

[![Maven Central](https://img.shields.io/badge/maven--central-6.1.2-blue.svg)](https://repo1.maven.org/maven2/org/bardframework/crud/)
[![License](http://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

A CRUD stack for Spring Boot where the REST layer is **interfaces with default methods**, queries are
built from **typed filter objects**, and the data layer is **QueryDSL SQL** — no JPA, no entity
manager, no lazy-loading surprises.

Adding an entity means writing five small classes. You write zero endpoint code and zero SQL strings.

`groupId` `org.bardframework.crud`, version **5.6.2** (managed by [
`bard-bom`](https://github.com/bardframework/bard-bom)).

## Artifacts

| Artifact | Contents |
| --- | --- |
| `crud-api` | Contracts and base classes: model, criteria, repository, service, REST controllers. No persistence technology. |
| `crud-querydsl-sql` | `ReadRepositoryQdslSqlAbstract` / `BaseRepositoryQdslSqlAbstract` — the QueryDSL-SQL implementation, plus `QueryDslUtils` (filter → `Predicate`). |
| `crud-table` | `TableModelRestController` — serves a table descriptor, paged table data, and an `.xlsx` export. Bridges `crud` and [`bard-table`](https://github.com/bardframework/bard-form-parent). |

Test fixtures ship as `test-jar` classifiers on `crud-api` and `crud-table`.

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-querydsl-sql</artifactId>
</dependency>
```

## The six types per entity

```
CustomerModel       what comes out    (BaseModelAbstract<String>)
CustomerDto         what goes in      (plain POJO)
CustomerCriteria    how you query it  (BaseCriteriaAbstract<String> + Filter fields)
CustomerRepository  persistence       (BaseRepository)
CustomerService     business logic    (BaseService, with lifecycle hooks)
CustomerRestController  transport     (implements the controller interfaces)
```

Note that **model and DTO are separate on purpose**: the shape you accept is not the shape you
return, so a client can never write a field you did not intend to expose.

Every signature carries the current user explicitly — `get(id, user)`, `save(dto, user)` — rather
than reading a thread-local. Repository and service implementations therefore get row-level security
as a parameter they cannot forget.

## Type parameters

```java
BaseService  <M, C, D, R, I, U>   // Model, Criteria, Dto, Repository, Id, User
BaseRepository<M, C, I, U>
BaseCrudRestControllerAbstract<M, C, D, S, I, U>
```

The base classes resolve `M`, `C` and `D` from your subclass at construction time via
`ReflectionUtils.getGenericArgType`, so `getEmptyCriteria()` and friends work without you passing
`Class` objects around.

## A complete entity

**Model and DTO**

```java
@Getter @Setter
public class CustomerModel extends BaseModelAbstract<String> {
    private String email;
    private String firstName;
    private String lastName;
}

@Getter @Setter
public class CustomerDto {
    private String email;
    private String firstName;
    private String lastName;
}
```

**Criteria** — filters, not raw values:

```java
@Getter @Setter
public class CustomerCriteria extends BaseCriteriaAbstract<String> {
    private StringFilter searchFilter;
    private LongFilter creditFilter;
}
```

**Repository** — describe the table, the projection, and how criteria become predicates:

```java
@Repository
public class CustomerRepositoryQdslSqlImpl
        extends BaseRepositoryQdslSqlAbstract<CustomerModel, CustomerCriteria, String, AppUser>
        implements CustomerRepository {

    private static final QBean<CustomerModel> Q_BEAN = QueryDslUtils.bean(CustomerModel.class,
            tbCustomer.id, tbCustomer.email, tbCustomer.firstName, tbCustomer.lastName);

    public CustomerRepositoryQdslSqlImpl(SQLQueryFactory queryFactory) { super(queryFactory); }

    @Override protected RelationalPathBase<?> getEntity()            { return tbCustomer; }
    @Override protected QBean<CustomerModel> getSelectExpression()   { return Q_BEAN; }
    @Override protected Expression<String> getIdSelectExpression()   { return tbCustomer.id; }
    @Override protected String generateId(CustomerModel m, AppUser u){ return UUID.randomUUID().toString(); }

    @Override
    protected Predicate getPredicate(CustomerCriteria criteria, AppUser user) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.orAllOf(
            QueryDslUtils.getPredicate(criteria.getSearchFilter(), tbCustomer.firstName),
            QueryDslUtils.getPredicate(criteria.getSearchFilter(), tbCustomer.lastName));
        return builder;
    }

    @Override protected <T extends StoreClause<T>> void onSave(T clause, CustomerModel m, AppUser u) {
        clause.set(tbCustomer.id, m.getId());
        this.onUpdate(clause, m, u);
    }

    @Override protected <T extends StoreClause<T>> void onUpdate(T clause, CustomerModel m, AppUser u) {
        clause.set(tbCustomer.email, m.getEmail());
        clause.set(tbCustomer.firstName, m.getFirstName());
        clause.set(tbCustomer.lastName, m.getLastName());
    }
}
```

**Service** — only the DTO↔model mapping is mandatory:

```java
@Service
public class CustomerService
        extends BaseService<CustomerModel, CustomerCriteria, CustomerDto, CustomerRepository, String, AppUser> {

    public CustomerService(CustomerRepository repository) { super(repository); }

    @Override protected CustomerModel onSave(CustomerDto dto, AppUser user) {
        CustomerModel m = new CustomerModel();
        m.setEmail(dto.getEmail());
        m.setFirstName(dto.getFirstName());
        return m;
    }

    @Override protected void onUpdate(CustomerDto dto, CustomerModel entity, AppUser user) {
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
    }
}
```

**Controller** — implement the interfaces you want; the endpoints come with them:

```java
@RestController
@RequestMapping(value = "api/customer", produces = APPLICATION_JSON_VALUE)
public class CustomerRestController implements
        ReadRestController<CustomerModel, CustomerCriteria, CustomerService, String, AppUser>,
        WriteRestController<CustomerModel, CustomerDto, CustomerService, String, AppUser> {

    private final CustomerService service;
    CustomerRestController(CustomerService service) { this.service = service; }

    @Override public CustomerService getService() { return service; }
    @Override public AppUser getUser() { return SecurityUtils.currentUser(); }
}
```

That is the whole HTTP layer. `implements ReadRestController` alone gives you a read-only resource;
add `WriteRestController` when you want mutation. `BaseCrudRestControllerAbstract` is a convenience
class that implements both.

## Endpoints you get

| Method | Path | From | Body / params |
| --- | --- | --- | --- |
| `GET` | `/{id}` | `ReadRestController` | → model, or `404` |
| `GET` | `/filter` | `ReadRestController` | `?firstName.contains=al&page=1&size=20&sort=id,desc` → `PagedData<M>` |
| `POST` | `` | `WriteRestController` | DTO, validated with group `ValidationGroups.Save` |
| `PUT` | `/{id}` | `WriteRestController` | DTO, validated with group `ValidationGroups.Update` |
| `PATCH` | `/{id}` | `WriteRestController` | `application/json-merge-patch`, a `Map<String, Object>` |
| `DELETE` | `/{id}` | `WriteRestController` | → deleted count, or `404` |
| `GET` | `/table` | `TableModelRestController` | → `TableModel` (headers + filter/save/update forms) |
| `GET` | `/table/filter` | `TableModelRestController` | criteria → `TableData` (formatted rows) |
| `GET` | `/table/export` | `TableModelRestController` | criteria → `.xlsx` |

`PagedData<M>` serialises as `{ "result": [...], "total": 123 }`. Pages are **1-based**.

## Filters

A criteria object is composed of `Filter` instances, which map onto query-string parameters. This
keeps queries expressive without exposing a query language.

| Filter | Applies to | Operations |
| --- | --- | --- |
| `Filter<T, F>` (base) | anything | `equals`, `notEquals`, `specified`, `in`, `notIn` |
| `RangeFilter<T, F>` | comparables | the above plus `from`, `to` |
| `StringFilter` | text | the above plus `contains`, `doesNotContain`, `startWith`, `endWith` |
| `NumberRangeFilter` | numbers | `IntegerFilter`, `LongFilter`, `ShortFilter`, `ByteFilter`, `DoubleFilter`, `FloatFilter`, `BigDecimalFilter`, `BigIntegerFilter` |
| others | | `BooleanFilter`, `EnumFilter`, `InstantFilter`, `LocalTimeFilter`, `DurationFilter`, `IdFilter` |

```
GET /api/customer/filter?firstName.contains=ali&credit.from=1000&status.in=ACTIVE,PENDING
```

`QueryDslUtils.getPredicate(filter, expression)` turns each one into a QueryDSL `Predicate`.

> **Safety rule:** a filter object that is present but has *no* restriction set throws
> `IllegalArgumentException` rather than matching everything. An accidental "match all" in a delete
> path is a data-loss bug; Bard makes you pass `null` when you really mean "no restriction".

## Service lifecycle hooks

`BaseService` calls these around every operation — override the ones you need:

```
save    preSave(dto)   → onSave(dto) → [repository] → postSave(model, dto)
update  preUpdate(...) → onUpdate(dto, entity) → [repository] → postUpdate(...)
patch   prePatch(...)  → [repository] → postPatch(...)
delete  preDelete(...) → [repository] → postDelete(...)
read    preFetch(criteria) → [repository] → postFetch(model)
```

`postFetch` is the natural place to enrich a model with data from another service; `preDelete` is
where you cascade or veto. Delete resolves the criteria to concrete ids first, so a `preDelete` hook
that changes state cannot widen the deletion.

## Entity flavours

Beyond the base contracts, `crud-api` ships three ready-made specialisations:

| Package | Interfaces | Behaviour |
| --- | --- | --- |
| `activable` | `ActivableEntityCriteria/Repository/Service/RestController` | Entities with an enable/disable flag. Adds `PUT /{id}/enable` and `PUT /{id}/disable`. |
| `searchable` | `SearchableEntity*` | A single free-text `query` across a repository-defined set of columns. Adds `POST /search` (criteria in the body, pageable) — for autocomplete and quick search. |
| `tree` | `TreeEntity*`, `TreeEntityModelAbstract` | Parent/children hierarchies. Adds `GET /tree` and `GET /{rootId}/tree`, returning a populated subtree. |

## QueryDSL setup

`crud-querydsl-sql` needs an `SQLQueryFactory` and the generated `Q` classes for your tables:

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

The module bundles a patched `com.querydsl.sql.codegen.MetaDataExporter` and `SQLSerializer` — the Q
classes can be generated from a live schema or checked in (the sample repository checks them in).

`EnumByNameTypeSafe` maps an enum column by name while failing loudly on an unknown value instead of
silently returning `null`.

## Validation

`ValidationGroups.Save` and `ValidationGroups.Update` are applied by the controller interfaces, so
the same DTO can require different things on create and on edit. Pair with
[`bard-validator`](https://github.com/bardframework/bard-validator) for XML-declared rules, or use
Jakarta Bean Validation annotations — both work.

## Testing

`crud-api` and `crud-table` publish `test-jar` artifacts with abstract contract tests
(`BaseRepositoryTest`, `BaseServiceTest`, `ReadRestControllerTest`, `WriteRestControllerTest`,
`TableModelRestControllerTest`). Extend them and supply a `RepositoryDataProvider` /
`ServiceDataProvider` to have your entity's whole surface exercised:

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-api</artifactId>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

## See also

* [bard-crud-sample](https://github.com/bardframework/bard-crud-sample) — a runnable application.
* [bard-form-parent](https://github.com/bardframework/bard-form-parent) — the table/form descriptors that `crud-table` serves.

## License

Apache License 2.0.
