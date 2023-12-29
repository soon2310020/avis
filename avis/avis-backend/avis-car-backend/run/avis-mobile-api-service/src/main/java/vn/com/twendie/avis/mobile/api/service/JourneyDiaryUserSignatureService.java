package vn.com.twendie.avis.mobile.api.service;

import org.springframework.data.domain.Page;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiarySignatureDTO;
import vn.com.twendie.avis.mobile.api.model.response.MemberCustomerDTO;

import java.util.List;

public interface JourneyDiaryUserSignatureService {

    List<MemberCustomerDTO> suggestionMember(User user, long contractId, String name);

    Page<JourneyDiarySignatureDTO> getJourneyDiarySignature(Long fromDate, Long toDate, User user, int page, int size);

    JourneyDiarySignatureDTO getById(Long id);
}
