package com.stg.service3rd.mb_card;

import com.stg.service3rd.mb_card.adapter.MbCardApiCaller;
import com.stg.service3rd.mb_card.adapter.MbCardFunctions;
import com.stg.service3rd.mb_card.dto.req.CardNumbToCardIDReq;
import com.stg.service3rd.mb_card.dto.resp.CardNumbToCardIDResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MbCardApi3rdServiceImpl implements MbCardApi3rdService {
    private final MbCardApiCaller mbCardApiCaller;

    @Override
    public CardNumbToCardIDResp convertCardNumbToCardID(CardNumbToCardIDReq request) throws Exception {
        return mbCardApiCaller.post(MbCardFunctions.CONVERT_CARD_NUMB_TO_CARD_ID, mbCardApiCaller.getAuthHeaderHmac(request), request, CardNumbToCardIDResp.class);
    }
}
