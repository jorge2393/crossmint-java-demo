import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration utility for environment variables and application settings.
 */
public class Config {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String CROSSMINT_API_KEY = getenv("CROSSMINT_API_KEY", "your_crossmint_api_key_here");
    public static final String CROSSMINT_BASE_URL = getenv("CROSSMINT_BASE_URL", "https://staging.crossmint.com");
    public static final String NETWORK = getenv("NETWORK", "base-sepolia");
    public static final String LOG_LEVEL = getenv("LOG_LEVEL", "info");

    public static final String DEMO_RECIPIENT_ADDRESS = getenv("DEMO_RECIPIENT_ADDRESS", "0x6671f7552df0fbAF762Bd40aEd1cA3ec670d6161");
    public static final String DEMO_AMOUNT_USDXM = getenv("DEMO_AMOUNT_USDXM", "1");
    public static final String FUND_AMOUNT = getenv("FUND_AMOUNT", "10");

    private static String getenv(String key, String fallback) {
        String val = System.getenv(key);
        if (val == null || val.isEmpty()) {
            val = dotenv.get(key);
        }
        return val == null || val.isEmpty() ? fallback : val;
    }
}