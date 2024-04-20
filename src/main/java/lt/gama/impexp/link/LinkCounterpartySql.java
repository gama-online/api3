package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DebtType;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkCounterpartySql implements LinkBase<CounterpartySql> {

    @Autowired
    private Auth auth;


    @Override
    public CounterpartySql resolve(CounterpartySql document) {
        if (!Validators.isValid(document.getAccount(DebtType.VENDOR)) || !Validators.isValid(document.getAccount(DebtType.CUSTOMER))) {

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            if (!companySettings.isDisableGL() && companySettings.getGl() != null) {

                if (!Validators.isValid(companySettings.getGl().getCounterpartyVendor()) ||
                        !Validators.isValid(companySettings.getGl().getCounterpartyCustomer())) {
                    return null;    // do not import - no settings
                }

                if (!Validators.isValid(document.getAccount(DebtType.VENDOR))) {
                    document.setAccount(DebtType.VENDOR, companySettings.getGl().getCounterpartyVendor());
                }
                if (!Validators.isValid(document.getAccount(DebtType.CUSTOMER))) {
                    document.setAccount(DebtType.CUSTOMER, companySettings.getGl().getCounterpartyCustomer());
                }
            }
        }
        return document;
    }

    @Override
    public void finish(long documentId) {
    }

}
