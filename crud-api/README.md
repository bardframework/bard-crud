crud-api
========

[![Maven Central](https://img.shields.io/badge/maven--central-5.3.2-blue.svg)](https://repo1.maven.org/maven2/org/bardframework/crud/crud-api/)
[![License](http://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The CRUD contracts and base classes, with **no persistence technology attached**. Depend on this from
your domain and service layer; pick an implementation ([`crud-querydsl-sql`](../crud-querydsl-sql))
separately.

Part of [**Bard CRUD**](../README.md) · [Bard Framework](https://github.com/bardframework)

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-api</artifactId>
    <version>5.3.2</version>
</dependency>
```

## Package map

```
org.bardframework.crud.api
├── base          the core contracts — start here
├── activable     entities with an enable/disable flag
├── searchable    entities with a free-text search endpoint
└── tree          parent/children hierarchies
```

## base

| Type | Role |
| --- | --- |
| `BaseModel<I>` / `BaseModelAbstract<I>` | What the API returns. `equals`/`hashCode` by id. |
| `BaseCriteria<I>` / `BaseCriteriaAbstract<I>` | What the API queries by — a bag of `Filter` objects. |
| `ReadRepository<M, C, I, U>` | `get`, `getList`, `getOne`, `getFirst`, `getIds`, `getCount`, `isExist`, `isNotExist`. |
| `BaseRepository<M, C, I, U>` | Adds `save`, `update`, `patch`, `delete`. |
| `ReadService<M, C, R, I, U>` | Read side plus `preFetch` / `postFetch` hooks. |
| `BaseService<M, C, D, R, I, U>` | Write side plus the full hook set; the only abstract methods are `onSave(dto)` and `onUpdate(dto, entity)`. |
| `ReadRestController` | `GET /{id}`, `GET /filter`. |
| `WriteRestController` | `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`. |
| `BaseCrudRestControllerAbstract` | Both of the above, for when you want a class rather than interfaces. |
| `PagedData<M>` | `{ "result": [...], "total": n }`. |
| `ValidationGroups` | `Save` and `Update` marker interfaces. |
| `InvalidFieldException` | Thrown when a request asks for a projection field (`getList(criteria, pageable, fields, user)`) that maps to no column. |

The REST controllers are **interfaces with default methods**, so a controller implements the ones it
wants and inherits the endpoints. A read-only resource is `implements ReadRestController` and nothing
else.

## Type parameters, once

```
M  model      what goes out
C  criteria   how it is queried
D  dto        what comes in
R  repository
I  id
U  user       passed explicitly to every operation, never read from a thread-local
```

`ReadService` and `BaseService` resolve `M`, `C` and `D` from your subclass at construction time via
`ReflectionUtils.getGenericArgType`, so `getEmptyCriteria()` works without any `Class` plumbing.

## Service hooks

```
save    preSave(dto)      → onSave(dto)          → repository → postSave(model, dto)
update  preUpdate(model)  → onUpdate(dto, model) → repository → postUpdate(...)
patch   prePatch(...)                            → repository → postPatch(...)
delete  preDelete(...)                           → repository → postDelete(...)
read    preFetch(criteria)                       → repository → postFetch(model)
```

Delete resolves the criteria to concrete ids *before* deleting, so a `preDelete` hook that changes
state cannot accidentally widen what gets removed.

## Entity flavours

| Package | Adds |
| --- | --- |
| `activable` | `PUT /{id}/enable`, `PUT /{id}/disable`, and criteria/repository support for the flag. |
| `searchable` | `POST /search` taking a criteria with a free-text `query` and a `Pageable`. |
| `tree` | `GET /tree` and `GET /{rootId}/tree`, returning a populated subtree via `TreeEntityModel`. |

## Testing

A `test-jar` classifier publishes abstract contract tests. Extend them, supply a data provider, and
your entity's whole surface is exercised:

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-api</artifactId>
    <version>5.3.2</version>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

`BaseRepositoryTest` · `BaseServiceTest` · `ReadRestControllerTest` · `WriteRestControllerTest` ·
`RepositoryDataProvider` · `ServiceDataProvider`

## A note on `GenericRequestBodyResolverConfiguration`

`crud-api` ships one class under `org.springframework.web.servlet.mvc.method.annotation` — Spring's
own package, because it needs package-private access. On startup it replaces Spring's
`RequestResponseBodyMethodProcessor` with a subclass that resolves the *concrete* type argument of a
generic `@RequestBody` parameter from the implementing controller.

Without it, `WriteRestController<M, D, S, I, U>.SAVE(@RequestBody D dto)` would deserialise into the
erased type. With it, `D` resolves to `CustomerDto` for `CustomerRestController`. This is what makes
generic default-method controllers possible at all.

Register it if your controllers use the generic interfaces:

```java
@Bean
GenericRequestBodyResolverConfiguration genericRequestBodyResolver(RequestMappingHandlerAdapter adapter) {
    return new GenericRequestBodyResolverConfiguration(adapter);
}
```

## License

Apache License 2.0.
