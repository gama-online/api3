package lt.gama.tasks;

import lt.gama.impexp.EntityType;
import lt.gama.model.type.enums.DataFormatType;

import static lt.gama.ConstWorkers.IMPORT_QUEUE;

//TODO finish
public class ImportTask extends BaseDeferredTask {

    private final String name;
    private final long recNo;
    private final EntityType entityType;
    private final boolean delete;
    private final DataFormatType format;


    public ImportTask(long companyId, String name, long recNo, EntityType entityType, boolean delete, DataFormatType format) {
        super(companyId, IMPORT_QUEUE);
        this.name = name;
        this.recNo = recNo;
        this.entityType = entityType;
        this.delete = delete;
        this.format = format;
    }

    @Override
    public void execute() {}


    @Override
    public String toString() {
        return "name='" + name + '\'' +
                " recNo=" + recNo +
                " entityType=" + entityType +
                " delete=" + delete +
                " format='" + format + '\'' +
                ' ' + super.toString();
    }
}
