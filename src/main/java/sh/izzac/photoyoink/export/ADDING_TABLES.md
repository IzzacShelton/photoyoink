## Adding new tables/tuples

This export system is table-driven. To add a new table (or change exported columns), you typically only touch:
- `sh.izzac.photoyoink.export.ExportRegistry`
- (optionally) `sh.izzac.photoyoink.export.MetadataExtractionService`
- (optionally) add a new `...export.model.*` record if you want a strongly-typed model

### Steps
1. **Decide the VALUES tuple columns** you want to generate (order matters).
2. Add a `TupleFormat.TableSpec<YourModel>` to `ExportRegistry`:
   - `tableName`
   - `List<TupleFormat.ColumnSpec<YourModel>>` for each column
3. If the value is already present in an existing model, point the column at it.
4. If it’s not present, extend `MetadataExtractionService` to extract the field from `Metadata`.
5. Extraction functions should return `Optional.empty()` for missing values; those render as `NULL`.

### Making it appear in the UI context menu
The metadata tree context menu is populated from `ExportRegistry.exportTargets()`.

To add a new copy action:
1. Add (or update) a `TupleFormat.TableSpec` in `ExportRegistry`.
2. Add a corresponding `ExportTarget` entry in `ExportRegistry.exportTargets()` that returns the tuple text (or `Optional.empty()` when not available).

### Example pattern (ColumnSpec)
`new TupleFormat.ColumnSpec<>(\"SomeColumn\", model -> model.someOptionalField())`

### Notes
- The UI currently exports **one image at a time** and copies **one tuple** per table from the context menu.
  If you add batch selection later, you can collect tuples into a list and join them with `,\n` under a single `INSERT ... VALUES` (if you ever decide to emit INSERTs again).

