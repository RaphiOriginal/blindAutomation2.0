package ch.raphaelbrunner.blindautomation.common.entity;

import ch.raphaelbrunner.blindautomation.common.interfaces.AutomationObject;
import lombok.Data;

import java.util.List;

@Data
public class ListResponse <T extends AutomationObject> {
    private int total;
    private List<T> data;
}
