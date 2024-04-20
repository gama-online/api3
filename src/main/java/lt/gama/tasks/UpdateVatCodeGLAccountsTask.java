package lt.gama.tasks;

import jakarta.persistence.EntityManager;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.CompanySettingsGL;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.service.AuthSettingsCacheService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2019-02-21.
 */
public class UpdateVatCodeGLAccountsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected AuthSettingsCacheService authSettingsCacheService;


    private final  List<VATCodeTotal> vatCode;


    public UpdateVatCodeGLAccountsTask(long companyId, List<VATCodeTotal> vatCode) {
        super(companyId);
        this.vatCode = vatCode;
    }

    @Override
    public void execute() {
        if (vatCode == null || vatCode.size() == 0) return;

        try {
            Boolean u = dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
                CompanySql company = dbServiceSQL.getById(CompanySql.class, getCompanyId());
                if (company == null || company.getSettings() == null || company.getSettings().isDisableGL() ||
                        company.getSettings().getGl() == null) return false;

                boolean updated = false;
                for (VATCodeTotal vatCode : this.vatCode) {
                    if (StringHelper.isEmpty(vatCode.getCode())) break;
                    if (vatCode.getGl() == null || !Validators.isPartialValid(vatCode.getGl())) break;

                    // 15.24. Conditional-Or Operator ||
                    // Thus, || compares the same result as | on boolean or Boolean operands.
                    // It differs only in that the right-hand operand expression is evaluated conditionally rather than always.
                    updated |= setDC(company.getSettings().getGl(), vatCode.getCode(), vatCode.getGl());
                }

                if (updated) {
                    dbServiceSQL.saveEntity(company);
                }
                return updated;
            });
            if (u) authSettingsCacheService.remove(getCompanyId());

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }

    /**
     * Update VatCode settings with the latest values
     * @return true if modified and need to save
     */
    private boolean setDC(CompanySettingsGL settingsGL, String code, GLDC values) {
        if (settingsGL == null || StringHelper.isEmpty(code) || !Validators.isPartialValid(values)) return false;

        Map<String, GLDC> vatCode = settingsGL.getVatCode();
        if (vatCode == null) {
            vatCode = new HashMap<>();
            settingsGL.setVatCode(vatCode);
        }
        GLDC gldc = vatCode.get(code);
        if (gldc == null) {
            vatCode.put(code, values);
            return true;
        }

        boolean result = false;
        if (Validators.isValidDebit(values) && !Objects.equals(values.getDebit(), gldc.getDebit())) {
            gldc.setDebit(values.getDebit());
            result = true;
        }
        if (Validators.isValidCredit(values) && !Objects.equals(values.getCredit(), gldc.getCredit())) {
            gldc.setCredit(values.getCredit());
            result = true;
        }
        return result;
    }

}
