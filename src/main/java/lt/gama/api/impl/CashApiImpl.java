package lt.gama.api.impl;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.CashApi;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.documents.CashOpeningBalanceDto;
import lt.gama.model.dto.documents.CashOperationDto;
import lt.gama.model.dto.documents.CashRateInfluenceDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.mappers.CashOpeningBalanceSqlMapper;
import lt.gama.model.mappers.CashOperationSqlMapper;
import lt.gama.model.mappers.CashRateInfluenceSqlMapper;
import lt.gama.model.mappers.CashSqlMapper;
import lt.gama.model.sql.base.BaseDocumentSql_;
import lt.gama.model.sql.documents.CashOpeningBalanceSql;
import lt.gama.model.sql.documents.CashOperationSql;
import lt.gama.model.sql.documents.CashOperationSql_;
import lt.gama.model.sql.documents.CashRateInfluenceSql;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.sql.entities.CashSql_;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class CashApiImpl implements CashApi {

	private final DBServiceSQL dbServiceSQL;
	private final CashService cashService;
	private final DocumentService documentService;
	private final MoneyAccountService moneyAccountService;
	private final CashSqlMapper cashSqlMapper;
	private final CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper;
	private final CashOperationSqlMapper cashOperationSqlMapper;
	private final CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;
	private final APIResultService apiResultService;


	public CashApiImpl(DBServiceSQL dbServiceSQL, CashService cashService, DocumentService documentService, MoneyAccountService moneyAccountService, CashSqlMapper cashSqlMapper, CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper, CashOperationSqlMapper cashOperationSqlMapper, CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.cashService = cashService;
        this.documentService = documentService;
        this.moneyAccountService = moneyAccountService;
        this.cashSqlMapper = cashSqlMapper;
        this.cashOpeningBalanceSqlMapper = cashOpeningBalanceSqlMapper;
        this.cashOperationSqlMapper = cashOperationSqlMapper;
        this.cashRateInfluenceSqlMapper = cashRateInfluenceSqlMapper;
        this.apiResultService = apiResultService;
    }

    /*
     *  Cash
     */

	@Override
	public APIResult<PageResponse<CashDto, Void>> listCash(PageRequest request) throws GamaApiException {
		return apiResultService.result(() ->
				dbServiceSQL.list(request, CashSql.class, null, cashSqlMapper,
					(cb, root) -> {
						if (StringHelper.hasValue(request.getFilter())) {
							String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
							return cb.or(
									cb.like(cb.lower(cb.function("unaccent", String.class, root.get(CashSql_.NAME))), "%" + filter + "%"),
									cb.like(cb.lower(cb.function("unaccent", String.class, root.get(CashSql_.CASHIER))), "%" + filter + "%")
							);
						}
						return null;
					},
					(cb, root) -> Arrays.asList(cb.asc(root.get(CashSql_.NAME)), cb.asc(root.get(CashSql_.ID))),
					(cb, root) -> Arrays.asList(root.get(CashSql_.NAME), root.get(CashSql_.ID).alias("id"))));
	}

	@Override
	public APIResult<CashDto> saveCash(final CashDto request) throws GamaApiException {
		return apiResultService.result(() -> cashService.saveCash(request));
	}

    @Override
    public APIResult<CashDto> getCash(IdRequest request) throws GamaApiException {
		return apiResultService.result(() ->
				cashSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(CashSql.class, request.getId(), request.getDb())));
    }

    @Override
    public APIResult<Void> deleteCash(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> dbServiceSQL.deleteById(CashSql.class, request.getId()));
    }

    @Override
    public APIResult<CashDto> undeleteCash(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashSqlMapper.toDto(dbServiceSQL.undeleteById(CashSql.class, request.getId())));
    }

    /*
     *  Cash order
     */

	@Override
	public APIResult<PageResponse<CashOperationDto, Void>> listCashOrder(PageRequest request) throws GamaApiException {
		return apiResultService.result(() -> 
				dbServiceSQL.list(request, CashOperationSql.class,
					CashOperationSql.GRAPH_ALL, cashOperationSqlMapper,
					root -> {
						root.join(BaseDocumentSql_.COUNTERPARTY, JoinType.LEFT);
						root.join(BaseDocumentSql_.EMPLOYEE, JoinType.LEFT);
						root.join(CashOperationSql_.CASH, JoinType.LEFT);
					},
					(cb, root) -> {
						Predicate where = EntityUtils.whereDoc(request, cb, root, patterns -> {
							Predicate[] predicates = new Predicate[patterns.length];
							for (int i = 0; i < patterns.length; i++) {
								predicates[i] = cb.like(cb.lower(cb.function("unaccent", String.class,
										root.get(CashOperationSql_.CASH).get(CashSql_.NAME))), patterns[i]);
							}
							return patterns.length == 1 ? predicates[0] : cb.and(predicates);
						});
						Long cashId = (Long) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.CASH_ID);
						if (cashId != null) {
							Predicate predicate = cb.equal(root.get(CashOperationSql_.CASH).get(CashSql_.ID), cashId);
							where = where == null ? predicate : cb.and(where, predicate);
						}
						String sumType = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.SUM_TYPE);
						if (sumType != null) {
							Predicate predicate = sumType.equals("+")
									? cb.greaterThanOrEqualTo(root.get(CashOperationSql_.AMOUNT).get("amount"), 0)
									: cb.lessThan(root.get(CashOperationSql_.AMOUNT).get("amount"), 0);
							where = where == null ? predicate : cb.and(where, predicate);
						}
						return where;
					},
					(cb, root) -> EntityUtils.orderMoneyDoc(request.getOrder(), cb, root),
					(cb, root) -> EntityUtils.selectIdMoneyDoc(request.getOrder(), cb, root)));
	}

	@Override
	public APIResult<CashOperationDto> saveCashOrder(CashOperationDto request) throws GamaApiException {
		return apiResultService.result(() -> cashService.saveCashOperation(request));
	}

    @Override
	public APIResult<CashOperationDto> getCashOrder(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> documentService.getDocument(CashOperationSql.class, request.getId(), request.getDb()));
	}

	@Override
	public APIResult<CashOperationDto> finishCashOrder(FinishRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.finishCashOperation(request.id, request.finishGL));
	}

	@Override
	public APIResult<Void> deleteCashOrder(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
	}

	@Override
	public APIResult<CashOperationDto> recallCashOrder(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.recallCashOperation(request.getId()));
	}

	/*
     *  Cash opening balance
     */

	@Override
	public APIResult<PageResponse<CashOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
		return apiResultService.result(() ->
				dbServiceSQL.list(request, CashOpeningBalanceSql.class,
					CashOpeningBalanceSql.GRAPH_ALL, cashOpeningBalanceSqlMapper,
					(cb, root) -> EntityUtils.whereDoc(request, cb, root, null),
					(cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
					(cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
	}

	@Override
	public APIResult<CashOpeningBalanceDto> saveOpeningBalance(CashOpeningBalanceDto request) throws GamaApiException {
		return apiResultService.result(() -> cashService.saveCashOpeningBalance(request));
	}

    @Override
    public APIResult<CashOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
		return apiResultService.result(() ->
				documentService.getDocument(CashOpeningBalanceSql.class, request.getId(), request.getDb()));
	}

	@Override
	public APIResult<CashOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.finishCashOpeningBalance(request.getId()));
	}

	@Override
	public APIResult<CashOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.importOpeningBalance(request.getId(), request.getFileName()));
	}

	@Override
	public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
	}

	/*
	 *  Cash $$$ Rate Influence
	 */

    @Override
    public APIResult<PageResponse<CashRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException {
		return apiResultService.result(() ->
				dbServiceSQL.list(request, CashRateInfluenceSql.class,
					CashRateInfluenceSql.GRAPH_ALL, cashRateInfluenceSqlMapper,
					(cb, root) -> EntityUtils.whereDoc(request, cb, root, null),
					(cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
					(cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<CashRateInfluenceDto> saveRateInfluence(CashRateInfluenceDto request) throws GamaApiException {
		return apiResultService.result(() -> cashService.saveCashRateInfluence(request));
    }

    @Override
    public APIResult<CashRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> 
				documentService.getDocument(CashRateInfluenceSql.class, request.getId(), request.getDb()));
	}

	@Override
	public APIResult<CashRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.finishCashRateInfluence(request.id, request.finishGL));
	}

    @Override
    public APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<CashRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.recallCashRateInfluence(request.getId()));
    }

	@Override
	public APIResult<List<RepMoneyBalance<CashDto>>> genRateInfluence(DateRequest request) throws GamaApiException {
		return apiResultService.result(() -> moneyAccountService.genRateInfluence(request.getDate(), AccountType.CASH));
	}

	/*
     * Reports
     */

	@Override
	public APIResult<List<RepMoneyBalance<CashDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.reportCashBalance(request));
	}

	@Override
	public APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<CashDto>>> reportFlow(PageRequest request) throws GamaApiException {
		return apiResultService.result(() -> cashService.reportCashFlow(request));
	}

	@Override
	public APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException {
		return apiResultService.result(() -> moneyAccountService.reportBalanceInterval(request, AccountType.CASH));
	}
}
