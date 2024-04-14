package lt.gama.jpa.generators;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;

public class EditorValueGeneration implements BeforeExecutionGenerator {
//TODO
//    @Inject
//    static Provider<IAuth> authProvider;


    private final EnumSet<EventType> eventTypes = EnumSet.of(EventType.UPDATE, EventType.INSERT);


    @Override
    public EnumSet<EventType> getEventTypes() {
        return eventTypes;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
//TODO        if (authProvider.get().isMigrating() && owner instanceof BaseEntitySql entity) {
//            return entity.getUpdatedBy();
//        } else {
//            return authProvider.get().getName();
//        }
        return "editor";
    }
}
