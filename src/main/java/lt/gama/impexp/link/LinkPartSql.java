package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.DBServiceSQL;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class LinkPartSql implements LinkBase<PartSql> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private Auth auth;


    @Override
    public PartSql resolve(PartSql document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        if (document.getType() == null) document.setType(PartType.PRODUCT);

        if (document.getType() == PartType.SERVICE) {

            if (!Validators.isPartialValid(document.getGlExpense()) || !Validators.isPartialValid(document.getGlIncome())) {

                if (!companySettings.isDisableGL() && companySettings.getGl() != null) {

                    if (!Validators.isValid(companySettings.getGl().getServiceIncome()) ||
                            !Validators.isValid(companySettings.getGl().getServiceExpense())) {
                        return null;    // do not import - no settings
                    }

                    if (!Validators.isPartialValid(document.getGlExpense())) {
                        document.setGlExpense(new GLDC(companySettings.getGl().getServiceExpense()));
                    }
                    if (!Validators.isPartialValid(document.getGlIncome())) {
                        document.setGlIncome(new GLDC(companySettings.getGl().getServiceIncome()));
                    }
                }
            }

        } else {

            if (!Validators.isPartialValid(document.getGlExpense()) || !Validators.isPartialValid(document.getGlIncome()) ||
                    !Validators.isValid(document.getAccountAsset())) {

                if (!companySettings.isDisableGL() && companySettings.getGl() != null) {

                    if (!Validators.isValid(companySettings.getGl().getProductIncome()) ||
                            !Validators.isValid(companySettings.getGl().getProductExpense()) ||
                            !Validators.isValid(companySettings.getGl().getProductAsset())) {
                        return null;    // do not import - no settings
                    }

                    if (!Validators.isPartialValid(document.getGlExpense())) {
                        document.setGlExpense(new GLDC(companySettings.getGl().getProductExpense()));
                    }
                    if (!Validators.isPartialValid(document.getGlIncome())) {
                        document.setGlIncome(new GLDC(companySettings.getGl().getProductIncome()));
                    }
                    if (!Validators.isValid(document.getAccountAsset())) {
                        document.setAccountAsset(companySettings.getGl().getProductAsset());
                    }
                }
            }
        }

        if (companySettings.isVatPayer()) {
            if (document.unknownTaxable()) {
                document.setTaxable(document.getVatRate() != null || StringHelper.hasValue(document.getVatRateCode()));
            }

            if (document.isTaxable() && StringHelper.isEmpty(document.getVatRateCode())) {
                VATRate vat = null;
                if (document.getVatRate() == null) {
                    vat = dbServiceSQL.getMaxVATRate(companySettings.getCountry(), null);
                } else {
                    VATRatesDate vatRatesDate = dbServiceSQL.getVATRateDate(companySettings.getCountry(), null);
                    if (vatRatesDate != null && CollectionsHelper.hasValue(vatRatesDate.getRates())) {
                        BigDecimal rate = NumberUtils.toScaledBigDecimal(document.getVatRate(), 2, RoundingMode.HALF_UP);
                        vat = vatRatesDate.getRates().stream()
                                .filter(e -> Objects.equals(rate, NumberUtils.toScaledBigDecimal(e.getRate(), 2, RoundingMode.HALF_UP)))
                                .findFirst()
                                .orElse(null);
                    }
                }
                if (vat != null) {
                    document.setVatRateCode(vat.getCode());
                }
            }
        }

        return document;
    }

    @Override
    public void finish(long document) {
    }
}
