package ch.raphaelbrunner.blindautomation.executor.handler;

import ch.raphaelbrunner.blindautomation.action.Action;
import ch.raphaelbrunner.blindautomation.action.ActionType;
import ch.raphaelbrunner.blindautomation.model.Blind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ActionExecutor {

    private final Map<ActionType, ActionFunction> actionMap;

    @Autowired
    public ActionExecutor(final RestTemplate restTemplate) {
        this.actionMap = new HashMap<>();
        this.actionMap.put(ActionType.MOVE, (blind, action) -> {
            final Map<String, Object> params = new HashMap<>();
            params.put("value", (int) action.getValue());
            restTemplate.exchange(blind.getUrl() + "/roller/0?go=to_pos&roller_pos={value}",
                    HttpMethod.GET, null, String.class, params);
        });
        this.actionMap.put(ActionType.OPEN, (blind, action) ->
            restTemplate.exchange(blind.getUrl() + "/roller/0?go=open", HttpMethod.GET, null, String.class)
        );
        this.actionMap.put(ActionType.CLOSE, (blind, action) ->
            restTemplate.exchange(blind.getUrl() + "/roller/0?go=close", HttpMethod.GET, null, String.class)
        );
        this.actionMap.put(ActionType.TILT, (blind, action) -> {
            final Map<String, Object> params = new HashMap<>();
            params.put("duration", Math.abs(action.getValue()));
            params.put("direction", action.getValue() > 0 ? "open" : "close");
            restTemplate.exchange(blind.getUrl() + "/roller/0?go={direction}&duration={duration}",
                    HttpMethod.GET, null, String.class, params);
        });
    }

    public void execute(final Action action, final Blind blind) {
        final ActionFunction executionFunction = actionMap.get(action.getAction());
        executionFunction.execute(blind, action);
    }
}
