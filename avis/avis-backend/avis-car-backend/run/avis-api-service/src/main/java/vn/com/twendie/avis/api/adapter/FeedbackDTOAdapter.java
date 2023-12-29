package vn.com.twendie.avis.api.adapter;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.response.FeedbackDTO;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.Feedback;
import vn.com.twendie.avis.data.model.MemberCustomer;

import java.util.Objects;
import java.util.function.Function;

@Component
public class FeedbackDTOAdapter implements Function<Feedback, FeedbackDTO> {

    @Override
    public FeedbackDTO apply(Feedback feedback) {
        MemberCustomer memberCustomer = feedback.getCreatedBy().getMemberCustomer();
        Customer customer = feedback.getCreatedBy().getCustomer();
        return FeedbackDTO.builder()
                .id(feedback.getId())
                .adminName(Objects.isNull(memberCustomer) ? "" : memberCustomer.getName())
                .customerName(customer.getName())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .createdBy(feedback.getCreatedBy().getName())
                .build();
    }
}
