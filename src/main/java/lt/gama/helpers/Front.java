package lt.gama.helpers;

import lt.gama.model.dto.entities.AccountDto;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.sync.SyncAbilities;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.model.type.sync.WarehouseAbilities;

import java.util.ArrayList;

/**
 * gama-online
 * Created by valdas on 2016-02-03.
 */
public final class Front {

    private Front() {}

    /**
     * Settings needed in frontend only
     */
    public static CompanySettings CompanySettings(CompanySettings src) {
        if (src == null) return null;

        CompanySettings settings = new CompanySettings();

        settings.setValidUntil(src.getValidUntil());
        settings.setStartAccounting(src.getStartAccounting());
        settings.setAccYear(src.getAccYear());
        settings.setAccMonth(src.getAccMonth());
        settings.setCurrency(src.getCurrency());
        settings.setDecimal(src.getDecimal());
        settings.setDecimalPrice(src.getDecimalPrice());
        settings.setDecimalCost(src.getDecimalCost());
        settings.setRegion(src.getRegion());
        settings.setLanguage(src.getLanguage());
        settings.setCountry(src.getCountry());
        settings.setDisableGL(src.isDisableGL());
        settings.setVatPayer(src.isVatPayer());
        settings.setEnableTaxFree(src.getEnableTaxFree());

        settings.setCfPartSN(src.getCfPartSN());
        settings.setCfPart(src.getCfPart());
        settings.setCfCounterparty(src.getCfCounterparty());
        settings.setCfEmployee(src.getCfEmployee());

        settings.setWarehouse(src.getWarehouse());
        settings.setAccount(src.getAccount());
        settings.setCash(src.getCash());

        settings.setSales(src.getSales());

        settings.setChargeAdvance(src.getChargeAdvance());

        if (src.getSync() != null) {
            settings.setSync(new SyncSettings());
            settings.getSync().setSyncActive(BooleanUtils.isTrue(src.getSync().getSyncActive()));
            settings.getSync().setSyncWarehouseActive(BooleanUtils.isTrue(src.getSync().getSyncWarehouseActive()));
            settings.getSync().setAbilities(BooleanUtils.isTrue(src.getSync().getSyncActive())
                    ? src.getSync().getAbilities()
                    : new SyncAbilities());
            settings.getSync().setWarehouseAbilities(BooleanUtils.isTrue(src.getSync().getSyncWarehouseActive())
                    ? src.getSync().getWarehouseAbilities()
                    : new WarehouseAbilities());
        } else {
            settings.setSync(new SyncSettings());
            settings.getSync().setSyncActive(false);
            settings.getSync().setSyncWarehouseActive(false);
            settings.getSync().setAbilities(new SyncAbilities());
            settings.getSync().setWarehouseAbilities(new WarehouseAbilities());
        }
        return settings;
    }

    /**
     * Clear all very sensitive data that do not need to transfer to the client
     */
    public static AccountDto Account(AccountSql src) {
        if (src == null) return null;
        AccountDto account = new AccountDto();
        account.setId(src.getId());
        if (src.getPayer() != null) {
            CompanyDto payer = new CompanyDto();
            payer.setId(src.getPayer().getId());
            payer.setName(src.getPayer().getName());
            account.setPayer(payer);
        }
        account.setLastLogin(src.getLastLogin());
        if (CollectionsHelper.hasValue(src.getCompanies())) {
            account.setCompanies(new ArrayList<>());
            for (AccountInfo accountInfo : src.getCompanies()) {
                account.getCompanies().add(Front.AccountInfo(accountInfo));
            }
        }
        return account;
    }

    public static AccountInfo AccountInfo(AccountInfo src) {
        if (src == null) return null;
        AccountInfo accountInfo = new AccountInfo();

        accountInfo.setCompanyId(src.getCompanyId());
        accountInfo.setCompanyName(src.getCompanyName());
        accountInfo.setCode(src.getCode());
        accountInfo.setVatCode(src.getVatCode());

        accountInfo.setEmployeeId(src.getEmployeeId());
        accountInfo.setEmployeeName(src.getEmployeeName());
        accountInfo.setEmployeeOffice(src.getEmployeeOffice());
        accountInfo.setApi(src.getApi());

        accountInfo.setLastLogin(src.getLastLogin());

        return accountInfo;
    }

}
