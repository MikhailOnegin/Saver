package digital.fact.saver.data.legacyDatabase;

import android.provider.BaseColumns;

public class LegacyDbContract {

    public static abstract class TSources implements BaseColumns {
        public static final String TABLE_NAME = "SOURCES";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_START_SUM = "start_sum";
        public static final String COLUMN_ADDING_DATE = "adding_date";
        public static final String COLUMN_AIM_SUM = "aim_sum";
        public static final String COLUMN_ORDER = "order_number";
        public static final String COLUMN_VISIBILITY = "visibility";
    }

    public static abstract class TOperations implements BaseColumns {
        public static final String TABLE_NAME = "OPERATIONS";

        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ADDING_DATE = "adding_date";
        public static final String COLUMN_SUM = "sum";
        public static final String COLUMN_FROM_SOURCE = "from_source";
        public static final String COLUMN_TO_SOURCE = "to_source";
        public static final String COLUMN_PLAN_ID = "plan_id";
        public static final String COLUMN_CLASS = "class";
        public static final String COLUMN_COMMENT = "comment";
    }

    public static abstract class TPlans implements BaseColumns {
        public static final String TABLE_NAME = "PLANS";

        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SUM = "sum";
        public static final String COLUMN_OPERATION_ID = "operation_id";
        public static final String COLUMN_PLANNING_DATE = "planning_date";
    }

    public static abstract class TClasses implements BaseColumns {
        public static final String TABLE_NAME = "CLASSES";

        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_NAME = "name";
    }

}