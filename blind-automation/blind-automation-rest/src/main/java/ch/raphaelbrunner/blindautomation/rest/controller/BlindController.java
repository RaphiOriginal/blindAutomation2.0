package ch.raphaelbrunner.blindautomation.rest.controller;

import ch.raphaelbrunner.blindautomation.common.entity.ListResponse;
import ch.raphaelbrunner.blindautomation.model.Blind;
import ch.raphaelbrunner.blindautomation.model.enums.DeviceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

@RestController
@Transactional
public class BlindController {

    private static final Logger LOG = LoggerFactory.getLogger(BlindController.class);

    private final ObjectMapper mapper;

    private final EntityManager entityManager;

    @Autowired
    public BlindController(final ObjectMapper mapper, final EntityManager entityManager) {
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @GetMapping("/blindautomation/Blind")
    public ResponseEntity<String> allBlinds() {
        try {
            final List<Blind> activeBlinds = fetchAllActiveBlinds();
            final ListResponse<Blind> payload = new ListResponse<>();
            payload.setData(activeBlinds);
            payload.setTotal(activeBlinds.size());
            final String json = mapper.writeValueAsString(payload);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        }
        catch (final JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/blindautomation/Blind/{id}")
    public ResponseEntity<String> blind(@RequestParam final UUID id) {
        try {
            final Blind blind = fetchBlindById(id);
            if (blind != null) {
                final String json = mapper.writeValueAsString(blind);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body(String.format("No Blind with id '%s' found.", id));
            }
        }
        catch (final JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/blindautomation/Blind")
    public ResponseEntity<String> updateBlind(@RequestBody final String body) {
        LOG.debug("Received POST request on /blindautomation/Blind endpoint with body '{}'", body);
        try {
            final Blind toBeUpdated = mapper.readValue(body, Blind.class);
            final Blind existingBlind = fetchBlindById(toBeUpdated.getId());
            if (existingBlind != null) {
                existingBlind.setName(toBeUpdated.getName());
                existingBlind.setDuration(toBeUpdated.getDuration());
                existingBlind.setSunInPosition(toBeUpdated.getSunInPosition());
                existingBlind.setSunOutPosition(toBeUpdated.getSunOutPosition());
                entityManager.persist(existingBlind);
                final String json = mapper.writeValueAsString(existingBlind);
                return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(json);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                    .body(String.format("No Blind with id '%s' found.", toBeUpdated.getId()));
        } catch (final JsonProcessingException e) {
            LOG.error("Error while processing body '{}'.", body, e);
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body("Send body is not a valid Json representing a Blind");
        }
    }

    private Blind fetchBlindById(final UUID id) {
        return entityManager.find(Blind.class, id);
    }

    private List<Blind> fetchAllActiveBlinds() {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Blind> query = cb.createQuery(Blind.class);
        final Root<Blind> root = query.from(Blind.class);
        query.select(root);
        query.where(cb.equal(root.get(Blind.Fields.deviceStatus), DeviceStatus.ACTIVE));

        return entityManager.createQuery(query).getResultList();
    }
}
