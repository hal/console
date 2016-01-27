package org.jboss.hal.dmr.dispatch;

/**
 * @author Heiko Braun
 * @date 9/17/13
 */
public class DispatchException extends Exception {

    private final int statusCode;

    public DispatchException(String message, int statusCode) {
        super(message + " Status " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
