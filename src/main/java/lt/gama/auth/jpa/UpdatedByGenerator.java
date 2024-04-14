//TODO package lt.gama.auth.jpa;
//
//import lt.gama.auth.i.IAuth;
//import lt.gama.model.sql.base.BaseEntitySql;
//import org.hibernate.Session;
//import org.hibernate.tuple.ValueGenerator;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//public class UpdatedByGenerator implements ValueGenerator<String> {
//    @Inject
//    static Provider<IAuth> authProvider;
//
//    @Override
//    public String generateValue(Session session, Object owner) {
//        if (owner instanceof BaseEntitySql && authProvider.get().isMigrating()) {
//            return ((BaseEntitySql) owner).getUpdatedBy();
//        } else {
//            return authProvider.get().getName();
//        }
//    }
//}
