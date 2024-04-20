//package lt.gama.auth.jpa;
//
//import lt.gama.auth.i.IAuth;
//import lt.gama.helpers.DateUtils;
//import lt.gama.model.sql.base.BaseEntitySql;
//import org.hibernate.Session;
//import org.hibernate.tuple.ValueGenerator;
//
//import java.time.LocalDateTime;
//
//public class UpdateTimestampGenerator implements ValueGenerator<LocalDateTime> {
//    @Autowired
//    static Auth auth;
//
//    @Override
//    public LocalDateTime generateValue(Session session, Object owner) {
//        if (owner instanceof BaseEntitySql && auth.isMigrating()) {
//            return ((BaseEntitySql) owner).getUpdatedOn();
//        } else {
//            return DateUtils.now();
//        }
//    }
//}
