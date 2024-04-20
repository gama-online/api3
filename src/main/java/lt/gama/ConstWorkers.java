package lt.gama;

/**
 * Gama
 * Created by valdas on 15-03-21.
 */
public final class ConstWorkers {

    private ConstWorkers() {}  // Prevents instantiation

    // Queues
    public static final String DEFAULT_QUEUE_NAME = "default"; // Queue.DEFAULT_QUEUE
    public static final String IMPORT_QUEUE = "import-queue";

    // Bucket Folder
    public static final String TASKS_FOLDER = "tasks";
    public static final String IMPORT_FOLDER = "import";
    public static final String DOCS_PRINT_FOLDER = "docs_print";
    public static final String MAINTENANCE_FOLDER = "maintenance";

    public static final int GCS_BUFFER_SIZE = 16 * 1024; //2 * 1024 * 1024;

    // CRON JOBS
    public static final class Cron {
        // Clean old imports from storage
        public static final String CLEAN_STORAGE_PATH = "/cron/clean-storage";

        // Expand inventory balance records into the future
        public static final String INVENTORY_BALANCE_PATH = "/cron/inventory-balance";

        // Save subscriptions count on the date
        public static final String SUBSCRIPTIONS_COUNT_PATH = "/cron/subscriptions-count";

        // Save subscriptions count on the date
        public static final String SUBSCRIPTIONS_INVOICING_PATH = "/cron/subscriptions-invoicing";

        // e-shops and other systems sync with gama-online
        public static final String SYNC_PATH = "/cron/sync";

        public static final String TAX_FREE_SYNC_PATH = "/cron/tax-free";
    }

    // Tasks
    public static final String TASKS_QUEUE_PATH = "/tasks/queue";


    // WORKERS...

    public static final String TOKEN = "token";

    // Export bank operations in ISO20022
    public static final class ExportBankISO {
        public static final String WORKER_PATH = "/workers/bank-exportISO20022";
        public static final String NOW = "now";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String FILTER = "filter";
        public static final String STATUS = "status";
        public static final String ACCOUNT = "account";
        public static final String CONSOLIDATED_PAYMENTS = "cp";
    }

    public static final class IVAZ {
        public static final String WORKER_PATH = "/workers/export-ivaz";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String FILTER = "filter";
        public static final String STATUS = "status";
        public static final String LABEL = "label";
        public static final String ID = "id";
    }

    public static final class ISAF {
        public static final String WORKER_PATH = "/workers/export-isaf";
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String SALE = "sale";
        public static final String PURCHASE = "purchase";
        public static final String ID = "id";
        public static final String IS_PERIOD = "isPeriod";
        public static final String YEAR_TO = "yearTo";
        public static final String MONTH_TO = "monthTo";
    }

    public static final class SAFT {
        public static final String GL_WORKER_PATH = "/workers/saft-gl";
        public static final String PARTS_WORKER_PATH = "/workers/saft-parts";
        public static final String VENDORS_WORKER_PATH = "/workers/saft-vendors";
        public static final String CUSTOMERS_WORKER_PATH = "/workers/saft-customers";
        public static final String ASSETS_WORKER_PATH = "/workers/saft-assets";
        public static final String PAYMENTS_WORKER_PATH = "/workers/saft-payments";
        public static final String TAX_ACCOUNTING_BASIS = "ab";
        public static final String CONTACT_FIRST_NAME = "fn";
        public static final String CONTACT_LAST_NAME = "ln";
        public static final String CONTACT_TELEPHONE = "ph";
        public static final String SELECTION_DATE_FROM = "sf";
        public static final String SELECTION_DATE_TO = "st";
        public static final String DATE_FROM = "df";
        public static final String DATE_TO = "dt";
        public static final String PART_NO = "p";
        public static final String PARTS_TOTAL = "t";
    }

    public static final class GPAIS {
        public static final String PRODUCTS_PATH = "/workers/gpais-products";
        public static final String REGISTRY_PATH = "/workers/gpais-registry";
        public static final String DATE_FROM = "df";
        public static final String DATE_TO = "dt";
    }

    // Show document print preview in normal or mail format
    public static final class PrintDoc {
        public static final String PATH = "/print/doc";
        public static final String UUID = "0";
        public static final String MAIL = "mail";
        public static final String LANGUAGE = "l";
        public static final String SUBTYPE = "t";
        public static final String COUNTRY = "c";
        public static final String REGENERATE = "r";
        public static final String DB = "db";
    }

    // Show advance statement
    public static final class AdvanceStmt {
        public static final String WORKER_PATH = "/workers/advance-stmt";
        public static final String ID = "i";
        public static final String DATE_FROM = "f";
        public static final String DATE_TO = "t";
        public static final String CURRENCY = "c";
    }

    // Show reconciliation statement
    public static final class ReconcileStmt {
        public static final String WORKER_PATH = "/workers/reconcile-stmt";
        public static final String ID = "i";
        public static final String DATE_FROM = "f";
        public static final String DATE_TO = "t";
        public static final String DEBT_TYPE = "d";
        public static final String CURRENCY = "c";
        public static final String LANGUAGE = "l";
    }

    // Show cash-book
    public static final class CashBook {
        public static final String WORKER_PATH = "/workers/cash-book";
        public static final String ID = "i";
        public static final String DATE_FROM = "f";
        public static final String DATE_TO = "t";
        public static final String CURRENCY = "c";
    }

    // Return html template
    public static final class HtmlTemplate {
        public static final String WORKER_PATH = "/workers/html-template";
        public static final String NAME = "n";
    }
}
