package com.stg.service3rd.mb_card;

import com.stg.service3rd.mb_card.dto.req.CardNumbToCardIDReq;
import com.stg.service3rd.mb_card.dto.resp.CardNumbToCardIDResp;

public interface MbCardApi3rdService {
    CardNumbToCardIDResp convertCardNumbToCardID(CardNumbToCardIDReq request) throws Exception;
}
