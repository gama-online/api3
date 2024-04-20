package lt.gama.service;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.base.BaseMoneyBalanceDto;
import lt.gama.model.dto.base.BaseMoneyRateInfluenceDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.*;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.i.IMoneyAccount;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.model.type.part.PartCostSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * Gama
 * Created by valdas on 15-03-31.
 */
@Service
public class GLGenService {

    private static final Logger log = LoggerFactory.getLogger(GLGenService.class);

    private final Auth auth;
    private final GLUtilsService glUtilsService;


    public GLGenService(Auth auth, GLUtilsService glUtilsService) {
        this.auth = auth;
        this.glUtilsService = glUtilsService;
    }


    public List<GLOperationDto> generateDoubleEntry(InvoiceDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountCustomer,
                                                    GLOperationAccount accountVATPay,
                                                    Map<Long, GLDC> accountsIncome,
                                                    Map<Long, GLOperationAccount> accountsAsset,
                                                    Map<Long, GLDC> accountsExpense,
                                                    Map<Long, GLOperationAccount> accountsVATRec,
                                                    Map<Long, GLOperationAccount> accountsVATPay,
                                                    GLOperationAccount accountTemp) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountCustomer), "No Customer Account");

        List<GLOperationDto> vats = new ArrayList<>();
        if (BooleanUtils.isNotTrue(document.getReverseVAT())) {
            boolean vatDone = false;
            if (CollectionsHelper.hasValue(document.getVatCodeTotals())) {
                for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                    if (GamaMoneyUtils.isZero(vatCodeTotal.getTax()) || vatCodeTotal.getGl() == null) continue;
                    if (GamaMoneyUtils.isPositive(vatCodeTotal.getTax()) && Validators.isValid(vatCodeTotal.getGl().getCredit())) {
                        vats.add(new GLOperationDto(accountCustomer, vatCodeTotal.getGl().getCredit(), vatCodeTotal.getTax()));
                        vatDone = true;
                    } else if (GamaMoneyUtils.isNegative(vatCodeTotal.getTax()) && Validators.isValid(vatCodeTotal.getGl().getDebit())) {
                        vats.add(new GLOperationDto(vatCodeTotal.getGl().getDebit(), accountCustomer, vatCodeTotal.getTax().negated()));
                        vatDone = true;
                    }
                }
            }

            if (!vatDone && GamaMoneyUtils.isNonZero(document.getBaseTaxTotal())) {
                Validators.checkArgument(Validators.isValid(accountVATPay), "No VAT Account");
                if (GamaMoneyUtils.isPositive(document.getBaseTaxTotal())) {
                    vats.add(new GLOperationDto(accountCustomer, accountVATPay, document.getBaseTaxTotal()));
                } else if (GamaMoneyUtils.isNegative(document.getBaseTaxTotal())) {
                    vats.add(new GLOperationDto(accountVATPay, accountCustomer, document.getBaseTaxTotal().negated()));
                }
            }
        }

        List<GLOperationDto> operations = new ArrayList<>();

        if (BooleanUtils.isTrue(document.getAdvance())) {
            makeOperations(vats, operations);

        } else {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            boolean isExpandInvoice = companySettings.getGl() != null &&
                    BooleanUtils.isTrue(companySettings.getGl().getExpandInvoice());

            final List<GLOperationDto> incomesExpanded = isExpandInvoice ? new ArrayList<>() : null;
            final List<GLOperationDto> expensesExpanded = isExpandInvoice ? new ArrayList<>() : null;

            final Map<DC, GamaMoney> incomes = isExpandInvoice ? null : new HashMap<>();
            final Map<DC, GamaMoney> expenses = isExpandInvoice ? null : new HashMap<>();

            CollectionsHelper.streamOf(document.getParts())
                    .flatMap(p -> Stream.concat(Stream.of(p), CollectionsHelper.streamOf(p.getParts())))
                    .forEach(part -> {

                        GLDC accIncome = accountsIncome.get(part.getId());
                        Validators.checkArgument(Validators.isPartialValid(accIncome), "Part {0} has no Income Account", part.toMessage());

                        if (GamaMoneyUtils.isPositive(part.getBaseTotal())) {
                            if (isExpandInvoice) {
                                incomesExpanded.add(new GLOperationDto(accountCustomer, accIncome, part.getBaseTotal()));
                            } else {
                                add(incomes, new DC(accountCustomer, accIncome), part.getBaseTotal());
                            }
                        } else if (GamaMoneyUtils.isNegative(part.getBaseTotal())) {
                            if (isExpandInvoice) {
                                incomesExpanded.add(new GLOperationDto(accIncome, accountCustomer, part.getBaseTotal().negated()));
                            } else {
                                add(incomes, new DC(accIncome, accountCustomer), part.getBaseTotal().negated());
                            }
                        }

                        GLOperationAccount accAsset = accountsAsset.get(part.getId());
                        GLDC accExpense = accountsExpense.get(part.getId());

                        if (Validators.isPartialValid(accExpense) && Validators.isValid(accAsset)) {
                            if (GamaMoneyUtils.isPositive(part.getCostTotal())) {
                                if (isExpandInvoice) {
                                    expensesExpanded.add(new GLOperationDto(accExpense, accAsset, part.getCostTotal()));
                                } else {
                                    add(expenses, new DC(accExpense, accAsset), part.getCostTotal());
                                }
                            } else if (GamaMoneyUtils.isNegative(part.getCostTotal())) {
                                if (isExpandInvoice) {
                                    expensesExpanded.add(new GLOperationDto(accAsset, accExpense, part.getCostTotal().negated()));
                                } else {
                                    add(expenses, new DC(accAsset, accExpense), part.getCostTotal().negated());
                                }
                            }
                        }
                    });

            makeOperations(vats, operations);
            makeOperations(incomes, operations);
            makeOperations(incomesExpanded, operations);
            makeOperations(expenses, operations);
            makeOperations(expensesExpanded, operations);
            if (BooleanUtils.isTrue(document.getEcr())) {
                operations.add(new GLOperationDto(accountTemp, accountCustomer, document.getBaseTotal()));
            }
        }
        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(PurchaseDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountVendor,
                                                    GLOperationAccount accountVATRec,
                                                    GLOperationAccount accountVATPay,
                                                    Map<Long, GLOperationAccount> accountsAsset,
                                                    Map<Long, GLDC> accountsExpense,
                                                    Map<Long, GLOperationAccount> accountsVATRec,
                                                    Map<Long, GLOperationAccount> accountsVATPay,
                                                    GLOperationAccount accountPurchaseExpense,
                                                    GLOperationAccount accountCurrRateNeg,
                                                    GLOperationAccount accountCurrRatePos,
                                                    GLOperationAccount accountTemp) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountVendor), "No Vendor Account");

        if (CollectionsHelper.hasValue(document.getExpenses())) {
            Validators.checkArgument(Validators.isValid(accountPurchaseExpense), "No Purchase Expenses Account");
        }

        boolean vatDone = false;
        List<GLOperationDto> vats = new ArrayList<>();
        if (CollectionsHelper.hasValue(document.getVatCodeTotals())) {
            for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                if (GamaMoneyUtils.isZero(vatCodeTotal.getTax()) || vatCodeTotal.getGl() == null) continue;
                if (GamaMoneyUtils.isPositive(vatCodeTotal.getTax()) && Validators.isValid(vatCodeTotal.getGl().getDebit())) {

                    GLOperationAccount credit = BooleanUtils.isNotTrue(document.getReverseVAT()) ? accountVendor :
                            Validators.isValid(vatCodeTotal.getGl().getCredit()) ? vatCodeTotal.getGl().getCredit() : accountVATPay;
                    vats.add(new GLOperationDto(vatCodeTotal.getGl().getDebit(), credit, vatCodeTotal.getTax()));
                    vatDone = true;

                } else if (GamaMoneyUtils.isNegative(vatCodeTotal.getTax()) && Validators.isValid(vatCodeTotal.getGl().getCredit())) {

                    GLOperationAccount debit = BooleanUtils.isNotTrue(document.getReverseVAT()) ? accountVendor :
                            Validators.isValid(vatCodeTotal.getGl().getDebit()) ? vatCodeTotal.getGl().getDebit() : accountVATPay;
                    vats.add(new GLOperationDto(debit, vatCodeTotal.getGl().getCredit(), vatCodeTotal.getTax().negated()));
                    vatDone = true;
                }
            }
        }

        if (!vatDone && GamaMoneyUtils.isNonZero(document.getBaseTaxTotal())) {

            Validators.checkArgument(Validators.isValid(accountVATRec), TranslationService.getInstance().translate(TranslationService.GL.NoVATRecAcc, auth.getLanguage()));
            Validators.checkArgument(Validators.isValid(accountVATPay), TranslationService.getInstance().translate(TranslationService.GL.NoVATPayAcc, auth.getLanguage()));

            if (GamaMoneyUtils.isPositive(document.getBaseTaxTotal())) {
                vats.add(new GLOperationDto(accountVATRec, BooleanUtils.isNotTrue(document.getReverseVAT()) ? accountVendor : accountVATPay, document.getBaseTaxTotal()));
            } else if (GamaMoneyUtils.isNegative(document.getBaseTaxTotal())) {
                vats.add(new GLOperationDto(BooleanUtils.isNotTrue(document.getReverseVAT()) ? accountVendor : accountVATPay, accountVATRec, document.getBaseTaxTotal().negated()));
            }
        }

        List<GLOperationDto> operations = new ArrayList<>();

        if (BooleanUtils.isTrue(document.getAdvance())) {
            makeOperations(vats, operations);

        } else {
            Map<DC, GamaMoney> assets = new HashMap<>();
            Map<DC, GamaMoney> expenses = new HashMap<>();
            Map<DC, GamaMoney> expensesExt = new HashMap<>();
            Map<DC, GamaMoney> expensesFS = null;

            List<GLOperationDto> expensesExpanded = null;

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            boolean isExpandInvoice = companySettings.getGl() != null &&
                    BooleanUtils.isTrue(companySettings.getGl().getExpandInvoice());

            if (isExpandInvoice) {
                expensesExpanded = new ArrayList<>();
            } else {
                expensesFS = new HashMap<>();
            }

            GamaMoney costTotal = null;
            for (PartPurchaseDto part : document.getParts()) {

                GLOperationAccount accAsset = accountsAsset.get(part.getId());
                GLDC accExpense = accountsExpense.get(part.getId());

                if (part.getType() == PartType.SERVICE) {
                    Validators.checkArgument(Validators.isPartialValid(accExpense), "Part {0} has no Expense Accounts", part.toMessage());
                    if (!part.isInCost()) {
                        if (GamaMoneyUtils.isNonZero(part.getBaseTotal())) {
                            addAuto(expenses, new DC(accExpense, accountVendor), GamaMoneyUtils.subtract(part.getCostTotal(), part.getExpense()));
                            costTotal = GamaMoneyUtils.add(costTotal, GamaMoneyUtils.subtract(part.getCostTotal(), part.getExpense()));
                        }
                        if (part.isAddExp()) {
                            if (CollectionsHelper.hasValue(document.getExpenses())) {
                                if (GamaMoneyUtils.isNonZero(part.getExpense())) {
                                    addAuto(expensesExt, new DC(accExpense, accountPurchaseExpense), part.getExpense());
                                }
                            }
                        }
                    }

                } else {
                    Validators.checkArgument(Validators.isValid(accAsset), "Part {0} has no Asset Accounts", part.toMessage());

                    if (GamaMoneyUtils.isNonZero(part.getCostTotal())) {
                        addAuto(assets, new DC(accAsset, accountVendor), GamaMoneyUtils.subtract(part.getCostTotal(), part.getExpense()));
                        costTotal = GamaMoneyUtils.add(costTotal, GamaMoneyUtils.subtract(part.getCostTotal(), part.getExpense()));
                    }

                    if (CollectionsHelper.hasValue(document.getExpenses())) {
                        if (GamaMoneyUtils.isNonZero(part.getExpense())) {
                            addAuto(expensesExt, new DC(accAsset, accountPurchaseExpense), part.getExpense());
                        }
                    }

                    if (part.isForwardSell() && CollectionsHelper.hasValue(part.getCostInfo())) {
                        GamaMoney cost = null;
                        for (PartCostSource costSource : part.getCostInfo()) {
                            if (costSource.isForwardSell()) {
                                cost = GamaMoneyUtils.add(cost, GamaMoneyUtils.negated(costSource.getCostTotal()));
                            }
                        }
                        if (isExpandInvoice) {
                            expensesExpanded.add(new GLOperationDto(accExpense, accAsset, cost));
                        } else {
                            add(expensesFS, new DC(accExpense, accAsset), cost);
                        }
                    }
                }
            }

            makeOperations(vats, operations);
            makeOperations(assets, operations);
            makeOperations(expenses, operations);
            makeOperations(expensesExt, operations);
            makeOperations(expensesFS, operations);
            makeOperations(expensesExpanded, operations);

            // if returning document
            if (GamaMoneyUtils.isNegative(document.getBaseSubtotal())) {
                GamaMoney cost = GamaMoneyUtils.subtract(document.getBaseSubtotal(), costTotal);
                if (!Objects.equals(document.getExchange().getBase(), document.getExchange().getCurrency())) {
                    // if currency - check currency rate change influence
                    // isPositive if |subtotal| < |costTotal|, example: |-10.00| < |-12.00| - rate is up - positive influence
                    if (GamaMoneyUtils.isPositive(cost)) {
                        operations.add(new GLOperationDto(accountCurrRateNeg, accountVendor, cost));

                    } else if (GamaMoneyUtils.isNegative(cost)) {
                        operations.add(new GLOperationDto(accountVendor, accountCurrRatePos, GamaMoneyUtils.negated(cost)));
                    }

                } else {
                    // if the same currency - check for return with discount
                    if (GamaMoneyUtils.isPositive(cost)) {
                        operations.add(new GLOperationDto(accountTemp, accountVendor, cost));

                    } else if (GamaMoneyUtils.isNegative(cost)) {
                        operations.add(new GLOperationDto(accountVendor, accountTemp, GamaMoneyUtils.negated(cost)));
                    }
                }
            }
        }
        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(BankOperationDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountBank,
                                                    GLOperationAccount account) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountBank), "No Bank Account");
        Validators.checkArgument(Validators.isValid(account), "No Other party Account");
        Validators.checkArgument(GamaMoneyUtils.isNonZero(document.getBaseAmount()), "No base sum");

        List<GLOperationDto> operations = new ArrayList<>();

        if (GamaMoneyUtils.isPositive(document.getBaseAmount())) {
            operations.add(new GLOperationDto(accountBank, account, document.getBaseAmount()));
        } else if (GamaMoneyUtils.isNegative(document.getBaseAmount())) {
            operations.add(new GLOperationDto(account, accountBank, document.getBaseAmount().negated()));
        }

        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(CashOperationDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountCash,
                                                    GLOperationAccount account) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountCash), "No Cash Account");
        Validators.checkArgument(Validators.isValid(account), "No Other party Account");
        Validators.checkArgument(GamaMoneyUtils.isNonZero(document.getBaseAmount()), "No base sum");

        List<GLOperationDto> operations = new ArrayList<>();

        if (GamaMoneyUtils.isPositive(document.getBaseAmount())) {
            operations.add(new GLOperationDto(accountCash, account, document.getBaseAmount()));
        } else if (GamaMoneyUtils.isNegative(document.getBaseAmount())) {
            operations.add(new GLOperationDto(account, accountCash, document.getBaseAmount().negated()));
        }

        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(EmployeeOperationDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountEmployee,
                                                    GLOperationAccount accountCounterparty,
                                                    GLOperationAccount accountTmp) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountEmployee), "No Employee Account");
        Validators.checkArgument(GamaMoneyUtils.isNonZero(document.getBaseAmount()), "No base sum");

        List<GLOperationDto> operations = new ArrayList<>();

        GLOperationAccount account;
        if (Validators.isValid(accountCounterparty)) {
            account = accountCounterparty;
        } else {
            Validators.checkArgument(Validators.isValid(accountTmp), "No Tmp Account");
            account = accountTmp;
        }

        if (GamaMoneyUtils.isPositive(document.getBaseAmount())) {
            operations.add(new GLOperationDto(accountEmployee, account, document.getBaseAmount()));
        } else if (GamaMoneyUtils.isNegative(document.getBaseAmount())) {
            operations.add(new GLOperationDto(account, accountEmployee, document.getBaseAmount().negated()));
        }

        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(DebtCorrectionDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    GLOperationAccount accountDebit,
                                                    GLOperationAccount accountCredit) {

        if (document == null || (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(accountDebit), "No Debit Account");
        Validators.checkArgument(Validators.isValid(accountCredit), "No Credit Account");
        Validators.checkArgument(GamaMoneyUtils.isNonZero(document.getBaseAmount()), "No base sum");

        List<GLOperationDto> operations = new ArrayList<>();

        if (GamaMoneyUtils.isPositive(document.getBaseAmount())) {
            operations.add(new GLOperationDto(accountDebit, accountCredit, document.getBaseAmount()));
        } else {
            operations.add(new GLOperationDto(accountCredit, accountDebit, document.getBaseAmount().negated()));
        }

        glUtilsService.assignSortOrder(operations);
        return operations;
    }


    public List<GLOperationDto> generateDoubleEntry(InventoryDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    Map<Long, GLDC> accountIncome,
                                                    Map<Long, GLOperationAccount> accountsAsset,
                                                    Map<Long, GLDC> accountsExpense) {

        if (document == null || CollectionsHelper.isEmpty(document.getParts()) ||
            (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        List<GLOperationDto> operations = new ArrayList<>();
        Map<DC, GamaMoney> assets = new HashMap<>();
        for (PartInventoryDto part : document.getParts()) {

            GLOperationAccount asset = accountsAsset.get(part.getId());
            GLDC expense = accountsExpense.get(part.getId());

            if (BigDecimalUtils.isPositive(part.getQuantity())) {
                add(assets, new DC(asset, expense), part.getCostTotal());
            } else if (BigDecimalUtils.isNegative(part.getQuantity())) {
                add(assets, new DC(expense, asset), GamaMoneyUtils.negated(part.getCostTotal()));
            }
        }
        makeOperations(assets, operations);
        glUtilsService.assignSortOrder(operations);
        return operations;
    }

    public List<GLOperationDto> generateDoubleEntry(TransProdDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    Map<Long, GLOperationAccount> accountsAsset,
                                                    Map<Long, GLDC> accountsExpense,
                                                    GLOperationAccount tempAccount) {
        if (document == null || CollectionsHelper.isEmpty(document.getPartsFrom()) ||
                (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) || BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        Validators.checkArgument(Validators.isValid(tempAccount), "No Temp Account");

        List<GLOperationDto> operations = new ArrayList<>();

        Map<DC, GamaMoney> assets = new HashMap<>();
        Map<DC, GamaMoney> expensesFS = null;

        List<GLOperationDto> expensesExpanded = null;

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        boolean isExpandInvoice = companySettings.getGl() != null &&
                BooleanUtils.isTrue(companySettings.getGl().getExpandInvoice());

        if (isExpandInvoice) {
            expensesExpanded = new ArrayList<>();
        } else {
            expensesFS = new HashMap<>();
        }

        boolean isProduction = CollectionsHelper.hasValue(document.getPartsTo());

        for (PartFromDto part : document.getPartsFrom()) {

            GLOperationAccount accAsset = accountsAsset.get(part.getId());
            GLDC accExpense = accountsExpense.get(part.getId());

            if (isProduction) {
                add(assets, new DC(tempAccount, accAsset), part.getCostTotal());
            }

            if (part.isForwardSell() && CollectionsHelper.hasValue(part.getCostInfo())) {
                GamaMoney cost = null;
                for (PartCostSource costSource : part.getCostInfo()) {
                    if (costSource.isForwardSell()) {
                        cost = GamaMoneyUtils.add(cost, GamaMoneyUtils.negated(costSource.getCostTotal()));
                    }
                }
                if (isExpandInvoice) {
                    expensesExpanded.add(new GLOperationDto(accExpense, accAsset, cost));
                } else {
                    add(expensesFS, new DC(accExpense, accAsset), cost);
                }
            }
        }

        if (document.getPartsTo() != null) {
            for (PartToDto part : document.getPartsTo()) {

                GLOperationAccount accAsset = accountsAsset.get(part.getId());
                add(assets, new DC(accAsset, tempAccount), part.getCostTotal());

                GLDC accExpense = accountsExpense.get(part.getId());

                if (part.isForwardSell() && CollectionsHelper.hasValue(part.getCostInfo())) {
                    GamaMoney cost = null;
                    for (PartCostSource costSource : part.getCostInfo()) {
                        if (costSource.isForwardSell()) {
                            cost = GamaMoneyUtils.add(cost, GamaMoneyUtils.negated(costSource.getCostTotal()));
                        }
                    }
                    if (isExpandInvoice) {
                        expensesExpanded.add(new GLOperationDto(accExpense, accAsset, cost));
                    } else {
                        add(expensesFS, new DC(accExpense, accAsset), cost);
                    }
                }
            }
        }

        makeOperations(assets, operations);
        makeOperations(expensesFS, operations);
        makeOperations(expensesExpanded, operations);
        glUtilsService.assignSortOrder(operations);
        return operations;
    }

    public List<GLOperationDto> generateDoubleEntry(BaseMoneyRateInfluenceDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    Map<Long, ? extends IMoneyAccount> accounts,
                                                    GLOperationAccount accCurrRateNeg,
                                                    GLOperationAccount accCurrRatePos) {
        if (document == null || CollectionsHelper.isEmpty(document.getAccounts()) ||
                (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) ||
                        BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        List<GLOperationDto> operations = new ArrayList<>();
        Map<DC, GamaMoney> sets = new HashMap<>();

        for (BaseMoneyBalanceDto balance : document.getAccounts()) {

            IMoneyAccount<?> account = accounts.get(balance.getAccountId());

            if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) continue;

            String currency = balance.getExchange().getCurrency();
            GLOperationAccount accountBank = account.getMoneyAccount().getGLAccount(currency).getAccount();

            /*
                if +: D: account - C: Positive influence
                if -: D: Negative influence - C: account
             */

            if (GamaMoneyUtils.isPositive(balance.getBaseFixAmount())) {
                add(sets, new DC(accountBank, accCurrRatePos), balance.getBaseFixAmount());
            } else {
                add(sets, new DC(accCurrRateNeg, accountBank), GamaMoneyUtils.negated(balance.getBaseFixAmount()));
            }
        }

        makeOperations(sets, operations);
        glUtilsService.assignSortOrder(operations);
        return operations;
    }

    public List<GLOperationDto> generateDoubleEntry(DebtRateInfluenceDto document,
                                                    DoubleEntrySql doubleEntry,
                                                    Map<Long, CounterpartySql> accounts,
                                                    GLOperationAccount accCurrRateNeg,
                                                    GLOperationAccount accCurrRatePos) {
        if (document == null || CollectionsHelper.isEmpty(document.getAccounts()) ||
                (doubleEntry != null && (BooleanUtils.isTrue(doubleEntry.getFinishedGL()) ||
                        BooleanUtils.isTrue(doubleEntry.getFrozen())))) return null;

        List<GLOperationDto> operations = new ArrayList<>();
        Map<DC, GamaMoney> sets = new HashMap<>();

        for (DebtBalanceDto balance : document.getAccounts()) {

            CounterpartySql account = accounts.get(balance.getAccountId());

            if (GamaMoneyUtils.isZero(balance.getBaseFixAmount())) continue;

            GLOperationAccount accountBank = account.getAccount(balance.getType());

            /*
                if +: D: account - C: Positive influence
                if -: D: Negative influence - C: account
             */

            if (GamaMoneyUtils.isPositive(balance.getBaseFixAmount())) {
                add(sets, new DC(accountBank, accCurrRatePos), balance.getBaseFixAmount());
            } else {
                add(sets, new DC(accCurrRateNeg, accountBank), GamaMoneyUtils.negated(balance.getBaseFixAmount()));
            }
        }

        makeOperations(sets, operations);
        glUtilsService.assignSortOrder(operations);
        return operations;
    }

    private void add(Map<DC, GamaMoney> accounts, DC key, GamaMoney amount) {
        GamaMoney value = accounts.get(key);
        value = GamaMoneyUtils.add(value, amount);
        if (value == null) {
            accounts.remove(key);
        } else {
            accounts.put(key, value);
        }
    }

    private void addAuto(Map<DC, GamaMoney> accounts, DC key, GamaMoney amount) {
        if (GamaMoneyUtils.isNegative(amount)) {
            key = key.reverse();
            amount = amount.negated();
        }
        GamaMoney value = accounts.get(key);
        value = GamaMoneyUtils.add(value, amount);
        if (value == null) {
            accounts.remove(key);
        } else {
            accounts.put(key, value);
        }
    }

    private static class DC implements Comparable<DC> {

        GLOperationAccount debit;

        GLOperationAccount credit;

        DC(GLOperationAccount debit, GLOperationAccount credit) {
            this.debit = debit;
            this.credit = credit;
        }

        DC(GLOperationAccount debit, GLDC credit) {
            this(debit, credit == null ? null : credit.getCreditEx());
        }

        DC(GLDC debit, GLOperationAccount credit) {
            this(debit == null ? null : debit.getDebitEx(), credit);
        }


        public DC reverse() {
            return new DC(credit, debit);
        }

        public GLOperationAccount getDebit() {
            return debit;
        }

        public void setDebit(GLOperationAccount debit) {
            this.debit = debit;
        }

        public GLOperationAccount getCredit() {
            return credit;
        }

        public void setCredit(GLOperationAccount credit) {
            this.credit = credit;
        }

            public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DC dc)) return false;
            return Objects.equals(debit, dc.debit) &&
                    Objects.equals(credit, dc.credit);
        }

            public int hashCode() {
            return Objects.hash(debit, credit);
        }

            public int compareTo(DC o) {
            if (o == null) return 1;
            int d = debit.getNumber().compareTo(o.debit.getNumber());
            return d != 0 ? d : credit.getNumber().compareTo(o.credit.getNumber());
        }
    }

    private static class DCComparator implements Comparator<Map.Entry<DC, GamaMoney>> {
            public int compare(Map.Entry<DC, GamaMoney> o1, Map.Entry<DC, GamaMoney> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    private void makeOperations(Map<DC, GamaMoney> sets, List<GLOperationDto> operations) {
        if (CollectionsHelper.isEmpty(sets) || operations == null) return;
        List<Map.Entry<DC, GamaMoney>> list = new ArrayList<>(sets.entrySet());
        list.sort(new DCComparator());
        for (Map.Entry<DC, GamaMoney> pair : list) {
            if (GamaMoneyUtils.isNonZero(pair.getValue())) {
                operations.add(new GLOperationDto(pair.getKey().getDebit(), pair.getKey().getCredit(), pair.getValue()));
            }
        }
    }

    private void makeOperations(List<GLOperationDto> src, List<GLOperationDto> operations) {
        if (CollectionsHelper.isEmpty(src) || operations == null) return;
        Collections.sort(src);
        operations.addAll(src);
    }
}
