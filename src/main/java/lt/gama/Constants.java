package lt.gama;

import java.time.LocalDate;
import java.util.List;

/**
 * Contains the client IDs and scopes for allowed clients consuming the gama API.
 */
public final class Constants {

    // project info
    public static final String PROJECT_ID = "gama-online";
    public static final String QUEUE_LOCATION = "us-central1";

    // refresh cookie name
    public static final String TOKEN_COOKIE_NAME = "M1";

    // Used as max date value
    public static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    public static final int CRON_CHECK_MONTHS_IN_ADVANCE = 3;
    public static final int GENERATE_MONTHS_IN_ADVANCE = 12;

    public static final String DEFAULT_SENDER_EMAIL = "mail@gama-online.lt";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@gama-online.lt";

    public static final int DB_BUFFER_SIZE = 1000;
    public static final int DB_BATCH_SIZE = 50; // hibernate.jdbc.batch_size

    // log labels names
    public static final String LOG_TRACE_ID = "log_trace_id";
    public static final String LOG_VERSION = "gama_version";

    public static final String LOG_LABEL_COMPANY = "gama_company_id";
    public static final String LOG_LABEL_TASK_ID = "gama_task_id";
    public static final String LOG_LABEL_LOGIN = "gama_login";
    public static final String LOG_LABEL_USER_NAME = "gama_user_name";
    public static final String LOG_LABEL_PERMISSIONS = "gama_permissions";
    public static final String LOG_LABEL_URL = "gama_url";
    public static final String LOG_LABEL_FRONT = "gama_front";
    public static final String LOG_FRONT_VERSION = "gama_front_version";
    public static final String LOG_FRONT_URL = "gama_front_url";

    public static final List<String> LOG_LABELS = List.of(
            LOG_LABEL_COMPANY,
            LOG_LABEL_TASK_ID,
            LOG_LABEL_LOGIN,
            LOG_LABEL_USER_NAME,
            LOG_LABEL_PERMISSIONS,
            LOG_LABEL_URL,
            LOG_LABEL_FRONT,
            LOG_FRONT_VERSION,
            LOG_FRONT_URL);
}
