//package lt.gama.auth.jpa;
//
//import lt.gama.auth.i.IAuth;
//import lt.gama.helpers.DateUtils;
//import lt.gama.model.sql.base.BaseEntitySql;
//import org.hibernate.Session;
//import org.hibernate.tuple.ValueGenerator;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.time.LocalDateTime;
//
//public class CreationTimestampGenerator implements ValueGenerator<LocalDateTime> {
//    @Autowired
//    static Auth auth;
//
//    @Override
//    public LocalDateTime generateValue(Session session, Object owner) {
//        if (owner instanceof BaseEntitySql && auth.isMigrating()) {
//            return ((BaseEntitySql) owner).getCreatedOn();
//        } else {
//            return DateUtils.now();
//        }
//    }
//}
