package ch.raphaelbrunner.blindautomation.executor.handler;

import ch.raphaelbrunner.blindautomation.action.Action;
import ch.raphaelbrunner.blindautomation.model.Blind;

@FunctionalInterface
public interface ActionFunction {

    void execute(final Blind blind, final Action action);
}
