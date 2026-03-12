import java.util.*;

/**
 * UseCase3DatabaseQueryCacheSystem
 * Simulates caching of database query results to reduce database load.
 */

class QueryResult {
    String query;
    String result;

    public QueryResult(String query, String result) {
        this.query = query;
        this.result = result;
    }
}

public class MultiLevelCache {

    // L1 Cache (in-memory fast cache)
    private LinkedHashMap<String, QueryResult> L1Cache;

    // L2 Cache (secondary cache)
    private LinkedHashMap<String, QueryResult> L2Cache;

    // L3 Database
    private HashMap<String, QueryResult> database = new HashMap<>();

    int l1Hits = 0;
    int l2Hits = 0;
    int dbHits = 0;

    public MultiLevelCache() {

        // L1 cache with LRU
        L1Cache = new LinkedHashMap<String, QueryResult>(1000, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, QueryResult> eldest) {
                return size() > 1000;
            }
        };

        // L2 cache with LRU
        L2Cache = new LinkedHashMap<String, QueryResult>(10000, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, QueryResult> eldest) {
                return size() > 10000;
            }
        };
    }

    // get query result
    public QueryResult getQueryResult(String query) {

        // L1 Cache
        if (L1Cache.containsKey(query)) {
            l1Hits++;
            System.out.println("L1 Cache HIT");
            return L1Cache.get(query);
        }

        // L2 Cache
        if (L2Cache.containsKey(query)) {
            l2Hits++;
            System.out.println("L1 MISS → L2 Cache HIT");

            QueryResult result = L2Cache.get(query);

            // promote to L1
            L1Cache.put(query, result);

            return result;
        }

        // Database
        if (database.containsKey(query)) {
            dbHits++;
            System.out.println("L1 MISS → L2 MISS → Database HIT");

            QueryResult result = database.get(query);

            // store in L2
            L2Cache.put(query, result);

            return result;
        }

        System.out.println("Query not found in database.");
        return null;
    }

    // add query result to database
    public void addQuery(String query, String result) {
        database.put(query, new QueryResult(query, result));
    }

    // cache statistics
    public void getStatistics() {

        int total = l1Hits + l2Hits + dbHits;

        System.out.println("\nCache Statistics:");

        if (total > 0) {
            System.out.println("L1 Hit Rate: " + (l1Hits * 100 / total) + "%");
            System.out.println("L2 Hit Rate: " + (l2Hits * 100 / total) + "%");
            System.out.println("Database Hit Rate: " + (dbHits * 100 / total) + "%");
        }
    }

    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        // simulate database entries
        cache.addQuery("SELECT * FROM users", "User Data");
        cache.addQuery("SELECT * FROM orders", "Order Data");
        cache.addQuery("SELECT * FROM products", "Product Data");

        cache.getQueryResult("SELECT * FROM users");   // DB → L2
        cache.getQueryResult("SELECT * FROM users");   // L1 hit
        cache.getQueryResult("SELECT * FROM orders");  // DB → L2

        cache.getStatistics();
    }
}
