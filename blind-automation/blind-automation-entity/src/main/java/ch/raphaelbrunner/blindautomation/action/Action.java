package ch.raphaelbrunner.blindautomation.action;

import lombok.Data;

import java.util.UUID;

@Data
public class Action {
    private UUID blindId;
    private ActionType action;
    private float value;
}
