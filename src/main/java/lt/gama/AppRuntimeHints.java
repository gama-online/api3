package lt.gama;

import lt.gama.api.request.PageRequest;
import lt.gama.jpa.*;
import lt.gama.jpa.generators.CreatorValueGeneration;
import lt.gama.jpa.generators.EditorValueGeneration;
import lt.gama.model.type.*;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.auth.*;
import lt.gama.model.type.base.BaseChild;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.base.BaseMoneyBalance;
import lt.gama.model.type.cf.CF;
import lt.gama.model.type.cf.CFDescription;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.*;
import lt.gama.model.type.gl.*;
import lt.gama.model.type.inventory.InvoiceNote;
import lt.gama.model.type.l10n.*;
import lt.gama.model.type.part.*;
import lt.gama.model.type.sync.SyncAbilities;
import lt.gama.model.type.sync.SyncDirection;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.model.type.sync.WarehouseAbilities;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import static org.springframework.aot.hint.MemberCategory.*;

public class AppRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // gama.jpa
        hints.reflection().registerType(CamelCaseToSnakeCaseNamingStrategy.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(GamaFunctionContributor.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(GamaPostgreSQLDialect.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(GamaSchemaFilterProvider.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(ImplicitNamingStrategyComponentPath.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);

        // gama.jpa.generators
        hints.reflection().registerType(CreatorValueGeneration.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(EditorValueGeneration.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);

        // gama.api.request
        hints.reflection().registerType(PageRequest.class, INVOKE_PUBLIC_CONSTRUCTORS);

        // hibernate
        hints.reflection().registerType(org.hibernate.generator.internal.CurrentTimestampGeneration.class, INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(TypeReference.of("com.google.cloud.sql.postgres.SocketFactory"), INVOKE_PUBLIC_CONSTRUCTORS, INVOKE_PUBLIC_METHODS);

        // java
        hints.reflection().registerType(java.util.HashMap.class);
        hints.reflection().registerType(java.util.HashSet.class);
        hints.reflection().registerType(java.util.LinkedHashMap.class);
        hints.reflection().registerType(java.util.LinkedHashSet.class);
        hints.reflection().registerType(java.util.LinkedList.class);
        hints.reflection().registerType(java.util.TreeMap.class);
        hints.reflection().registerType(java.util.TreeSet.class);

        hints.reflection().registerType(java.time.LocalDate.class);
        hints.reflection().registerType(java.time.LocalDateTime.class);
        hints.reflection().registerType(TypeReference.of("java.time.Ser"));


        // model.type.asset
        hints.serialization().registerType(AssetHistory.class);
        hints.serialization().registerType(Depreciation.class);

        // model.type.auth
        hints.serialization().registerType(AccountInfo.class);
        hints.serialization().registerType(BankCard.class);
        hints.serialization().registerType(CompanyAccount.class);
        hints.serialization().registerType(CompanyInfo.class);
        hints.serialization().registerType(CompanySalarySettings.class);
        hints.serialization().registerType(CompanySettings.class);
        hints.serialization().registerType(CompanySettingsGL.class);
        hints.serialization().registerType(CompanyTaxSettings.class);
        hints.serialization().registerType(CounterDesc.class);
        hints.serialization().registerType(CurrencySettings.class);
        hints.serialization().registerType(EmployeeRole.class);
        hints.serialization().registerType(GpaisSettings.class);
        hints.serialization().registerType(SalesSettings.class);
        hints.serialization().registerType(VATRatesDate.class);

        // model.type.base
        hints.serialization().registerType(BaseChild.class);
        hints.serialization().registerType(BaseDocEntity.class);
        hints.serialization().registerType(BaseDocPart.class);
        hints.serialization().registerType(BaseMoneyBalance.class);

        // model.type.cf
        hints.serialization().registerType(CF.class);
        hints.serialization().registerType(CFDescription.class);
        hints.serialization().registerType(CFValue.class);

        // model.type.doc
        hints.serialization().registerType(Doc.class);
        hints.serialization().registerType(DocBank.class);
        hints.serialization().registerType(DocBankAccount.class);
        hints.serialization().registerType(DocBankAccountBalance.class);
        hints.serialization().registerType(DocCash.class);
        hints.serialization().registerType(DocCashBalance.class);
        hints.serialization().registerType(DocCharge.class);
        hints.serialization().registerType(DocChargeAmount.class);
        hints.serialization().registerType(DocCompany.class);
        hints.serialization().registerType(DocCounterparty.class);
        hints.serialization().registerType(DocDebt.class);
        hints.serialization().registerType(DocDebtBalance.class);
        hints.serialization().registerType(DocEmployee.class);
        hints.serialization().registerType(DocEmployeeBalance.class);
        hints.serialization().registerType(DocExpense.class);
        hints.serialization().registerType(DocManufacturer.class);
        hints.serialization().registerType(DocPartSync.class);
        hints.serialization().registerType(DocPl.class);
        hints.serialization().registerType(DocPosition.class);
        hints.serialization().registerType(DocRC.class);
        hints.serialization().registerType(DocRecipe.class);
        hints.serialization().registerType(DocWarehouse.class);
        hints.serialization().registerType(DocWorkHours.class);
        hints.serialization().registerType(DocWorkSchedule.class);

        // model.type.gl
        hints.serialization().registerType(GLCurrencyAccount.class);
        hints.serialization().registerType(GLDC.class);
        hints.serialization().registerType(GLDCActive.class);
        hints.serialization().registerType(GLMoneyAccount.class);
        hints.serialization().registerType(GLOperationAccount.class);

        // model.type.inventory
        hints.serialization().registerType(InvoiceNote.class);

        // model.type.i10n
        hints.serialization().registerType(LangBase.class);
        hints.serialization().registerType(LangEmployee.class);
        hints.serialization().registerType(LangEmployee.class);
        hints.serialization().registerType(LangGLAccount.class);
        hints.serialization().registerType(LangInventory.class);
        hints.serialization().registerType(LangInvoice.class);
        hints.serialization().registerType(LangInvoiceNote.class);
        hints.serialization().registerType(LangPart.class);
        hints.serialization().registerType(LangVatNote.class);

        // model.type.part
        hints.serialization().registerType(DocPart.class);
        hints.serialization().registerType(DocPartBalance.class);
        hints.serialization().registerType(DocPartEstimate.class);
        hints.serialization().registerType(DocPartEstimateSubpart.class);
        hints.serialization().registerType(DocPartFrom.class);
        hints.serialization().registerType(DocPartHistory.class);
        hints.serialization().registerType(DocPartInventory.class);
        hints.serialization().registerType(DocPartInvoice.class);
        hints.serialization().registerType(DocPartInvoiceSubpart.class);
        hints.serialization().registerType(DocPartOB.class);
        hints.serialization().registerType(DocPartOrder.class);
        hints.serialization().registerType(DocPartPart.class);
        hints.serialization().registerType(DocPartPl.class);
        hints.serialization().registerType(DocPartPlActual.class);
        hints.serialization().registerType(DocPartPlDiscount.class);
        hints.serialization().registerType(DocPartPurchase.class);
        hints.serialization().registerType(DocPartTo.class);
        hints.serialization().registerType(PartCostSource.class);
        hints.serialization().registerType(PartRemainder.class);
        hints.serialization().registerType(PartSN.class);
        hints.serialization().registerType(VATRate.class);

        // model.type.sync
        hints.serialization().registerType(SyncAbilities.class);
        hints.serialization().registerType(SyncDirection.class);
        hints.serialization().registerType(SyncSettings.class);
        hints.serialization().registerType(WarehouseAbilities.class);

        // model.type
        hints.serialization().registerType(Contact.class);
        hints.serialization().registerType(Exchange.class);
        hints.serialization().registerType(ExternalUrl.class);
        hints.serialization().registerType(GamaBigMoney.class);
        hints.serialization().registerType(GamaMoney.class);
        hints.serialization().registerType(Location.class);
        hints.serialization().registerType(NameContact.class);
        hints.serialization().registerType(Packaging.class);

        // java
        hints.serialization().registerType(Boolean.class);
        hints.serialization().registerType(Long.class);
        hints.serialization().registerType(Integer.class);
        hints.serialization().registerType(Double.class);
        hints.serialization().registerType(Number.class);

        hints.serialization().registerType(java.math.BigDecimal.class);
        hints.serialization().registerType(java.math.BigInteger.class);

        hints.serialization().registerType(java.time.LocalDate.class);
        hints.serialization().registerType(java.time.LocalDateTime.class);
        hints.serialization().registerType(TypeReference.of("java.time.Ser"));

        hints.serialization().registerType(java.util.ArrayList.class);
        hints.serialization().registerType(java.util.HashMap.class);
        hints.serialization().registerType(java.util.HashSet.class);
        hints.serialization().registerType(java.util.LinkedHashMap.class);
        hints.serialization().registerType(java.util.LinkedHashSet.class);
        hints.serialization().registerType(java.util.LinkedList.class);
        hints.serialization().registerType(java.util.TreeMap.class);
        hints.serialization().registerType(java.util.TreeSet.class);
    }
}
