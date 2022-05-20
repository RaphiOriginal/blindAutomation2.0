package ch.raphaelbrunner.blindautomation.discovery.entity;

import ch.raphaelbrunner.blindautomation.discovery.enumeration.Action;
import ch.raphaelbrunner.blindautomation.common.interfaces.AutomationObject;
import lombok.Data;

@Data
public class Device implements AutomationObject {
    private String id;
    private String url;
    private Action action;
}
