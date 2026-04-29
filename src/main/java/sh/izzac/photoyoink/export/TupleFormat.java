package sh.izzac.photoyoink.export;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


public final class TupleFormat {
    private TupleFormat() {}

    public record ColumnSpec<TModel>(String columnName, Function<TModel, Optional<?>> extractor) {
        public ColumnSpec {
            if (columnName == null || columnName.isBlank()) throw new IllegalArgumentException("columnName must be non-empty");
            if (extractor == null) throw new IllegalArgumentException("extractor must be non-null");
        }
    }

    public record TableSpec<TModel>(String tableName, List<ColumnSpec<TModel>> columns) {
        public TableSpec {
            if (tableName == null || tableName.isBlank()) 
                throw new IllegalArgumentException("tableName must be non-empty");
            if (columns == null || columns.isEmpty()) 
                throw new IllegalArgumentException("columns must be non-empty");
        }
    }

    public static <TModel> String renderTuple(TableSpec<TModel> table, TModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < table.columns().size(); i++) {
            ColumnSpec<TModel> col = table.columns().get(i);
            Optional<?> v = col.extractor().apply(model);
            sb.append(lit(v.orElse(null)));
            if (i + 1 < table.columns().size()) sb.append(", ");
        }
        sb.append(')');
        return sb.toString();
    }

    /** Convert Java values to MySQL literal (or NULL). */
    public static String lit(Object value) {
        if (value == null) return "NULL";
        if (value instanceof Optional<?> opt) 
            return lit(opt.orElse(null));
        if (value instanceof String s) 
            return str(s);
        if (value instanceof Integer i)
            return i.toString();
        if (value instanceof Long l) 
            return l.toString();
        if (value instanceof BigDecimal bd) 
            return dec(bd, bd.scale());
        if (value instanceof Number n) 
            return n.toString();
        // otherwise stringify and quote
        return str(value.toString());
    }

    public static String str(String value) {
        if (value == null) return "NULL";
        String normalized = value
                .replace('\\', '/')
                .replace("'", "''")
                .replaceAll("[\r\n\t]", " ");
        return "'" + normalized + "'";
    }

    public static String dec(BigDecimal value, int scale) {
        if (value == null) return "NULL";
        BigDecimal scaled = value.setScale(scale, RoundingMode.HALF_UP);
        return scaled.toPlainString();
    }

    public static Optional<Integer> i32(Integer value) {
        return Optional.ofNullable(value);
    }
}

