package lt.gama.api.impl.v4;

import com.google.common.base.Objects;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.v4.CounterpartyApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.mappers.CounterpartySqlMapper;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DebtService;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.ArrayList;

@RestController("CounterpartyApiImplv4")
public class CounterpartyApiImpl implements CounterpartyApi {

    private final Auth auth;
    private final CounterpartySqlMapper counterpartySqlMapper;
    private final DebtService debtService;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;

    public CounterpartyApiImpl(Auth auth, CounterpartySqlMapper counterpartySqlMapper, DebtService debtService, DBServiceSQL dbServiceSQL, APIResultService apiResultService) {
        this.auth = auth;
        this.counterpartySqlMapper = counterpartySqlMapper;
        this.debtService = debtService;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
    }


    @Override
    public PageResponse<CounterpartyDto, Void> list(Integer cursor, int pageSize) throws GamaApiException {
        Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                        pageSize, PageRequest.MAX_PAGE_SIZE));

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageSize(pageSize);
        pageRequest.setCursor(cursor);
        pageRequest.setOrder("mainIndex");
        return debtService.pageCounterparty(pageRequest);
    }

    @Override
    public CounterpartyDto get(long id) throws GamaApiException {
        return apiResultService.execute(() -> {
            CounterpartySql entity = dbServiceSQL.getAndCheckNullable(CounterpartySql.class, id);
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyWithId, auth.getLanguage()),
                        id));
            }
            return counterpartySqlMapper.toDto(entity);
        });
    }

    @Override
    public PageResponse<CounterpartyDto, Void> findBy(String code, String name, Integer cursor, int pageSize) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(StringHelper.hasValue(code) || StringHelper.hasValue(name),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyCode, auth.getLanguage()) + ", " +
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoName, auth.getLanguage())
            );
            Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                    MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                            pageSize, PageRequest.MAX_PAGE_SIZE));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageSize(pageSize);
            pageRequest.setCursor(cursor);
            pageRequest.setOrder("mainIndex");
            pageRequest.setConditions(new ArrayList<>());

            if (StringHelper.hasValue(code)) pageRequest.getConditions().add(new PageRequestCondition(CustomSearchType.COUNTERPARTY_COM_CODE, code));
            if (StringHelper.hasValue(name)) pageRequest.getConditions().add(new PageRequestCondition(CustomSearchType.COUNTERPARTY_NAME, EntityUtils.prepareName(name)));

            return debtService.pageCounterparty(pageRequest);
        });
    }

    @Override
    public CounterpartyDto getByForeignId(long foreignId) throws GamaApiException {
        return apiResultService.execute(() -> {
            CounterpartySql entity = dbServiceSQL.getByForeignId(CounterpartySql.class, foreignId);
            if (entity == null) {
                throw new GamaNotFoundException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty, auth.getLanguage()));
            }
            return counterpartySqlMapper.toDto(entity);
        });
    }

    @Override
    public CounterpartyDto create(CounterpartyDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Validators.checkArgument(dto.getId() == null, "id specified");
            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoName, auth.getLanguage()));

            dto.setId(null);

            CounterpartySql entity = counterpartySqlMapper.toEntity(dto);

            if (!companySettings.isDisableGL()) {
                entity.setAccount(DebtType.VENDOR, companySettings.getGl().getCounterpartyVendor());
                entity.setAccount(DebtType.CUSTOMER, companySettings.getGl().getCounterpartyCustomer());
            }

            dbServiceSQL.saveEntityInCompany(entity);

            return counterpartySqlMapper.toDto(entity);
        });
    }

    @Override
    public CounterpartyDto update(CounterpartyDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(dto.getId() != null && dto.getId() != 0,
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyId, auth.getLanguage()));

            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoName, auth.getLanguage()));

            CounterpartySql entity = dbServiceSQL.getAndCheckNullable(CounterpartySql.class, dto.getId());
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterpartyWithId, auth.getLanguage()),
                        dto.getId()));
            }

            boolean updated = false;
            if (!StringHelper.isEquals(entity.getName(), dto.getName())) {
                entity.setName(dto.getName());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getComCode(), dto.getComCode())) {
                entity.setComCode(dto.getComCode());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getVatCode(), dto.getVatCode())) {
                entity.setVatCode(dto.getVatCode());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getShortName(), dto.getShortName())) {
                entity.setShortName(dto.getShortName());
                updated = true;
            }
            if (!Objects.equal(entity.getBusinessAddress(), dto.getBusinessAddress())) {
                entity.setBusinessAddress(dto.getBusinessAddress());
                updated = true;
            }
            if (!Objects.equal(entity.getPostAddress(), dto.getPostAddress())) {
                entity.setPostAddress(dto.getPostAddress());
                updated = true;
            }
            if (!Objects.equal(entity.getRegistrationAddress(), dto.getRegistrationAddress())) {
                entity.setRegistrationAddress(dto.getRegistrationAddress());
                updated = true;
            }
            if (!Objects.equal(entity.getLocations(), dto.getLocations())) {
                entity.setLocations(dto.getLocations());
                updated = true;
            }
            if (!Objects.equal(entity.getContacts(), dto.getContacts())) {
                entity.setContacts(dto.getContacts());
                updated = true;
            }

            if (updated) dbServiceSQL.saveEntityInCompany(entity);

            return counterpartySqlMapper.toDto(entity);
        });
    }
}
