package lt.gama.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lt.gama.Constants;
import lt.gama.api.request.MailRequestContact;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.mappers.CounterpartySqlMapper;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.mappers.WarehouseSqlMapper;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.sql.system.*;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.service.ex.subscription.GamaSubscriptionCompanyNotActiveException;
import lt.gama.service.ex.subscription.GamaSubscriptionNoCounterpartyException;
import lt.gama.service.ex.subscription.GamaSubscriptionZeroConnectionsException;
import lt.gama.service.repo.ConnectionRepository;
import lt.gama.tasks.RefreshCompanyConnectionsTask;
import lt.gama.tasks.SubscriptionsCountCompanyTask;
import lt.gama.tasks.SubscriptionsInvoicingCompanyTask;
import lt.gama.tasks.UpdateCompanyLastSubscriptionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * gama-online
 * Created by valdas on 2015-12-28.
 */
@Service
public class AccountingService {

    private static final Logger log = LoggerFactory.getLogger(AccountingService.class);

    @PersistenceContext
    protected EntityManager entityManager;
    
    private final TradeService tradeService;
    private final InventoryService inventoryService;
    private final MailService mailService;
    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final WarehouseSqlMapper warehouseSqlMapper;
    private final PartSqlMapper partSqlMapper;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final ConnectionRepository connectionRepository;
    private final DBServiceSQL dbServiceSQL;
    private final TaskQueueService taskQueueService;


    AccountingService(TradeService tradeService,
                      InventoryService inventoryService,
                      MailService mailService, Auth auth,
                      AuthSettingsCacheService authSettingsCacheService,
                      WarehouseSqlMapper warehouseSqlMapper,
                      PartSqlMapper partSqlMapper,
                      CounterpartySqlMapper counterpartySqlMapper,
                      ConnectionRepository connectionRepository,
                      DBServiceSQL dbServiceSQL,
                      TaskQueueService taskQueueService) {
        this.tradeService = tradeService;
        this.inventoryService = inventoryService;
        this.mailService = mailService;
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.warehouseSqlMapper = warehouseSqlMapper;
        this.partSqlMapper = partSqlMapper;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.connectionRepository = connectionRepository;
        this.dbServiceSQL = dbServiceSQL;
        this.taskQueueService = taskQueueService;
    }

    @Transactional
    public ConnectionSql updateConnections(CompanySql company, LocalDate date) {
        ConnectionSql connection = getConnection(company.getId(), date);
        if (connection == null) {
            connection = new ConnectionSql(company.getId(), date);
            connection.setActiveAccounts(company.getActiveAccounts());
            connection.setPayerAccounts(company.getPayerAccounts());
            connection.setOtherAccounts(company.getOtherAccounts());
        } else if (connection.getTotalAccounts() < company.getTotalAccounts()) {
            connection.setActiveAccounts(company.getActiveAccounts());
            connection.setPayerAccounts(company.getPayerAccounts());
            connection.setOtherAccounts(company.getOtherAccounts());
        }
        return connectionRepository.save(connection);
    }

    @Transactional
    public void setConnections(CompanySql company, LocalDate date) {
        ConnectionSql connection = getConnection(company.getId(), date);
        if (connection == null) connection = new ConnectionSql(company.getId(), date);
        connection.setActiveAccounts(company.getActiveAccounts());
        connection.setPayerAccounts(company.getPayerAccounts());
        connection.setOtherAccounts(company.getOtherAccounts());
        connection.setDate(date);
        connectionRepository.save(connection);
    }

    public void refreshCompanyConnections(Long companyId, LocalDate date) {
        // need the delay because javax.persistence.OptimisticLockException
        if (companyId != null) taskQueueService.queueTask(new RefreshCompanyConnectionsTask(companyId, date), 5);
    }

    public Set<Long> refreshCompaniesConnections(long companyId, LocalDate date, Long... payerIds) {
        final Set<Long> refreshedCompanies = new HashSet<>();

        refreshedCompanies.add(companyId);
        if (payerIds != null) refreshedCompanies.addAll(Stream.of(payerIds).filter(Objects::nonNull).toList());
        refreshedCompanies.addAll(entityManager.createQuery(
                "SELECT payer.id FROM " + AccountSql.class.getName() + " a" +
                        " WHERE payer IS NOT null" +
                        " AND (a.archive IS null OR a.archive = false)", Long.class)
                .getResultStream()
                .collect(Collectors.toSet()));

        refreshedCompanies.forEach(id -> refreshCompanyConnections(id, date));
        return refreshedCompanies;
    }

    private GamaMoney getAccountPrice(CompanySql company) {
        GamaMoney accountPrice;
        if (GamaMoneyUtils.isPositive(company.getAccountPrice())) {
            accountPrice = company.getAccountPrice();
        } else {
            SystemSettingsSql systemSettings = Validators.checkNotNull(dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID), "No SystemSettings");
            Validators.checkNotNull(systemSettings.getAccountPrice(), "No default account price");
            accountPrice = systemSettings.getAccountPrice();
        }
        return accountPrice;
    }

    public GamaMoney companyAmountToPay(CompanySql company) {
        if (GamaMoneyUtils.isPositive(company.getTotalPrice())) {
            return company.getTotalPrice();
        }
        if (company.getTotalAccounts() <= 0) return null;
        GamaMoney accountPrice = getAccountPrice(company);
        return GamaMoneyUtils.isZero(accountPrice) ? null : accountPrice.multipliedBy(company.getTotalAccounts());
    }

    public void invoicingAll() {
        List<Long> keys = entityManager.createQuery(
                "SELECT id FROM " + CompanySql.class.getName() + " a" +
                        " WHERE a.status = :status" +
                        " AND (a.archive IS null OR a.archive = false)",
                        Long.class)
                .setParameter("status", CompanyStatusType.SUBSCRIBER)
                .getResultList();

        if (CollectionsHelper.isEmpty(keys)) {
            log.info(this.getClass().getSimpleName() + ": No subscribers");
            return;
        }
        keys.forEach(companyId -> taskQueueService.queueTask(new SubscriptionsInvoicingCompanyTask(companyId)));
    }

    public void sendInvoicingErrorAdminEMail(String msgBody) {
        SystemSettingsSql systemSettings = Validators.checkNotNull(dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID), "No SystemSettings");
        long ownerCompanyId = Validators.checkNotNull(systemSettings.getOwnerCompanyId(), "No owner in SystemSettings");
        CompanySql owner = Validators.checkNotNull(
                dbServiceSQL.getById(CompanySql.class, ownerCompanyId),
                "No owner company " + ownerCompanyId);
        Validators.checkArgument(StringHelper.hasValue(owner.getEmail()), "No owner company " + ownerCompanyId + " email");
        sendInvoicingErrorAdminEMail(owner.getEmail(), msgBody);
    }

    private void sendInvoicingErrorAdminEMail(String email, String msgBody) {
        mailService.sendMail(StringHelper.hasValue(email) ? email : Constants.DEFAULT_SENDER_EMAIL, "Gama - worker", Constants.DEFAULT_ADMIN_EMAIL,
                "Administrator", "Invoicing error", msgBody, null,
                null, null, null);
    }

    public InvoiceDto invoicingCompany(LocalDate date, long subscriberCompanyId) {
        return invoicingCompany(date, subscriberCompanyId, false);
    }

    public InvoiceDto invoicingCompany(LocalDate date, long subscriberCompanyId, boolean debug) {
        Validators.checkArgument(subscriberCompanyId != 0, "Company id = 0");
        CompanySql subscriberCompany = Validators.checkNotNull(
                dbServiceSQL.getById(CompanySql.class, subscriberCompanyId),
                "No company with id " + subscriberCompanyId);
        if (Validators.isValid(subscriberCompany.getPayer())) {
            log.info(this.getClass().getSimpleName() + ": " + subscriberCompany.getName() +  "(" + subscriberCompanyId + ")" + " has payer " + subscriberCompany.getPayer().getId());
            return null;
        }

        SystemSettingsSql systemSettings = Validators.checkNotNull(dbServiceSQL.getById(SystemSettingsSql.class, SystemSettingsSql.ID), "No SystemSettings");

        long ownerCompanyId = Validators.checkNotNull(systemSettings.getOwnerCompanyId(), "No owner in SystemSettings");
        CompanySql owner = Validators.checkNotNull(
                dbServiceSQL.getById(CompanySql.class, ownerCompanyId),
                "No owner company " + ownerCompanyId);
        Validators.checkNotNull(owner.getSettings(), "No owner company " + ownerCompanyId + " settings");
        Validators.checkNotNull(owner.getSettings().getCurrency(), "No owner company " + ownerCompanyId + " currency settings");
        Validators.checkArgument(StringHelper.hasValue(owner.getSettings().getCurrency().getCode()), "No owner company " + ownerCompanyId + " currency");

        auth.setCompanyId(ownerCompanyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        InvoiceDto invoice;
        try {
            invoice = generateSubscriptionInvoice(date, systemSettings, subscriberCompany, owner.getSettings().getCurrency().getCode(), debug);
            if (debug) log.info(this.getClass().getSimpleName() + ": " + invoice);
        } catch (jakarta.persistence.RollbackException e) {
            log.warn(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            throw e;
        } catch (GamaSubscriptionZeroConnectionsException e) {
            log.info(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        } catch (GamaSubscriptionCompanyNotActiveException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            sendInvoicingErrorAdminEMail(owner.getEmail(), "Invoicing error for company " + subscriberCompany.getName() +
                    " (" + subscriberCompany.getId() + ")" + "\n" + e.getMessage());
            return null;
         }

        if (invoice == null) {
            String msg = "Invoicing error for company " + subscriberCompany.getName() +
                    " (" + subscriberCompany.getId() + ") - No invoice generated";
            if (!debug) sendInvoicingErrorAdminEMail(owner.getEmail(), msg);
            log.error(this.getClass().getSimpleName() + ": " + msg);
            return null;
        }

        if (subscriberCompany.getSubscriberEmail() == null || subscriberCompany.getSubscriberEmail().isEmpty()) {
            String msg = "Can't send invoice - No Subscriber Email for company " + subscriberCompany.getName() +
                    " (" + subscriberCompany.getId() + ")";
            if (!debug) sendInvoicingErrorAdminEMail(owner.getEmail(), msg);
            log.error(this.getClass().getSimpleName() + ": " + msg);
            return null;
        }

        if (!debug) {
            List<MailRequestContact> recipients = new ArrayList<>();
            recipients.add(new MailRequestContact(subscriberCompany.getSubscriberEmail(), subscriberCompany.getSubscriberName()));
            try {
                inventoryService.emailInvoice(invoice.getId(), recipients, null, null);
            } catch (GamaUnauthorizedException e) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }

        return invoice;
    }

    private PartInvoiceDto calculateSubscriptionDocPart(LocalDate date, SystemSettingsSql systemSettings, CompanySql subscriberCompany)
            throws GamaSubscriptionCompanyNotActiveException, GamaSubscriptionZeroConnectionsException {

        if (BooleanUtils.isTrue(subscriberCompany.getArchive()) || subscriberCompany.getStatus() != CompanyStatusType.SUBSCRIBER) {
            throw new GamaSubscriptionCompanyNotActiveException("Company not active or deleted, id "+ subscriberCompany.getId());
        }

        GamaMoney price;
        BigDecimal quantity;

        if (GamaMoneyUtils.isPositive(subscriberCompany.getTotalPrice())) {
            price = subscriberCompany.getTotalPrice();
            quantity = BigDecimal.ONE;

        } else {
            int totalConnections = getTotalConnectionsPerMonth(subscriberCompany.getId(), date);
            if (totalConnections == 0) throw new GamaSubscriptionZeroConnectionsException("No connections in company " + subscriberCompany.getId());
            if (totalConnections < 0) throw new GamaException("Negative connections count (" + totalConnections + ") in company " + subscriberCompany.getId());

            price = GamaMoneyUtils.isPositive(subscriberCompany.getAccountPrice()) ? subscriberCompany.getAccountPrice() :
            Validators.checkNotNull(systemSettings.getAccountPrice(), "No default account price");
            Validators.checkArgument(GamaMoneyUtils.isPositive(price), "No account price");
            quantity = BigDecimal.valueOf(totalConnections);
        }

        // check if not full month
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        if (subscriberCompany.getSubscriptionDate() != null && subscriberCompany.getSubscriptionDate().isAfter(firstDayOfMonth)) {
            if (subscriberCompany.getSubscriptionDate().getMonthValue() == firstDayOfMonth.getMonthValue()) {
                int daysInMonth = firstDayOfMonth.lengthOfMonth();
                int activeDays = daysInMonth - subscriberCompany.getSubscriptionDate().getDayOfMonth() + 1;
                price = price.multipliedBy(1.0 * activeDays / daysInMonth);
            }
        }

        long ownerCompanyId = systemSettings.getOwnerCompanyId();
        CompanySettings ownerCompanySettings = dbServiceSQL.getAndCheckCompanySettings(ownerCompanyId);
        String ownerCountry = Validators.checkNotNull(ownerCompanySettings.getCountry(), "No country in Owner Settings");
        CountryVatRateSql countryVatRate = dbServiceSQL.getById(CountryVatRateSql.class, ownerCountry);
        VATRatesDate vatRatesDate = countryVatRate == null ? null : countryVatRate.getRatesMap(date);

        long subscriptionServiceId = Validators.checkNotNull(systemSettings.getSubscriptionServiceId(), "No Subscription Service Id in SystemSettings");
        PartDto subscriptionService = partSqlMapper.toDto(dbServiceSQL.getAndCheck(PartSql.class, subscriptionServiceId));

        PartInvoiceDto docPartInvoice = new PartInvoiceDto(subscriptionService,
                subscriptionService.getVatRateCode() != null && vatRatesDate != null && vatRatesDate.getRatesMap() != null ?
                        vatRatesDate.getRatesMap().get(subscriptionService.getVatRateCode()) : null);
        docPartInvoice.setQuantity(quantity);
        docPartInvoice.setPrice(GamaBigMoney.of(price));
        docPartInvoice.setDiscountedPrice(docPartInvoice.getPrice());
        docPartInvoice.setTotal(GamaMoneyUtils.toMoney(docPartInvoice.getDiscountedPrice().multipliedBy(quantity)));
        docPartInvoice.setDiscountedTotal(docPartInvoice.getTotal());

        return docPartInvoice;
    }

    public InvoiceDto generateSubscriptionInvoice(LocalDate date, SystemSettingsSql systemSettings, CompanySql company, String currency)
            throws GamaSubscriptionCompanyNotActiveException, GamaSubscriptionNoCounterpartyException, GamaSubscriptionZeroConnectionsException {
        return generateSubscriptionInvoice(date, systemSettings, company, currency, false);
    }

    public InvoiceDto generateSubscriptionInvoice(LocalDate date, SystemSettingsSql systemSettings, CompanySql company, String currency, boolean debug)
            throws GamaSubscriptionCompanyNotActiveException, GamaSubscriptionNoCounterpartyException, GamaSubscriptionZeroConnectionsException {
        Validators.checkNotNull(company, "No company");
        Validators.checkNotNull(systemSettings.getSubscriptionServiceId(), "No Subscription Service Id in SystemSettings");
        Validators.checkNotNull(systemSettings.getSubscriptionWarehouseId(), "No Subscription Warehouse Id in SystemSettings");

        if (BooleanUtils.isTrue(company.getArchive()) || !CompanyStatusType.SUBSCRIBER.equals(company.getStatus())) {
            throw new GamaSubscriptionCompanyNotActiveException("Company not active or deleted, id " + company.getId());
        }

        // check if company has active connections - if zero 'GamaSubscriptionZeroConnectionsException' will be thrown
        PartInvoiceDto docPartInvoice = Validators.checkNotNull(calculateSubscriptionDocPart(date, systemSettings, company),
                "No invoice part generated for company " + company.getId());

        String code = Validators.checkNotNull(company.getCode(), "No company code");
        List<CounterpartySql> counterparties = entityManager.createQuery(
                "SELECT a FROM " + CounterpartySql.class.getName() + " a" +
                        " WHERE companyId = :companyId" +
                        " AND (a.archive IS null OR a.archive = false)" +
                        " AND comCode = :comCode",
                        CounterpartySql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("comCode", code)
                .getResultList();

        if (CollectionsHelper.isEmpty(counterparties)) {
            throw new GamaSubscriptionNoCounterpartyException("No subscription counterparty with code '" + code + "'");
        }
        CounterpartyDto counterparty = counterpartySqlMapper.toDto(counterparties.get(0));
        WarehouseDto warehouse = warehouseSqlMapper.toDto(dbServiceSQL.getAndCheck(WarehouseSql.class, systemSettings.getSubscriptionWarehouseId()));

        InvoiceDto invoice = new InvoiceDto();
        invoice.setDate(date);
        invoice.setDueDate(counterparty.getCreditTerm() == null ? date : date.plusDays(counterparty.getCreditTerm()));
        invoice.setAutoNumber(true);
        invoice.setWarehouse(warehouse);
        invoice.setCounterparty(counterparty);
        invoice.setLocation(company.getLocation());
        if (!LocationUtils.isValid(invoice.getLocation())) invoice.setLocation(counterparty.getLocation());
        invoice.setExchange(new Exchange(currency));
        invoice.setParts(new ArrayList<>());

        invoice.getParts().add(docPartInvoice);

        invoice.setSubtotal(docPartInvoice.getTotal());
        if (docPartInvoice.isTaxable() && docPartInvoice.getVat() != null) {
            Double vatRate = docPartInvoice.getVat().getRate();
            invoice.setTaxTotal(docPartInvoice.getTotal().multipliedBy(vatRate == null ? 0.0 : vatRate / 100.0));
        }
        invoice.setTotal(GamaMoneyUtils.add(invoice.getSubtotal(), invoice.getTaxTotal()));

        if (!debug) {
            invoice = tradeService.saveInvoice(invoice);
            taskQueueService.queueTask(new UpdateCompanyLastSubscriptionTask(company.getId(), invoice.getSubtotal()));
        }
        return invoice;
    }

    public void subscriptionsUpdatingAll() {
        List<Long> keys = entityManager.createQuery(
                "SELECT id FROM " + CompanySql.class.getName() + " a" +
                        " WHERE a.status = :status" +
                        " AND (a.archive IS null OR a.archive = false)",
                        Long.class)
                .setParameter("status", CompanyStatusType.SUBSCRIBER)
                .getResultList();

        if (CollectionsHelper.isEmpty(keys)) {
            log.info(this.getClass().getSimpleName() + ": No subscribers");
            return;
        }
        keys.forEach(companyId -> taskQueueService.queueTask(new SubscriptionsCountCompanyTask(companyId)));
    }

    public void subscriptionsUpdate(long companyId, LocalDate date) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            updateConnections(company, date);
        });
    }

    public ConnectionSql getConnection(long companyId, LocalDate date) {
        List<ConnectionSql> connections = entityManager.createQuery(
                "SELECT a FROM " + ConnectionSql.class.getName() + " a" +
                        " WHERE companyId = :companyId AND date = :date",
                        ConnectionSql.class)
                .setParameter("companyId", companyId)
                .setParameter("date", date)
                .getResultList();
        return connections.isEmpty() ? null : connections.get(0);
    }

    public ConnectionSql getMaxConnectionPerMonth(long companyId, LocalDate date) {
        List<ConnectionSql> connections = entityManager.createQuery(
                "SELECT a FROM " + ConnectionSql.class.getName() + " a" +
                        " LEFT JOIN " + ConnectionSql.class.getName() + " c ON" +
                        "  c.companyId = :companyId" +
                        "  AND c.date >= :dateFrom AND c.date < :dateTo" +
                        "  AND a.activeAccounts + a.payerAccounts < c.activeAccounts + c.payerAccounts" +
                        " WHERE c.id IS NULL" +
                        " AND a.companyId = :companyId" +
                        " AND a.date >= :dateFrom AND a.date < :dateTo",
                        ConnectionSql.class)
                .setParameter("companyId", companyId)
                .setParameter("dateFrom", date.withDayOfMonth(1))
                .setParameter("dateTo", date.withDayOfMonth(1).plusMonths(1))
                .getResultList();
        return connections.isEmpty() ? null : connections.get(0);
    }

    public int getTotalConnectionsPerMonth(long companyId, LocalDate date) {
        try {
            Integer count = entityManager.createQuery(
                            "SELECT MAX(a.activeAccounts + a.payerAccounts) FROM " + ConnectionSql.class.getName() + " a" +
                                    " WHERE companyId = :companyId" +
                                    " AND date >= :dateFrom AND date < :dateTo",
                            Integer.class)
                    .setParameter("companyId", companyId)
                    .setParameter("dateFrom", date.withDayOfMonth(1))
                    .setParameter("dateTo", date.withDayOfMonth(1).plusMonths(1))
                    .getSingleResult();
            return count == null ? 0 : count;
        } catch (NoResultException e) {
            return 0;
        }
    }
}
