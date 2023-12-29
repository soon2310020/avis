package com.stg.service.impl;

import com.stg.entity.MbalPackage;
import com.stg.repository.MbalPackageRepository;
import com.stg.service.MbalPackageService;
import com.stg.utils.InsuranceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MbalPackageImpl implements MbalPackageService {
    private final MbalPackageRepository mbalPackageRepository;

    @Override
    public List<MbalPackage> list() {
        List<MbalPackage> mbalPackages = mbalPackageRepository.findAllByActiveIsTrueOrderByIdAsc();
        for (MbalPackage mbalPackage : mbalPackages) {
            mbalPackage.setCategory(InsuranceCategory.HAPPY.name());
            if (mbalPackage.getId() >= 8 && mbalPackage.getId() <= 14 || (mbalPackage.getId() == 20 || mbalPackage.getId() == 21 || mbalPackage.getId() == 22)) {
                mbalPackage.setCategory(InsuranceCategory.HEALTHY.name());
            }
        }
        return mbalPackages;
    }

}
