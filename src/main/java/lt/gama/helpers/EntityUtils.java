package lt.gama.helpers;

import jakarta.persistence.criteria.*;
import lt.gama.model.i.IId;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EntityUtils {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static <K> boolean isIdNull(IId<K> e) {
        return e == null || e.getId() == null;
    }

    public static <K> boolean isIdEquals(IId<K> e1, IId<K> e2) {
        return e1 == e2 ||
                ((e1 == null || e1.getId() == null) && (e2 == null || e2.getId() == null)) ||
                (e1 != null && e1.getId() != null && e2 != null && e2.getId() != null && e1.getId().equals(e2.getId()));
    }

    public static <K> K getId(IId<K> e) {
        return e == null ? null : e.getId();
    }

    /**
     * Prepare name value for search index
     * 1) Leave only letters and digits
     * 2) Compress multiple spaces
     * 3) Convert to lower case
     * @param name value
     * @return prepared value
     */
    public static String prepareName(String name) {
        if (StringHelper.isEmpty(name)) return null;
        String nonunicode = StringUtils.stripAccents(name);
        String value = nonunicode.replaceAll("[^\\p{Alnum}]+", " ");
        return StringHelper.isEmpty(value) ? null : value.trim().toLowerCase();
    }

    /**
     * Encoding date into 3 characters (3 x 7 = 21Bits):
     * <ul>
     * <li>year - 12bits (1..4095)</li>
     * <li>month - 4bits (1..12)</li>
     * <li>day - 5bits (1..31)</li>
     * </ul>
     * result:
     * <pre>
     * 01234567 01234567 01234567
     * 0yyyyyyy 0yyyyymm 0mmddddd
     * </pre>
     */
    public static String encodeLocalDate(LocalDate date) {
        if (date == null) return null;
        int year = date.getYear();
        int month = date.getMonthValue(); // 1..12
        int day = date.getDayOfMonth(); // 1..31
        char[] c = new char[3];
        c[0] = (char) (year >> 5 & 0x007f);
        c[1] = (char) ((month >> 2 | year << 2) & 0x007f);
        c[2] = (char) ((month << 5 | day) & 0x007f);
        return String.valueOf(c);
    }

    public static LocalDate decodeLocalDate(String encoded) {
        if (encoded == null || encoded.length() != 3) return null;
        int c0 = encoded.charAt(0);
        int c1 = encoded.charAt(1);
        int c2 = encoded.charAt(2);
        int year = c0 << 5 | c1 >> 2;
        int month = (c1 & 0x0003) << 2 | c2 >> 5;
        int day = c2 & 0x001f;
        return LocalDate.of(year, month, day);
    }

    /**
     * Normalize entity class name, i.e. return simple class name without Sql or Dto suffix
     * @return class name
     */
    public static String normalizeEntityClassName(Class<?> entity) {
        return normalizeEntityName(entity.getSimpleName());
    }

    public static String[] splitPattern(String pattern) {
        if (pattern.charAt(0) == '\"') {
            if (pattern.length() > 1) {
                return new String[]{StringUtils.stripAccents(pattern.substring(1).trim()).toLowerCase()};
            } else {
                return new String[0];
            }
        } else {
            return CollectionsHelper.streamOf(StringHelper.splitTokens(pattern))
                    .map(s -> StringUtils.stripAccents(s.trim()).toLowerCase()).toArray(String[]::new);
        }
    }

//TODO remove comments
//    public static Predicate whereDoc(PageRequest request, CriteriaBuilder cb, Root<?> root, Function<String[], Predicate> filterPredicate) {
//        if (!StringHelper.hasValue(request.getFilter())) return null;
//        String[] patterns = splitPattern(request.getFilter());
//
//        Predicate[] predicates = new Predicate[patterns.length];
//        for (int i = 0; i < patterns.length; i++) {
//            String pattern = patterns[i];
//            predicates[i] = cb.or(
//                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.NUMBER))), '%' + pattern + '%'),
//                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.NOTE))), '%' + pattern + '%'),
//                    cb.like(cb.lower(cb.function("unaccent", String.class,
//                            root.get(BaseDocumentSql_.COUNTERPARTY).get(CounterpartySql_.NAME))), '%' + pattern + '%'),
//                    cb.like(cb.lower(cb.function("unaccent", String.class,
//                            root.get(BaseDocumentSql_.EMPLOYEE).get(EmployeeSql_.NAME))), '%' + pattern + '%'));
//        }
//        Predicate result = patterns.length == 1 ? predicates[0] : cb.and(predicates);
//
//        Predicate specDocPredicate = filterPredicate != null ? filterPredicate.apply(patterns) : null;
//        if (specDocPredicate != null) result = cb.or(result, specDocPredicate);
//        return result;
//    }
//TODO remove comments
//    public static List<Selection<?>> selectIdDoc(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return expresionsList(orderBy, cb, root, true);
//    }
//TODO remove comments
//    public static List<Selection<?>> selectIdMoneyDoc(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return expresionsMoneyDocList(orderBy, cb, root, true);
//    }

    public static String normalizeEntityName(String name) {
        if (StringHelper.isEmpty(name)) return null;
        if (name.endsWith("Sql") || name.endsWith("Dto")) {
            name = name.substring(0, name.length() - 3);
        }
        // special cases for compatibility
        switch (name) {
            case "EmployeeOpeningBalance" -> name = "AdvanceOpeningBalance";
            case "EmployeeOperation" -> name = "AdvanceOperation";
            case "EmployeeRateInfluence" -> name = "AdvanceRateInfluence";
            case "CashOperation" -> name = "CashOrder";
        }
        return StringHelper.trim2null(name);
    }
//TODO remove comments
//    public static List<Selection<?>> expresionsList(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return expresionsList(orderBy, cb, root, false);
//    }
//TODO remove comments
//    public static List<Selection<?>> expresionsList(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
//        if ("number".equalsIgnoreCase(orderBy) || "-number".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    root.get(BaseDocumentSql_.DATE),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("employee".equalsIgnoreCase(orderBy) || "-employee".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.EMPLOYEE).get(EmployeeSql_.NAME)))),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("counterparty".equalsIgnoreCase(orderBy) || "-counterparty".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.COUNTERPARTY).get(CounterpartySql_.NAME)))),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        return List.of(
//                root.get(BaseDocumentSql_.DATE),
//                root.get(BaseDocumentSql_.SERIES),
//                root.get(BaseDocumentSql_.ORDINAL),
//                root.get(BaseDocumentSql_.NUMBER),
//                id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//    }
//TODO remove comments
//    public static List<Selection<?>> expresionsMoneyDocList(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return expresionsMoneyDocList(orderBy, cb, root, false);
//    }
//TODO remove comments
//    public static List<Selection<?>> expresionsMoneyDocList(String orderBy, CriteriaBuilder cb, Root<?> root, boolean id) {
//        if ("number".equalsIgnoreCase(orderBy) || "-number".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("employee".equalsIgnoreCase(orderBy) || "-employee".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.EMPLOYEE).get(EmployeeSql_.NAME)))),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("counterparty".equalsIgnoreCase(orderBy) || "-counterparty".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    cb.lower(cb.trim(cb.function("unaccent", String.class, root.get(BaseDocumentSql_.COUNTERPARTY).get(CounterpartySql_.NAME)))),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("payer-receiver".equalsIgnoreCase(orderBy) || "-payer-receiver".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    cb.lower(cb.trim(cb.function("unaccent", String.class,
//                            cb.coalesce(
//                                    root.get(BaseDocumentSql_.COUNTERPARTY).get(CounterpartySql_.NAME),
//                                    root.get(BaseDocumentSql_.EMPLOYEE).get(EmployeeSql_.NAME))))),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("amount".equalsIgnoreCase(orderBy) || "-amount".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        if ("baseAmount".equalsIgnoreCase(orderBy) || "-baseAmount".equalsIgnoreCase(orderBy)) {
//            return List.of(
//                    root.get(BaseMoneyDocumentSql_.BASE_AMOUNT).get("amount"),
//                    root.get(BaseDocumentSql_.DATE),
//                    root.get(BaseDocumentSql_.SERIES),
//                    root.get(BaseDocumentSql_.ORDINAL),
//                    root.get(BaseDocumentSql_.NUMBER),
//                    id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//        }
//        return List.of(
//                root.get(BaseDocumentSql_.DATE),
//                root.get(BaseDocumentSql_.SERIES),
//                root.get(BaseDocumentSql_.ORDINAL),
//                root.get(BaseDocumentSql_.NUMBER),
//                root.get(BaseMoneyDocumentSql_.AMOUNT).get("amount"),
//                id ? root.get(BaseDocumentSql_.ID).alias("id") : root.get(BaseDocumentSql_.ID));
//    }
//TODO remove comments
//    public static List<Order> orderDoc(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return EntityUtils.orderList(cb, orderBy, expresionsList(orderBy, cb, root).toArray(Expression[]::new));
//    }
//TODO remove comments
//    public static List<Order> orderMoneyDoc(String orderBy, CriteriaBuilder cb, Root<?> root) {
//        return EntityUtils.orderList(cb, orderBy, expresionsMoneyDocList(orderBy, cb, root).toArray(Expression[]::new));
//    }

    public static List<Order> orderList(CriteriaBuilder cb, String orderBy, Expression<?>... fields) {
        final boolean desc = StringHelper.hasValue(orderBy) && orderBy.charAt(0) == '-';
        return Stream.of(fields)
                .filter(Objects::nonNull)
                .map(x -> desc ? cb.desc(x) : cb.asc(x))
                .collect(Collectors.toList());
    }
}
