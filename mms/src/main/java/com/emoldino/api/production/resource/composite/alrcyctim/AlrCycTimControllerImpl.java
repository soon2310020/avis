package com.emoldino.api.production.resource.composite.alrcyctim;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetIn;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetOut;
import com.emoldino.api.production.resource.composite.alrcyctim.service.AlrCycTimService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;
@RestController
public class AlrCycTimControllerImpl implements AlrCycTimController {
    @Override
    public AlrCycTimGetOut get(AlrCycTimGetIn input, Pageable pageable) {
        return BeanUtils.get(AlrCycTimService.class).get(input, pageable);
    }

    @Override
    public SuccessOut postNoteBatch(AlrCycTimGetIn input, BatchIn batchin, NoteIn body) {
        BeanUtils.get(AlrCycTimService.class).postNoteBatch(input, batchin, body);
        return SuccessOut.getDefault();
    }
}
