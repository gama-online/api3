package lt.gama.helpers;

import lt.gama.api.request.PageRequestCondition;
import lt.gama.model.type.enums.CustomSearchType;

import java.util.Collection;

/**
 * gama-online
 * Created by valdas on 2018-03-06.
 */
public class PageRequestUtils {

    private PageRequestUtils() {}

    public static Object getFieldValue(Collection<PageRequestCondition> collection, CustomSearchType searchType) {
        return searchType == null ? null : getFieldValue(collection, searchType.getField());
    }

    public static Object getFieldValue(Collection<PageRequestCondition> collection, String field) {
        if (collection != null && field != null) {
            for (PageRequestCondition condition : collection) {
                if (field.equals(condition.getField())) {
                    return condition.getValue();
                }
            }
        }
        return null;
    }

//    public static PageRequestCondition removeField(Collection<PageRequestCondition> collection, CustomSearchType searchType) {
//        PageRequestCondition condRemoved = null;
//        if (collection != null && searchType != null) {
//            Iterator<PageRequestCondition> it = collection.iterator();
//            while (it.hasNext()) {
//                PageRequestCondition condition = it.next();
//                if (searchType.getField().equals(condition.getField())) {
//                    condRemoved = condition;
//                    it.remove();
//                }
//            }
//        }
//        return condRemoved;
//    }
}
