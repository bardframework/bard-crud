crud-table
==========

[![Maven Central](https://img.shields.io/badge/maven--central-6.1.2-blue.svg)](https://repo1.maven.org/maven2/org/bardframework/crud/crud-table/)
[![License](http://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

The bridge between [`crud-api`](../crud-api) and
[`bard-table`](https://github.com/bardframework/bard-form-parent): three endpoints that turn a
`TableTemplate` plus a `BaseService` into a complete, client-renderable data grid — including Excel
export.

Part of [**Bard CRUD**](../README.md) · [Bard Framework](https://github.com/bardframework)

```xml
<dependency>
    <groupId>org.bardframework.crud</groupId>
    <artifactId>crud-table</artifactId>
    <version>6.1.2</version>
</dependency>
```

## Usage

```java
@RestController
@RequestMapping(value = "api/customer", produces = APPLICATION_JSON_VALUE)
public class CustomerRestController implements
        ReadRestController<CustomerModel, CustomerCriteria, CustomerService, String, AppUser>,
        WriteRestController<CustomerModel, CustomerDto, CustomerService, String, AppUser>,
        TableModelRestController<CustomerModel, CustomerCriteria, CustomerService, AppUser> {

    @Qualifier("CustomerPage") @Autowired
    private TableTemplate tableTemplate;

    @Override public TableTemplate getTableTemplate() { return tableTemplate; }
    @Override public boolean isRtl(Locale locale, AppUser user) { return "fa".equals(locale.getLanguage()); }
    @Override public String getExportFileName(String contentType, Locale locale, AppUser user) {
        return "customers.xlsx";
    }
}
```

The `TableTemplate` itself is a bean — usually declared in a `*Page.xml` next to the entity. See
[bard-table](https://github.com/bardframework/bard-form-parent).

## Endpoints

| Method | Path | Returns |
| --- | --- | --- |
| `GET` | `/table` | `TableModel` — headers plus the filter, save and update forms. The client renders the whole screen from this. |
| `GET` | `/table/filter` | `TableData` — rows already formatted for display (dates localised, enums translated, file sizes humanised). |
| `GET` | `/table/export` | An `.xlsx` of every row matching the criteria. |

`/table` is fetched once; `/table/filter` is fetched on every search, sort and page change.

## Why rows arrive pre-formatted

`TableData` holds display strings, not raw values. The alternative — sending raw values and
formatting on the client — means every client reimplements Jalali dates, enum translation and number
formatting, and they drift apart. Formatting once on the server keeps the Angular grid, the Android
list and the spreadsheet identical.

`HeaderTemplate` has two hooks for this: `format()` for the JSON response and `formatForExport()` for
Excel. A date can therefore render as a Jalali string on screen and as a real date cell in the
spreadsheet.

## Export

`ExcelUtils` builds the workbook with Apache POI — headers from the table template, right-to-left
sheet direction when `isRtl` says so, and cell types from each header's `excelFormat`. Export runs the
same criteria as the grid, so what the user filtered is what they download.

> Export requests an unbounded page. For very large tables, bound it — a criteria guard, a permission
> check, or an asynchronous job — before exposing the endpoint publicly.

## Testing

A `test-jar` classifier publishes `TableModelRestControllerTest` and `TableModeCheckUtils`, which
verify that every header maps to a readable property on the model and that the template's forms match
the DTO. That check catches the most common table bug — a header name that silently no longer exists
on the model — at build time.

## License

Apache License 2.0.
