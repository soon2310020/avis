package com.emoldino.api.production.resource.composite.alrcyctim;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetOut;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Production / Alert / Cycle Time")
@RequestMapping("/api/production/alr-cyc-tim")
public interface AlrCycTimController {
    public static final String NAME = "CycleTime";
    public static final String NAME_PLURAL = "CycleTimes";

    @ApiOperation("Get " + NAME_PLURAL)
    @GetMapping
    AlrCycTimGetOut get(AlrCycTimGetIn input, Pageable pageable);

    @ApiOperation("Post " + NAME_PLURAL + " Note Batch")
    @PostMapping("/note-batch")
    SuccessOut postNoteBatch(AlrCycTimGetIn input, BatchIn batchin, @RequestBody NoteIn body);
}
