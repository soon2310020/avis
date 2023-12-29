package saleson.common.event.alert;

import saleson.common.enumeration.AlertType;
import saleson.model.Terminal;
import saleson.model.TerminalDisconnect;

public class DisconnectTerminalAlertEvent{
    private TerminalDisconnect terminalDisconnect;

    public DisconnectTerminalAlertEvent(TerminalDisconnect terminalDisconnect) {
        this.terminalDisconnect = terminalDisconnect;
    }

    public TerminalDisconnect get() {
        return this.terminalDisconnect;
    }

    public AlertType getAlertType() {
        return AlertType.DISCONNECTED;
    }

    public Terminal getTerminal() {
        return terminalDisconnect.getTerminal();
    }
}
