package omni.storage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import omni.core.ConversionRecord;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 5 / Lab 16 : JDBC CRUD Persistence
 *
 *  Full Create / Read / Update / Delete operations implemented against
 *  standard JDBC interfaces (Connection, PreparedStatement, Statement,
 *  ResultSet).  The underlying storage is an in-memory ArrayList backed
 *  by JDK dynamic proxies (java.lang.reflect.Proxy) so that no external
 *  database driver JAR is required — the project compiles and runs with
 *  pure JDK 8+.
 *
 *  The CRUD methods use real PreparedStatement parameter binding so the
 *  code is directly portable to MySQL / PostgreSQL / Derby by simply
 *  swapping the createConnection() implementation.
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ConversionDB {

    /* ── Column names (match the "table schema") ── */
    private static final String[] COLUMNS = {
        "id", "file_name", "file_size", "source_format",
        "target_format", "efficiency_score", "remarks", "timestamp"
    };

    /** In-memory row storage — each Map represents one database row. */
    private final List<Map<String, Object>> table =
            Collections.synchronizedList(new ArrayList<>());
    private int nextId = 1;

    public ConversionDB() {
        /* Table "created" in-memory — ready for CRUD. */
    }

    /* ══════════════════════════════════════════════════════════════════════
     *  C R U D   O P E R A T I O N S
     * ══════════════════════════════════════════════════════════════════════ */

    /* ── CREATE : Insert a conversion record ─────────────────────────── */
    public void insertRecord(ConversionRecord rec) throws SQLException {
        Connection conn = createConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO conversion_logs "
              + "(id, file_name, file_size, source_format, target_format, "
              + " efficiency_score, remarks, timestamp) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, nextId++);
            ps.setString(2, rec.getFileName());
            ps.setLong(3, rec.getFileSizeBytes());
            ps.setString(4, rec.getSourceFormat());
            ps.setString(5, rec.getTargetFormat());
            ps.setDouble(6, rec.getEfficiencyScore());
            ps.setString(7, rec.getRemarks());
            ps.setString(8, rec.getTimestamp());
            ps.executeUpdate();
            ps.close();
        } finally {
            conn.close();
        }
    }

    /* ── READ : High-efficiency records (Lab 16 exact query) ─────── */
    public List<ConversionRecord> readHighEfficiency() throws SQLException {
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM conversion_logs WHERE efficiency_score > 70.0");
            List<ConversionRecord> results = extractRecords(rs);
            rs.close();
            stmt.close();
            return results;
        } finally {
            conn.close();
        }
    }

    /* ── READ : All records ──────────────────────────────────────────── */
    public List<ConversionRecord> readAll() throws SQLException {
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM conversion_logs");
            List<ConversionRecord> results = extractRecords(rs);
            rs.close();
            stmt.close();
            return results;
        } finally {
            conn.close();
        }
    }

    /* ── UPDATE : Change remarks for a given record ID ───────────── */
    public int updateRemarks(int id, String newRemarks) throws SQLException {
        Connection conn = createConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE conversion_logs SET remarks = ? WHERE id = ?");
            ps.setString(1, newRemarks);
            ps.setInt(2, id);
            int affected = ps.executeUpdate();
            ps.close();
            return affected;
        } finally {
            conn.close();
        }
    }

    /* ── DELETE : Clear all conversion history ────────────────────── */
    public int deleteAll() throws SQLException {
        Connection conn = createConnection();
        try {
            Statement stmt = conn.createStatement();
            int affected = stmt.executeUpdate("DELETE FROM conversion_logs");
            stmt.close();
            return affected;
        } finally {
            conn.close();
        }
    }

    /** @return current number of rows in the table. */
    public int getRecordCount() {
        return table.size();
    }

    /* ── Helper : build ConversionRecord list from a ResultSet ─────── */
    private List<ConversionRecord> extractRecords(ResultSet rs)
            throws SQLException {
        List<ConversionRecord> list = new ArrayList<>();
        while (rs.next()) {
            ConversionRecord rec = new ConversionRecord(
                rs.getString("file_name"),
                rs.getLong("file_size"),
                rs.getString("source_format"),
                rs.getString("target_format"),
                rs.getDouble("efficiency_score"));
            rec.setRemarks(rs.getString("remarks"));
            rec.setTimestamp(rs.getString("timestamp"));
            list.add(rec);
        }
        return list;
    }

    /* ══════════════════════════════════════════════════════════════════════
     *  I N - M E M O R Y   J D B C   P R O X Y   E N G I N E
     *
     *  Uses java.lang.reflect.Proxy to create lightweight implementations
     *  of Connection, Statement, PreparedStatement, and ResultSet that
     *  operate against the in-memory ArrayList.  This eliminates the need
     *  for any external database driver while still exercising the full
     *  JDBC API surface.
     * ══════════════════════════════════════════════════════════════════════ */

    /** Create a proxy Connection backed by in-memory storage. */
    private Connection createConnection() {
        return (Connection) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ Connection.class },
            new ConnectionHandler());
    }

    /* ── Connection proxy handler ─────────────────────────────────────── */
    private class ConnectionHandler implements InvocationHandler {
        private boolean closed = false;

        @Override
        public Object invoke(Object proxy, Method method,
                             Object[] args) throws Throwable {
            switch (method.getName()) {
                case "prepareStatement":
                    return makePreparedStatement((String) args[0]);
                case "createStatement":
                    return makeStatement();
                case "close":
                    closed = true;
                    return null;
                case "isClosed":
                    return closed;
                case "toString":
                    return "InMemoryConnection[rows=" + table.size() + "]";
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "isWrapperFor":
                    return false;
                case "unwrap":
                    throw new SQLException("Not a wrapper");
                default:
                    return null;            // safe no-op for unused methods
            }
        }
    }

    /* ── Statement proxy ──────────────────────────────────────────────── */
    private Statement makeStatement() {
        return (Statement) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ Statement.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "executeQuery":
                        return buildResultSet(filterRows((String) args[0]));
                    case "executeUpdate":
                        return execDml((String) args[0]);
                    case "close":
                        return null;
                    case "toString":
                        return "InMemoryStatement";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return null;
                }
            });
    }

    /* ── PreparedStatement proxy ───────────────────────────────────────── */
    private PreparedStatement makePreparedStatement(String sql) {
        final Map<Integer, Object> params = new HashMap<>();

        return (PreparedStatement) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ PreparedStatement.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setString":
                        params.put((Integer) args[0], args[1]);
                        return null;
                    case "setInt":
                        params.put((Integer) args[0], args[1]);
                        return null;
                    case "setLong":
                        params.put((Integer) args[0], args[1]);
                        return null;
                    case "setDouble":
                        params.put((Integer) args[0], args[1]);
                        return null;
                    case "executeUpdate":
                        return execPreparedDml(sql, params);
                    case "executeQuery":
                        return buildResultSet(filterRows(sql));
                    case "close":
                        return null;
                    case "toString":
                        return "InMemoryPS[" + sql + "]";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return null;
                }
            });
    }

    /* ── ResultSet proxy ──────────────────────────────────────────────── */
    private ResultSet buildResultSet(List<Map<String, Object>> rows) {
        final Iterator<Map<String, Object>> iter = rows.iterator();
        @SuppressWarnings("unchecked")
        final Map<String, Object>[] current = new Map[1];

        return (ResultSet) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ ResultSet.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "next":
                        if (iter.hasNext()) {
                            current[0] = iter.next();
                            return true;
                        }
                        return false;

                    case "getString":
                        return asString(current[0].get(colName(args[0])));

                    case "getInt": {
                        Object v = current[0].get(colName(args[0]));
                        return (v instanceof Number) ? ((Number) v).intValue() : 0;
                    }
                    case "getLong": {
                        Object v = current[0].get(colName(args[0]));
                        return (v instanceof Number) ? ((Number) v).longValue() : 0L;
                    }
                    case "getDouble": {
                        Object v = current[0].get(colName(args[0]));
                        return (v instanceof Number) ? ((Number) v).doubleValue() : 0.0;
                    }
                    case "close":
                        return null;
                    case "toString":
                        return "InMemoryResultSet";
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "equals":
                        return proxy == args[0];
                    default:
                        return null;
                }
            });
    }

    /* ══════════════════════════════════════════════════════════════════════
     *  Lightweight SQL Interpretation
     * ══════════════════════════════════════════════════════════════════════ */

    /** Execute a prepared DML (INSERT or UPDATE). */
    private int execPreparedDml(String sql, Map<Integer, Object> params) {
        String upper = sql.trim().toUpperCase();

        if (upper.startsWith("INSERT")) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",               params.get(1));
            row.put("file_name",        params.get(2));
            row.put("file_size",        params.get(3));
            row.put("source_format",    params.get(4));
            row.put("target_format",    params.get(5));
            row.put("efficiency_score", params.get(6));
            row.put("remarks",          params.get(7));
            row.put("timestamp",        params.get(8));
            table.add(row);
            return 1;

        } else if (upper.startsWith("UPDATE")) {
            /* UPDATE … SET remarks = ?1 WHERE id = ?2 */
            int    targetId   = ((Number) params.get(2)).intValue();
            String newRemarks = (String)  params.get(1);
            for (Map<String, Object> row : table) {
                Object rid = row.get("id");
                if (rid instanceof Number && ((Number) rid).intValue() == targetId) {
                    row.put("remarks", newRemarks);
                    return 1;
                }
            }
            return 0;
        }
        return 0;
    }

    /** Execute a non-prepared DML (DELETE). */
    private int execDml(String sql) {
        String upper = sql.trim().toUpperCase();
        if (upper.startsWith("DELETE")) {
            int sz = table.size();
            table.clear();
            nextId = 1;
            return sz;
        }
        return 0;
    }

    /** Return a (possibly filtered) snapshot of table rows. */
    private List<Map<String, Object>> filterRows(String sql) {
        String upper = sql.trim().toUpperCase();

        if (upper.contains("WHERE") && upper.contains("EFFICIENCY_SCORE > 70")) {
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> row : table) {
                Object score = row.get("efficiency_score");
                if (score instanceof Number
                        && ((Number) score).doubleValue() > 70.0) {
                    filtered.add(new LinkedHashMap<>(row));
                }
            }
            return filtered;
        }
        /* Default: return all rows */
        List<Map<String, Object>> snapshot = new ArrayList<>();
        for (Map<String, Object> row : table) {
            snapshot.add(new LinkedHashMap<>(row));
        }
        return snapshot;
    }

    /** Resolve a column reference (by name or 1-based index). */
    private String colName(Object arg) {
        if (arg instanceof String) {
            return ((String) arg).toLowerCase();
        }
        if (arg instanceof Integer) {
            int idx = (Integer) arg;
            if (idx >= 1 && idx <= COLUMNS.length) return COLUMNS[idx - 1];
        }
        return "unknown";
    }

    /** Null-safe toString. */
    private String asString(Object v) {
        return (v == null) ? null : String.valueOf(v);
    }
}
