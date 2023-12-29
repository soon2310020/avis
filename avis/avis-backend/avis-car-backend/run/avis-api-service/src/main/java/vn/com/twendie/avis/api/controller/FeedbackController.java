package vn.com.twendie.avis.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.adapter.FeedbackDTOAdapter;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.payload.FeedbackPayload;
import vn.com.twendie.avis.api.model.response.FeedbackDTO;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.FeedbackService;
import vn.com.twendie.avis.data.model.Feedback;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ListUtils listUtils;
    private final FeedbackDTOAdapter feedbackDTOAdapter;

    public FeedbackController(FeedbackService feedbackService,
                              ListUtils listUtils,
                              FeedbackDTOAdapter feedbackDTOAdapter) {
        this.feedbackService = feedbackService;
        this.listUtils = listUtils;
        this.feedbackDTOAdapter = feedbackDTOAdapter;
    }

    @PostMapping("/create")
    @Transactional
    @RequirePermission(acceptedRoles = "Customer")
    public ApiResponse<?> createFeedback(
            @Valid @RequestBody FeedbackPayload payload,
            @CurrentUser UserDetails userDetails
    ) {
        User user = ((UserPrincipal) userDetails).getUser();
        Feedback feedback = feedbackService.createFeedback(payload, user);
        return ApiResponse.success(feedbackDTOAdapter.apply(feedback));
    }

    @GetMapping("")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> getAllFeedback(
            @RequestParam(name = "page", defaultValue = AvisApiConstant.DEFAULT_STARTER_PAGE_STRING) int page,
            @RequestParam(name = "page_size", defaultValue = AvisApiConstant.DEFAULT_PAGE_SIZE_STRING) int size
    ) {
        Page<Feedback> feedbackPage = feedbackService.findAll(page, size);
        Page<FeedbackDTO> feedbackDTOS = new PageImpl<>(listUtils.transform(feedbackPage.getContent(), feedbackDTOAdapter)
                , feedbackPage.getPageable(), feedbackPage.getTotalElements());
        return ApiResponse.success(GeneralPageResponse.toResponse(feedbackDTOS));
    }

    @GetMapping("/mine")
    @RequirePermission(acceptedRoles = {"Customer"})
    public ApiResponse<?> getMyFeedback(
            @RequestParam(name = "page", defaultValue = AvisApiConstant.DEFAULT_STARTER_PAGE_STRING) int page,
            @RequestParam(name = "page_size", defaultValue = AvisApiConstant.DEFAULT_PAGE_SIZE_STRING) int size,
            @CurrentUser UserDetails userDetails
    ) {
        User user = ((UserPrincipal) userDetails).getUser();
        Page<Feedback> feedbackPage = feedbackService.findMyFeedback(user.getId(), page, size);
        Page<FeedbackDTO> feedbackDTOS = new PageImpl<>(listUtils.transform(feedbackPage.getContent(), feedbackDTOAdapter)
                , feedbackPage.getPageable(), feedbackPage.getTotalElements());
        return ApiResponse.success(GeneralPageResponse.toResponse(feedbackDTOS));
    }

}
