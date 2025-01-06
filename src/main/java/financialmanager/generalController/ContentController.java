package financialmanager.generalController;

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
        return "index";  // Return the layout template
    }

    @GetMapping("/addBankAccount")
    public String getAddBankPage(Model model) {
        model.addAttribute("title", "Add Bank Account");
        return "addBankAccount";  // Only return the content fragment for this page
    }

    @GetMapping("/bankAccountOverview")
    public String getBankAccountOverview(Model model) {
        model.addAttribute("title", "Bank Account Overview");
        return "bankAccountOverview";  // Only return the content fragment for this page
    }
}
