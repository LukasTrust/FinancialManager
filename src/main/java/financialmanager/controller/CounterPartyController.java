package financialmanager.controller;

import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/counterParties/data")
public class CounterPartyController {

    private final CounterPartyService counterPartyService;

    @GetMapping("")
    public ResponseEntity<?> getCounterPartyDisplays() {
        return counterPartyService.getCounterPartyDisplays();
    }

    @PostMapping("/{counterPartyId}/change/name/{newValue}")
    public ResponseEntity<Response> updateCounterPartyName(@PathVariable Long counterPartyId,
                                                           @RequestBody Map<String, String> requestBody) {
        return counterPartyService.updateCounterPartyField(counterPartyId, requestBody, CounterParty::setName);
    }

    @PostMapping("/{counterPartyId}/change/description/{newValue}")
    public ResponseEntity<Response> updateCounterPartyDescription(@PathVariable Long counterPartyId,
                                                                  @RequestBody Map<String, String> requestBody) {
        return counterPartyService.updateCounterPartyField(counterPartyId, requestBody, CounterParty::setDescription);
    }

    @PostMapping("/hide")
    public ResponseEntity<Response> hideCounterParties(@RequestBody List<Long> counterPartyIds) {
        return counterPartyService.updateCounterPartyVisibility(counterPartyIds, true);
    }

    @PostMapping("/unHide")
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

    @PostMapping("/merge/{headerId}")
    public ResponseEntity<Response> mergeCounterParties(@PathVariable Long headerId, @RequestBody List<Long> counterPartyIds) {
        return counterPartyService.mergeCounterParties(headerId, counterPartyIds);
    }
}
