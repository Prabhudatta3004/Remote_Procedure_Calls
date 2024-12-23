package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static dslabs.clientserver.ClientTimer.CLIENT_RETRY_MILLIS;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
    private final Address serverAddress;
    private int sequenceNum;
    private Result result;
    private Command lastCommand;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleClient(Address address, Address serverAddress) {
        super(address);
        this.serverAddress = serverAddress;
        sequenceNum = 0;
    }

    @Override
    public synchronized void init() {
        // No initialization necessary
    }

    /* -------------------------------------------------------------------------
        Client Methods
       -----------------------------------------------------------------------*/
    @Override
    public synchronized void sendCommand(Command command) {
        // Store the command for potential retries
        lastCommand = command;

        // Create and send the request
        Request request = new Request(command, sequenceNum);
        result = null; // Reset the result
        send(request, serverAddress);

        // Start a retry timer
        set(new ClientTimer(sequenceNum), CLIENT_RETRY_MILLIS);
    }

    @Override
    public synchronized boolean hasResult() {
        return result != null;
    }

    @Override
    public synchronized Result getResult() throws InterruptedException {
        // Wait for the result to be set by handleReply
        while (!hasResult()) {
            wait();
        }
        return result;
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private synchronized void handleReply(Reply reply, Address sender) {
        // Ensure the sequence number matches
        if (reply.sequenceNum() == sequenceNum) {
            result = reply.result(); // Set the result
            sequenceNum++; // Increment sequence number for the next command
            notifyAll(); // Notify waiting threads
        }
    }

    /* -------------------------------------------------------------------------
        Timer Handlers
       -----------------------------------------------------------------------*/
    private synchronized void onClientTimer(ClientTimer timer) {
        // Resend the request if no result has been received
        if (timer.sequenceNum() == sequenceNum && result == null) {
            Request request = new Request(lastCommand, sequenceNum);
            send(request, serverAddress);

            // Restart the timer
            set(timer, CLIENT_RETRY_MILLIS);
        }
    }
}
