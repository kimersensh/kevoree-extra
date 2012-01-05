package lsr.common;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Represents command which is sent by client to replica. In response to this
 * command, replica should send {@link ClientReply}.
 */
public class ClientCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    private final CommandType commandType;
    private final Request request;

    /**
     * The type of command.
     */
    public enum CommandType {
        REQUEST, ALIVE
    };

    /**
     * Creates new command.
     * 
     * @param commandType - the type of command
     * @param args - the argument for this command
     */
    public ClientCommand(CommandType commandType, Request args) {
        this.commandType = commandType;
        request = args;
    }

    /**
     * Creates new command from <code>ByteBuffer</code> which contain serialized
     * command.
     * 
     * @param input - the buffer with serialized command
     */
    public ClientCommand(ByteBuffer input) {
        commandType = CommandType.values()[input.getInt()];
        // Discard the next int, size of request.
        input.getInt();
        request = Request.create(input);
    }

    /**
     * Writes serialized command to specified buffer. The remaining amount of
     * bytes in the buffer has to be greater or equal than
     * <code>byteSize()</code>.
     * 
     * @param buffer - the byte buffer to write command to
     */
    public void writeTo(ByteBuffer buffer) {
        buffer.putInt(commandType.ordinal());
        buffer.putInt(request.byteSize());
        request.writeTo(buffer);
    }

    /**
     * The size of the command after serialization in bytes.
     * 
     * @return the size of the command in bytes
     */
    public int byteSize() {
        return 4 + 4 + request.byteSize();
    }

    /**
     * Returns the type of command.
     * 
     * @return command type
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Returns the request (argument) for this command.
     * 
     * @return request (argument) object
     */
    public Request getRequest() {
        return request;
    }

    public String toString() {
        return commandType + ": " + request;
    }
}
