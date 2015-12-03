package ru.zipta.authtest;

/**
 * Created by User on 10.08.2015.
 */
class UpdateConnect {
}

class StatusQuery {
}

class GPSLoggerCommand {

    public final static int START = 0;
    public final static int STOP = 1;
    public final static int STATUS = 2;

    public final int command;

    GPSLoggerCommand(int command) {
        this.command = command;
    }
}

class GPSLoggerStatus{
    public final static int EMPTY = 0;
    public final static int NEW_POSITION = 1;

    public final boolean active;
    public final int event;

    public GPSLoggerStatus(boolean active) {
        this.active = active;
        this.event = EMPTY;
    }

    public GPSLoggerStatus(boolean active, int event) {
        this.active = active;
        this.event = event;
    }

}

class StatusReply {
    public final String status;

    public StatusReply(String status) {
        this.status = status;
    }
}

class ErrorEvent {
    public final int status;
    public final String message;

    ErrorEvent(int command, String status) {
        this.status = command;
        this.message = status;
    }
}
