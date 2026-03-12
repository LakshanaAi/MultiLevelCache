import java.util.*;

/**
 * UseCase1NetflixCacheSystem
 * Simulates a 3-level cache system (L1, L2, L3) for video streaming.
 */

class Video {
    String videoId;
    String data;

    public Video(String videoId, String data) {
        this.videoId = videoId;
        this.data = data;
    }
}

public class MultiLevelCache {

    // L1 Cache (fast memory cache)
    private LinkedHashMap<String, Video> L1;

    // L2 Cache (SSD cache simulation)
    private LinkedHashMap<String, Video> L2;

    // L3 Database (all videos)
    private HashMap<String, Video> database = new HashMap<>();

    int l1Hits = 0;
    int l2Hits = 0;
    int l3Hits = 0;

    public MultiLevelCache() {

        // L1 cache with LRU eviction
        L1 = new LinkedHashMap<String, Video>(10000, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Video> eldest) {
                return size() > 10000;
            }
        };

        // L2 cache with LRU eviction
        L2 = new LinkedHashMap<String, Video>(100000, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, Video> eldest) {
                return size() > 100000;
            }
        };
    }

    // get video from cache system
    public Video getVideo(String videoId) {

        // L1 Cache
        if (L1.containsKey(videoId)) {
            l1Hits++;
            System.out.println("L1 Cache HIT");
            return L1.get(videoId);
        }

        // L2 Cache
        if (L2.containsKey(videoId)) {
            l2Hits++;
            System.out.println("L1 MISS → L2 Cache HIT");

            Video video = L2.get(videoId);

            // promote to L1
            L1.put(videoId, video);

            return video;
        }

        // L3 Database
        if (database.containsKey(videoId)) {
            l3Hits++;
            System.out.println("L1 MISS → L2 MISS → L3 Database HIT");

            Video video = database.get(videoId);

            // add to L2
            L2.put(videoId, video);

            return video;
        }

        System.out.println("Video not found.");
        return null;
    }

    // add video to database
    public void addVideo(Video video) {
        database.put(video.videoId, video);
    }

    // statistics
    public void getStatistics() {

        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\nCache Statistics:");

        if (total > 0) {
            System.out.println("L1 Hit Rate: " + (l1Hits * 100 / total) + "%");
            System.out.println("L2 Hit Rate: " + (l2Hits * 100 / total) + "%");
            System.out.println("L3 Hit Rate: " + (l3Hits * 100 / total) + "%");
        }
    }

    public static void main(String[] args) {

        MultiLevelCache cache = new MultiLevelCache();

        // add videos to database
        cache.addVideo(new Video("video_123", "Movie A"));
        cache.addVideo(new Video("video_456", "Movie B"));
        cache.addVideo(new Video("video_999", "Movie C"));

        cache.getVideo("video_123"); // DB → L2
        cache.getVideo("video_123"); // L1 hit
        cache.getVideo("video_999"); // DB → L2

        cache.getStatistics();
    }
}
