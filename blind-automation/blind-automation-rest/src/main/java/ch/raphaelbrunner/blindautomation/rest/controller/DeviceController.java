package ch.raphaelbrunner.blindautomation.rest.controller;

import ch.raphaelbrunner.blindautomation.common.entity.ListResponse;
import ch.raphaelbrunner.blindautomation.model.Blind;
import ch.raphaelbrunner.blindautomation.model.enums.DeviceStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@Transactional
public class DeviceController {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceController.class);

    private final ObjectMapper mapper;

    private final EntityManager entityManager;

    @Autowired
    public DeviceController(final ObjectMapper mapper, final EntityManager entityManager) {
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

    @PostMapping("/blindautomation/Blind")
    public ResponseEntity<String> updateBlind(@RequestBody final String body) {
        LOG.debug("Received POST request on /blindautomation/Blind endpoint with body '{}'", body);
        return ResponseEntity.notFound().build();
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
