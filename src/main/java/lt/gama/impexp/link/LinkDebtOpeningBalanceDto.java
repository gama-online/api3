package lt.gama.impexp.link;

import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.DebtOpeningBalanceDto;
import lt.gama.model.mappers.DebtOpeningBalanceSqlMapper;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2015-12-16.
 */
public class LinkDebtOpeningBalanceDto implements LinkBase<DebtOpeningBalanceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper;

    @Autowired
    private DebtService debtService;


    @Override
    public DebtOpeningBalanceDto resolve(DebtOpeningBalanceDto document) {
        return document;
    }

    @Override
    public void finish(long documentId) {
        debtService.runOpeningBalanceTask(documentId);
    }
}
