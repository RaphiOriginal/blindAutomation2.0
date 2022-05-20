package ch.raphaelbrunner.blindautomation.model;

import ch.raphaelbrunner.blindautomation.common.interfaces.AutomationObject;
import ch.raphaelbrunner.blindautomation.model.enums.BlindStatus;
import ch.raphaelbrunner.blindautomation.model.enums.DeviceStatus;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name="T_BLIND")
@FieldNameConstants
public class Blind implements AutomationObject {
    @Id
    private UUID id;

    @Column(name="NAME")
    private String name;

    @Column(name="SUN_IN_POSITION")
    private float sunInPosition;

    @Column(name="SUN_OUT_POSITION")
    private float sunOutPosition;

    @Column(name="URL")
    private String url;

    @Column(name="DEVICE_ID")
    private String deviceId;

    @Column(name="DEVICE_STATUS")
    @Enumerated
    private DeviceStatus deviceStatus;

    @Transient
    private BlindStatus blindStatus;

    @Transient
    private int blindTiltDegree;

    @Column(name="DURATION")
    private float duration;
}
