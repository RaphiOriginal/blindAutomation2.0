package ch.raphaelbrunner.blindautomation.discovery.entity;

import ch.raphaelbrunner.blindautomation.discovery.enumeration.Action;
import lombok.Data;

@Data
public class Device {
    private String id;
    private String url;
    private Action action;
}
