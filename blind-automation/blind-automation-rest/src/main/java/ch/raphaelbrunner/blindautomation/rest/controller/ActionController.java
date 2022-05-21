package ch.raphaelbrunner.blindautomation.rest.controller;

import ch.raphaelbrunner.blindautomation.action.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ActionController {

    private static final Logger LOG = LoggerFactory.getLogger(ActionController.class);

    @GetMapping("blindautomation/action")
    public ResponseEntity<?> executeAction(@RequestParam final UUID blindId, @RequestParam final ActionType action,
                                           @RequestParam final float value) {
        LOG.debug("Received action request with blindId={}, action={}, value={}", blindId, action, value);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
