package staypark.parking.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import staypark.parking.handler.HealthHandler;

@RestController
public class HealthController {
	
	@Autowired
	private HealthHandler healthHandler;

    @RequestMapping("/heartbeat")
    public String heartbeat() {
        return "Thump!";
    }
    
    @RequestMapping("/deep-heartbeat")
    public ResponseEntity<String> deepbeat() {
       if (healthHandler.dbProbe()) {
    	   return ResponseEntity.ok("Thump!");
       } else {
    	   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
       }
    }
}
