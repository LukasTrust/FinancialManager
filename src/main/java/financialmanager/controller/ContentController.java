package financialmanager.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContentController {

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignupPage(){
        return "signup";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Add Bank Account");
        model.addAttribute("template", "addBankAccount");
        return "index";
    }

    @GetMapping("/addBankAccount")
    public String getAddBankPage(Model model) {
        model.addAttribute("title", "Add Bank Account");
        return "addBankAccount";
    }

    @GetMapping("/bankAccountOverview")
    public String getBankAccountOverview(Model model) {
        model.addAttribute("title", "Bank Account Overview");
        return "bankAccountOverview";
    }

    @GetMapping("/transactions")
    public String getTransactions(Model model) {
        model.addAttribute("title", "Transactions");
        return "transactions";
    }

    @GetMapping("/changeContract")
    public String getChangeContract(Model model) {
        model.addAttribute("title", "Change Contracts");
        return "changeContract";
    }

    @GetMapping("/counterParties")
    public String getCounterParties(Model model) {
        model.addAttribute("title", "Counter Parties");
        return "counterParties";
    }

    @GetMapping("/contracts")
    public String getContracts(Model model) {
        model.addAttribute("title", "Contracts");
        return "contracts";
    }
}
