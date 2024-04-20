package lt.gama.impexp.link;

import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.InventoryOpeningBalanceDto;
import lt.gama.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2015-12-16.
 */
public class LinkInventoryOpeningBalanceDto implements LinkBase<InventoryOpeningBalanceDto>  {

    @Autowired
    private TradeService tradeService;


    @Override
    public InventoryOpeningBalanceDto resolve(InventoryOpeningBalanceDto document) {
        return null;
    }

    @Override
    public void finish(long documentId) {
        tradeService.runOpeningBalanceTask(documentId);
    }
}
