package client;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Asynchronous protocol to send messages out over a PrintWriter socket
 * @author Josh
 *
 */
public class ClientSendProtocol implements Runnable {
    
    private final PrintWriter out;
    private final String message;
    
    /**
     * Asynchronous printwriter.  Writes message to PrintWriter socket.
     * @param out: PrintWriter to write message to
     * @param message: message to write
     */
    public ClientSendProtocol(PrintWriter out, String message) {
        this.out = out;
        this.message = message;
    }
    
    /**
     * Sends message to server over a PrintWriter
     * @throws IOException 
     */
    @Override
    public void run() {
		out.println(message);
    }

}
