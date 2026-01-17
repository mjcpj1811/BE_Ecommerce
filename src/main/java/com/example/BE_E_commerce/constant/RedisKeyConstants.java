package com.example.BE_E_commerce.constant;

public class RedisKeyConstants {

    // ========== USER ==========
    public static final String USER_SESSION = "session:user:";
    public static final String USER_PROFILE = "user:profile:";

    // ========== TOKEN BLACKLIST ==========
    public static final String TOKEN_BLACKLIST = "blacklist:token:";

    // ========== PRODUCT ==========
    public static final String PRODUCT_DETAIL = "product:detail:";
    public static final String PRODUCT_LIST = "product:list:";
    public static final String PRODUCT_SEARCH = "product:search: ";
    public static final String HOT_PRODUCTS = "hot: products";
    public static final String NEW_PRODUCTS = "new:products";

    // ========== CATEGORY ==========
    public static final String CATEGORY_TREE = "category:tree";
    public static final String CATEGORY_DETAIL = "category:detail:";

    // ========== SHOP ==========
    public static final String SHOP_DETAIL = "shop:detail:";
    public static final String SHOP_PRODUCTS = "shop:products: ";
    public static final String SHOP_STATS = "shop:stats:";

    // ========== CART ==========
    public static final String CART = "cart:";

    // ========== STOCK LOCK ==========
    public static final String STOCK_LOCK = "lock:stock:";

    // ========== RATE LIMIT ==========
    public static final String RATE_LIMIT = "ratelimit:";

    // ========== NOTIFICATION ==========
    public static final String NOTIFICATION_UNREAD = "notifications:unread:";

    // ========== ONLINE USERS (Chat) ==========
    public static final String ONLINE_USERS = "online:users";

    // ========== CACHE PREFIXES ==========
    public static final String CACHE_PRODUCT = "cache:product:";
    public static final String CACHE_CATEGORY = "cache:category:";
    public static final String CACHE_SHOP = "cache:shop:";

    // ========== TTL (seconds) ==========
    public static final long TTL_USER_SESSION = 86400; // 24 hours
    public static final long TTL_PRODUCT = 3600; // 1 hour
    public static final long TTL_CATEGORY = 7200; // 2 hours
    public static final long TTL_SHOP = 1800; // 30 minutes
    public static final long TTL_CART = 604800; // 7 days
    public static final long TTL_STOCK_LOCK = 10; // 10 seconds
    public static final long TTL_RATE_LIMIT = 60; // 1 minute

    private RedisKeyConstants() {
        // Prevent instantiation
    }

    // ========== HELPER METHODS ==========

    public static String userSessionKey(Long userId) {
        return USER_SESSION + userId;
    }

    public static String userProfileKey(Long userId) {
        return USER_PROFILE + userId;
    }

    public static String productDetailKey(Long productId) {
        return PRODUCT_DETAIL + productId;
    }

    public static String productListKey(Long categoryId, int page) {
        return PRODUCT_LIST + categoryId + ":page:" + page;
    }
    public static String tokenBlacklistKey(String token) {
        return TOKEN_BLACKLIST + token;
    }
    public static String shopDetailKey(Long shopId) {
        return SHOP_DETAIL + shopId;
    }

    public static String cartKey(Long userId) {
        return CART + userId;
    }

    public static String stockLockKey(Long variantId) {
        return STOCK_LOCK + variantId;
    }

    public static String rateLimitKey(String ip, String endpoint) {
        return RATE_LIMIT + ip + ":" + endpoint;
    }

    public static String notificationUnreadKey(Long userId) {
        return NOTIFICATION_UNREAD + userId;
    }
}