package lt.gama.tasks;

import lt.gama.model.i.base.IBaseCompany;
import lt.gama.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class DeleteLabelTask<E extends IBaseCompany> extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected LabelService labelService;


    private final String label;
    private final Class<E> entityClass;


    public DeleteLabelTask(long companyId, String label, Class<E> entityClass) {
        super(companyId);
        this.label = label;
        this.entityClass = entityClass;
    }

    @Override
    public void execute() {
        try {
            labelService.deleteLabelAction(label, entityClass);
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}
