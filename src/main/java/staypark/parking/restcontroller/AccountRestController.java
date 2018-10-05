package staypark.parking.restcontroller;

import staypark.parking.handler.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountRestController {

    @Autowired
    private AccountHandler accountHandler;

    @RequestMapping("/account/username/{username}/check")
    public boolean checkUsername(@PathVariable("username") String username, @RequestHeader HttpHeaders headers) {
        return accountHandler.checkUsernameExists(username);
    }

    @RequestMapping("/account/email/{email}/check")
    public boolean checkEmail(@PathVariable("email") String email, @RequestHeader HttpHeaders headers) {
        return accountHandler.checkEmailExists(email);
    }
}
