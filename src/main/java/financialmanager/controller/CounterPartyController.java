package financialmanager.controller;

import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.Response;
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
    public ResponseEntity<Response> updateCounterPartyName(@PathVariable Long counterPartyId,
                                                    @PathVariable String newValue) {
        return counterPartyService.updateCounterPartyField(counterPartyId, newValue, CounterParty::setName);
    }

    @PostMapping("/{counterPartyId}/change/description/{newValue}")
    public ResponseEntity<Response> updateCounterPartyDescription(@PathVariable Long counterPartyId,
                                                           @PathVariable String newValue) {
        return counterPartyService.updateCounterPartyField(counterPartyId, newValue, CounterParty::setDescription);
    }

    @PostMapping("/hideCounterParties")
    public ResponseEntity<Response> hideCounterParties(@RequestBody List<Long> counterPartyIds) {
        return counterPartyService.updateCounterPartyVisibility(counterPartyIds, true);
    }

    @PostMapping("/unHideCounterParties")
    public ResponseEntity<Response> unHideCounterParties(@RequestBody List<Long> counterPartyIds) {
        return counterPartyService.updateCounterPartyVisibility(counterPartyIds, false);
    }

    @PostMapping("/{counterPartyId}/removeSearchString")
    public ResponseEntity<Response> removeSearchStringFromCounterParty(
            @PathVariable("counterPartyId") Long counterPartyId,
            @RequestParam(value = "searchString") String searchString) {
        return counterPartyService.removeSearchStringFromCounterParty(counterPartyId, searchString);
    }

    @PostMapping("/{counterPartyId}/addSearchString")
    public ResponseEntity<Response> addSearchStringToCounterParty(
            @PathVariable("counterPartyId") Long counterPartyId,
            @RequestParam(value = "searchString") String searchString) {
        return counterPartyService.addSearchStringToCounterParty(counterPartyId, searchString);
    }

    @PostMapping("/mergeCounterParties/{headerId}")
    public ResponseEntity<Response> mergeCounterParties(@PathVariable Long headerId, @RequestBody List<Long> counterPartyIds) {
        return counterPartyService.mergeCounterParties(headerId, counterPartyIds);
    }
}
