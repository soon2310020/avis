package com.stg.service.impl.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.stg.config.security.auth.AccessToken;
import com.stg.config.security.auth.JwtAuthenticationProvider;
import com.stg.config.security.auth.JwtClaims;
import com.stg.entity.RefreshToken;
import com.stg.entity.sale.BranchInformation;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.sale.BranchInformationRepository;
import com.stg.service.BackofficeService;
import com.stg.service.CrmAuthenticationService;
import com.stg.service.caching.RmInfoCaching;
import com.stg.service.dto.quotation.crm.RmInfoRespDto;
import com.stg.service3rd.crm.CrmApi3rdService;
import com.stg.service3rd.crm.dto.resp.RmUserDetailDto;
import com.stg.service3rd.crm.dto.resp.RmUserDetailResp;
import com.stg.service3rd.crm.dto.resp.RmUserInfo;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.dto.resp.ICDataResp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmAuthenticationServiceImpl implements CrmAuthenticationService {
    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final RefreshTokenService refreshTokenService;
    private final CrmApi3rdService crmApi3rdService;
    private final MbalApi3rdService mbalApi3rdService;
    private final BackofficeService backofficeService;
    private final BranchInformationRepository branchInformationRepository;
    private final RmInfoCaching rmInfoCaching;

    @Override
    public AccessToken verifyToken(String token) {
//        // todo: comment when => go-live
//        AccessToken accessTokenTest = verifyTestToken(token);
//        if (accessTokenTest != null)  {
//            cacheRmInfo(accessTokenTest);
//            return accessTokenTest;
//        }

        try {
            log.debug("Get user token:" + token);
            RmUserInfo crmUser = crmApi3rdService.getUserInfo(token);
            String username = crmUser.getPreferred_username();

            // get rm code, info
            log.info("Get rm info by username: " + username);
            RmUserDetailResp rmUserDetailResp = crmApi3rdService.getCrmDataByUsername(username, token);
            String rmCode = rmUserDetailResp.getHrsCode();
            String branchCode = rmUserDetailResp.getBranch();
            String branchName = rmUserDetailResp.getBranchName();
            String fullName = rmUserDetailResp.getFullName();
            String phoneNumber = rmUserDetailResp.getPhoneNumber();
            String email = rmUserDetailResp.getEmail();

            // get ic info
            log.info("Get ic info by rm code: " + rmCode);
            ICDataResp icInfoResp = mbalApi3rdService.getIcByRmCode(rmCode);
            String icCode = icInfoResp != null ? icInfoResp.getCode() : null;

            if (branchName == null) {
                branchName = icInfoResp != null ? icInfoResp.getBranchName() : detectBranchName(rmCode, branchCode);
            }

            User user = new User(username, "", List.of());
            JwtClaims claims = new JwtClaims();
            claims.setRmCode(rmCode);
            claims.setIcCode(icCode);
            claims.setBranchCode(branchCode);
            claims.setBranchName(branchName);
            claims.setFullName(fullName);
            claims.setPhoneNumber(phoneNumber);
            claims.setEmail(email);

            AccessToken accessToken = jwtAuthenticationProvider
                    .getToken(new UsernamePasswordAuthenticationToken(user, null, null), claims);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), rmCode, icCode,
                    branchCode, branchName, fullName, phoneNumber, email);

            accessToken.setRefreshToken(refreshToken.getToken());
            accessToken.setIcInfo(icInfoResp);
            RmUserDetailDto rmUserDetailDto = rmUserDetailResp.toDto();
            rmUserDetailDto.setBranchName(branchName);
            accessToken.setRmInfo(rmUserDetailDto);

            cacheRmInfo(accessToken);

            return accessToken;
        } catch (Exception e) {
            log.error("Get user info failed", e);
            throw new ApplicationException("", new ErrorDto(HttpStatus.UNAUTHORIZED, "Truy cập không thành công!"));
        }
    }

    private void cacheRmInfo(AccessToken accessToken) {
        RmInfoCaching.RmCachingData rmInfo = new RmInfoCaching.RmCachingData();
        rmInfo.setUsername(accessToken.getUsername());
        rmInfo.setRmName(accessToken.getRmInfo().getFullName());
        rmInfo.setRmCode(accessToken.getRmInfo().getCode());
        rmInfo.setRmPhoneNumber(accessToken.getRmInfo().getPhoneNumber());
        rmInfo.setRmEmail(accessToken.getRmInfo().getEmail());
        rmInfo.setRmBranchCode(accessToken.getRmInfo().getBranchCode());
        rmInfo.setRmBranchName(accessToken.getRmInfo().getBranchName());

        if (accessToken.getIcInfo() != null) {
            rmInfo.setIcName(accessToken.getIcInfo().getName());
            rmInfo.setIcCode(accessToken.getIcInfo().getCode());
            rmInfo.setIcBranchCode(accessToken.getIcInfo().getBranchCode());
            rmInfo.setIcBranchName(accessToken.getIcInfo().getBranchName());
            rmInfoCaching.setData(accessToken.getIcInfo().getCode(), rmInfo);
        }

        rmInfoCaching.setData(accessToken.getUsername(), rmInfo);
        rmInfoCaching.setData(accessToken.getRmInfo().getCode(), rmInfo);
    }

    private String detectBranchName(String rmCode, String branchCode) {
        String branchName = null;

        BranchInformation branchInformation = branchInformationRepository.findByCode(branchCode).orElse(null);
        if (branchInformation != null) {
            branchName = branchInformation.getName();
        }

        if (branchName == null) {
            try {
                RmInfoRespDto rmInfo = backofficeService.searchCrm(rmCode, "RM");
                branchName = rmInfo.getT24Employee().getBranchName();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        return branchName;
    }

    /** for test */
    private AccessToken verifyTestToken(String token) {
        if ("x11234".equals(token) || "x-11234".equals(token) || "q-11234".equals(token)) {
            String username = "thuypt4";
            String rmCode = "MB00020056";
            String branchCode = "VN0010052";
            String branchName = "CN Lý Nam Đế";
            String fullName = "Vo Phi Long IC test";
            String phoneNumber = "0365721666";
            String email = "2210019364@mbbank.vn";
            String icCode = null;

            if (!"q-11234".equals(token)) {
                icCode = "2210019364";
            }

            JwtClaims claims = new JwtClaims();
            claims.setRmCode(rmCode);
            claims.setIcCode(icCode);
            claims.setBranchCode(branchCode);
            claims.setBranchName(branchName);
            claims.setFullName(fullName);
            claims.setPhoneNumber(phoneNumber);
            claims.setEmail(email);

            User user = new User(username, "", List.of());

            AccessToken accessToken = jwtAuthenticationProvider
                    .getToken(new UsernamePasswordAuthenticationToken(user, null, null), claims);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), rmCode, icCode,
                    branchCode, branchName, fullName, phoneNumber, email);

            accessToken.setRefreshToken(refreshToken.getToken());

            if (!"q-11234".equals(token)) {
                accessToken.setIcInfo(new ICDataResp(icCode, fullName, phoneNumber, email, branchCode, branchName));
            }
            accessToken.setRmInfo(new RmUserDetailDto(rmCode, username, fullName, branchCode, branchName, phoneNumber, email, true));

            return accessToken;
        }

        if ("rf-1234".equals(token)) {
            String username = "thuypt4";
            String rmCode = "MB00009593";
            String branchCode = "VN0010765";
            String branchName = "CN Bình Thuận";
            String fullName = "TEST RM 1";
            String phoneNumber = "0365721664";
            String email = "2210000919@gmail.com";
            String icCode = "2210000919";

            JwtClaims claims = new JwtClaims();
            claims.setRmCode(rmCode);
            claims.setIcCode(icCode);
            claims.setBranchCode(branchCode);
            claims.setBranchName(branchName);
            claims.setFullName(fullName);
            claims.setPhoneNumber(phoneNumber);
            claims.setEmail(email);

            User user = new User(username, "", List.of());

            AccessToken accessToken = jwtAuthenticationProvider
                    .getToken(new UsernamePasswordAuthenticationToken(user, null, null), claims);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), rmCode, icCode,
                    branchCode, branchName, fullName, phoneNumber, email);

            accessToken.setRefreshToken(refreshToken.getToken());

            accessToken.setIcInfo(new ICDataResp(icCode, fullName, phoneNumber, email, branchCode, branchName));
            accessToken.setRmInfo(new RmUserDetailDto(rmCode, username, fullName, branchCode, branchName, phoneNumber, email, true));

            return accessToken;
        }

        if ("q-linhvt".equals(token)) {
            String username = "linhvt10";
            String rmCode = "MB00098765";
            String branchCode = "VN0010011";
            String branchName = "CN Mỹ Đình";
            String fullName = "Vũ Thùy Linh";
            String phoneNumber = "0911275677";
            String email = "linhvt10@mbbank.com.vn";
            String icCode = "2210018765";

            JwtClaims claims = new JwtClaims();
            claims.setRmCode(rmCode);
            claims.setIcCode(icCode);
            claims.setBranchCode(branchCode);
            claims.setBranchName(branchName);
            claims.setFullName(fullName);
            claims.setPhoneNumber(phoneNumber);
            claims.setEmail(email);

            User user = new User(username, "", List.of());

            AccessToken accessToken = jwtAuthenticationProvider
                    .getToken(new UsernamePasswordAuthenticationToken(user, null, null), claims);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername(), rmCode, icCode,
                    branchCode, branchName, fullName, phoneNumber, email);

            accessToken.setRefreshToken(refreshToken.getToken());

            accessToken.setIcInfo(new ICDataResp(icCode, fullName, phoneNumber, email, branchCode, branchName));
            accessToken.setRmInfo(new RmUserDetailDto(rmCode, username, fullName, branchCode, branchName, phoneNumber, email, true));

            return accessToken;
        }
        return null;
    }
}
