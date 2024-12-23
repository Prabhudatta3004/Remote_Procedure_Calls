package dslabs.kvstore;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashMap;

@ToString
@EqualsAndHashCode
public class KVStore implements Application {

    private final HashMap<String, String> store = new HashMap<>();

    public interface KVStoreCommand extends Command {
    }

    public interface SingleKeyCommand extends KVStoreCommand {
        String key();
    }

    @Data
    public static final class Get implements SingleKeyCommand {
        @NonNull private final String key;

        @Override
        public boolean readOnly() {
            return true;
        }
    }

    @Data
    public static final class Put implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    @Data
    public static final class Append implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    public interface KVStoreResult extends Result {
    }

    @Data
    public static final class GetResult implements KVStoreResult {
        @NonNull private final String value;
    }

    @Data
    public static final class KeyNotFound implements KVStoreResult {
    }

    @Data
    public static final class PutOk implements KVStoreResult {
    }

    @Data
    public static final class AppendResult implements KVStoreResult {
        @NonNull private final String value;
    }

    @Override
    public KVStoreResult execute(Command command) {
        if (command instanceof Get) {
            Get g = (Get) command;
            String value = store.get(g.key());
            if (value == null) {
                return new KeyNotFound();
            }
            return new GetResult(value);
        }

        if (command instanceof Put) {
            Put p = (Put) command;
            store.put(p.key(), p.value());
            return new PutOk();
        }

        if (command instanceof Append) {
            Append a = (Append) command;
            String existingValue = store.get(a.key());
            if (existingValue == null) {
                existingValue = "";
            }
            String newValue = existingValue + a.value();
            store.put(a.key(), newValue);
            return new AppendResult(newValue);
        }

        throw new IllegalArgumentException("Unknown command type");
    }
}
