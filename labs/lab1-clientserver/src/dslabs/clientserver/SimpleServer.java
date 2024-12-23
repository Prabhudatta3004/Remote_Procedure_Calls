package dslabs.clientserver;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple server that receives requests and returns responses.
 *
 * See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
    private final KVStore app;

    /* -------------------------------------------------------------------------
        Construction and Initialization
       -----------------------------------------------------------------------*/
    public SimpleServer(Address address, KVStore app) {
        super(address);
        this.app = app; // Use the provided KVStore instance
    }

    @Override
    public void init() {
        // No additional initialization necessary for this simple server
    }

    /* -------------------------------------------------------------------------
        Message Handlers
       -----------------------------------------------------------------------*/
    private void handleRequest(Request request, Address sender) {
        // Execute the command using the KVStore application
        Result result = app.execute(request.command());

        // Create a reply with the result and the same sequence number
        Reply reply = new Reply(result, request.sequenceNum());

        // Send the reply back to the client
        send(reply, sender);
    }
}
