package dslabs.atmostonce;

import com.google.common.base.Objects;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application>
        implements Application {
    @Getter @NonNull private final T application;

    // Your code here...
    private final HashMap<Address, AMOResult> lastResult = new HashMap<>();


    @Override
    public AMOResult execute(Command command) {
        if (!(command instanceof AMOCommand)) {
            throw new IllegalArgumentException();
        }

        AMOCommand amoCommand = (AMOCommand) command;

        // Your code here...
        if (alreadyExecuted(amoCommand)) {
            return lastResult.get(amoCommand.clientAddress());
        }

        Result result = application.execute(amoCommand.command());
        AMOResult amoResult = new AMOResult(result, amoCommand.sequenceNum());
        lastResult.put(amoCommand.clientAddress(), amoResult);
        return amoResult;

        //        return null;
    }

    public Result executeReadOnly(Command command) {
        if (!command.readOnly()) {
            throw new IllegalArgumentException();
        }

        if (command instanceof AMOCommand) {
            return execute(command);
        }

        return application.execute(command);
    }

    public boolean alreadyExecuted(AMOCommand amoCommand) {
        // Your code here...

        if (lastResult.containsKey(amoCommand.clientAddress())){
            int lastSeqNum = lastResult.get(amoCommand.clientAddress()).sequenceNum();
            return amoCommand.sequenceNum() <= lastSeqNum;
        }
        return false;

    }
}