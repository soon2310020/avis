package saleson.api.tabTable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.payload.PartPayload;
import saleson.api.tabTable.payload.SaveTabTablePayload;
import saleson.api.tabTable.payload.UpdateTabTablePayload;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.user.payload.UserParam;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/tab-table")
public class TabTableController {

    @Autowired
    private TabTableService tabTableService;

    @GetMapping(value = "by-current-user")
    public ApiResponse getTabTableByCurrentUser(@RequestParam(value = "objectType") ObjectType objectType) {
        return tabTableService.getAllTabByCurrentUser(objectType);
    }

    @PostMapping(value = "save-tab-table-mold")
    public ApiResponse saveTabTableMold(MoldPayload moldPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableMold(moldPayload, payload);
    }

    @PutMapping
    public ApiResponse updateTabTable(@RequestBody List<UpdateTabTablePayload> updateTabTablePayloadList) {
        return tabTableService.updateTabTable(updateTabTablePayloadList);
    }

    @PostMapping(value = "save-tab-table-part")
    public ApiResponse saveTabTablePart(PartPayload partPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTablePart(partPayload, payload);
    }

    @PostMapping(value = "save-tab-table-location")
    public ApiResponse saveTabTableLocation(LocationPayload locationPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableLocation(locationPayload, payload);
    }

    @PostMapping(value = "save-tab-table-company")
    public ApiResponse saveTabTableCompany(CompanyPayload companyPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableCompany(companyPayload, payload);
    }

    @PostMapping(value = "save-tab-table-machine")
    public ApiResponse saveTabTableMachine(MachinePayload machinePayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableMachine(machinePayload, payload);
    }

    @PostMapping(value = "save-tab-table-user")
    public ApiResponse saveTabTableUser(UserParam userParam, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableUser(userParam, payload);
    }

    @PostMapping(value = "save-tab-table-terminal")
    public ApiResponse saveTabTableTerminal(TerminalPayload terminalPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableTerminal(terminalPayload, payload);
    }

    @PostMapping(value = "save-tab-table-counter")
    public ApiResponse saveTabTableCounter(CounterPayload counterPayload, @RequestBody SaveTabTablePayload payload) {
        return tabTableService.saveTabTableCounter(counterPayload, payload);
    }
}
