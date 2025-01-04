package financialmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContentController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String getSignupPage(){
        return "signup";
    }

    @GetMapping("/addBank")
    public String getAddBankPage(){
        return "addBank";
    }
}
