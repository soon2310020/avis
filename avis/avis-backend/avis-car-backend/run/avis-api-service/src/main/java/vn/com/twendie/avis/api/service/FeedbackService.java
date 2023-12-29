package vn.com.twendie.avis.api.service;

import org.springframework.data.domain.Page;
import vn.com.twendie.avis.api.model.payload.FeedbackPayload;
import vn.com.twendie.avis.data.model.Feedback;
import vn.com.twendie.avis.data.model.User;

public interface FeedbackService {

    Page<Feedback> findAll(int page, int size);

    Page<Feedback> findMyFeedback(Long userId, int page, int size);

    Feedback createFeedback(FeedbackPayload payload, User user);

}
