package vn.com.twendie.avis.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.ValidUtils;
import vn.com.twendie.avis.api.model.payload.FeedbackPayload;
import vn.com.twendie.avis.api.repository.FeedbackRepo;
import vn.com.twendie.avis.api.service.FeedbackService;
import vn.com.twendie.avis.data.model.Feedback;
import vn.com.twendie.avis.data.model.User;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepo feedbackRepo;

    public FeedbackServiceImpl(FeedbackRepo feedbackRepo) {
        this.feedbackRepo = feedbackRepo;
    }

    @Override
    public Page<Feedback> findAll(int page, int size) {
        int pageOffSet = page - 1;
        return feedbackRepo.findByDeletedFalseOrderByCreatedAtDesc(
                PageRequest.of(Math.max(pageOffSet, 0), size));
    }

    @Override
    public Page<Feedback> findMyFeedback(Long userId, int page, int size) {
        int pageOffSet = page - 1;
        return feedbackRepo.findByCreatedByIdAndDeletedFalseOrderByCreatedAtDesc(userId,
                PageRequest.of(Math.max(pageOffSet, 0), size));
    }

    @Override
    public Feedback createFeedback(FeedbackPayload payload, User user) {
        return feedbackRepo.save(Feedback.builder()
                .content(ValidUtils.normalizeString(payload.getContent()))
                .createdBy(user)
                .build());
    }
}
