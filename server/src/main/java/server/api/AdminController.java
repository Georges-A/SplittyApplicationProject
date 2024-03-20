package server.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private String password;

    public AdminController() {
        password = UUID.randomUUID().toString().substring(0, 8);
    }

    @GetMapping(path = {"", "/"})
    public ResponseEntity<String> generatePassword() {
        System.out.println("Password is: " + password);
        return ResponseEntity.ok(password);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody String password) {
        if (password.equals(this.password)) {
            return ResponseEntity.ok(password);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Your password is invalid.");
    }
}
