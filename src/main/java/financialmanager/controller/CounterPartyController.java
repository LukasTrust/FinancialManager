package financialmanager.controller;

import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/counterParty/data")
public class CounterPartyController {

    private final CounterPartyService counterPartyService;

    @GetMapping("")
    public ResponseEntity<?> getCounterPartyDisplays() {
        return counterPartyService.getCounterPartyDisplays();
    }

    @PostMapping("/{counterPartyId}/change/name/{newValue}")
    public ResponseEntity<?> updateCounterPartyName(@PathVariable Long counterPartyId,
                                                    @PathVariable String newValue) {
        return counterPartyService.updateCounterPartyField(counterPartyId, newValue, CounterParty::setName);
    }

    @PostMapping("/{counterPartyId}/change/description/{newValue}")
    public ResponseEntity<?> updateCounterPartyDescription(@PathVariable Long counterPartyId,
                                                           @PathVariable String newValue) {
        return counterPartyService.updateCounterPartyField(counterPartyId, newValue, CounterParty::setDescription);
    }

    @PostMapping("/{counterPartyId}/removeSearchString")
    public ResponseEntity<?> removeSearchStringFromCounterParty(
            @PathVariable("counterPartyId") Long counterPartyId,
            @RequestParam(value = "searchString") String searchString) {
        return counterPartyService.removeSearchStringFromCounterParty(counterPartyId, searchString);
    }

    @PostMapping("/hideCounterParties")
    public ResponseEntity<?> hideCounterParties(@RequestBody List<Long> counterPartyIds) {
        return counterPartyService.updateCounterPartyVisibility(counterPartyIds, true);
    }

    @PostMapping("/unHideCounterParties")
    public ResponseEntity<?> unHideCounterParties(@RequestBody List<Long> counterPartyIds) {
        return counterPartyService.updateCounterPartyVisibility(counterPartyIds, false);
    }

}
