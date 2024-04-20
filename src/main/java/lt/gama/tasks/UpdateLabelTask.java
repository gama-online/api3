package lt.gama.tasks;

import lt.gama.model.i.base.IBaseCompany;
import lt.gama.service.LabelService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class UpdateLabelTask<E extends IBaseCompany> extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected LabelService labelService;


    private final String labelOld;
    private final String labelNew;
    private final Class<E> entityClass;


    public UpdateLabelTask(long companyId, String labelOld, String labelNew, Class<E> entityClass) {
        super(companyId);
        this.labelOld = labelOld;
        this.labelNew = labelNew;
        this.entityClass = entityClass;
    }

    @Override
    public void execute() {
        try {
            labelService.updateLabelAction(labelOld, labelNew, entityClass);
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return "labelOld='" + labelOld + '\'' +
                " labelNew='" + labelNew + '\'' +
                " entityClass=" + entityClass +
                ' ' + super.toString();
    }
}
