package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A multithreaded chat room server. When a client connects the server requests a screen name by sending the client the
 * text "SUBMITNAME", and keeps requesting a name until a unique one is received. After a client submits a unique
 * name, the server acknowledges with "NAMEACCEPTED". Then all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name. The broadcast messages are prefixed with "MESSAGE ".
 * <p>
 * Because this is just a teaching example to illustrate a simple chat server, there are a few features that have been left out.
 * Two are very useful and belong in production code:
 * <p>
 * 1. The protocol should be enhanced so that the client can send clean disconnect messages to the server.
 * 2. The server should do some logging.
 */
public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room. Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients. This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * The map to store the PrintWriter objects along with the client names
     */
    private static Map<String, PrintWriter> clientMap = new HashMap<>();

    /**
     * The application main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket serverSocket = new ServerSocket(PORT);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }

                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            clientMap.put(name, out);  // Store the PrintWriter along with the name
                            break;
                        }
                    }
                }

                // Acknowledge the name and register the output stream.
                out.println("NAMEACCEPTED");
                writers.add(out);

                // Broadcast all clients the new client's name.
                broadcast("NEWCLIENT " + name);

                // Accept messages from this client and broadcast them.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    broadcast("MESSAGE " + name + ": " + input);
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    synchronized (names) {
                        names.remove(name);
                    }
                    clientMap.remove(name);  // Remove the client from the clientMap
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

        /**
         * Broadcast a message to all clients
         */
        private void broadcast(String message) {
            for (PrintWriter writer : writers) {
                writer.println(message);
            }
        }

        /**
         * Send a message to a specific client
         */
        private void sendMessageToClient(String clientName, String message) {
            PrintWriter writer = clientMap.get(clientName);
            if (writer != null) {
                writer.println(message);
            }
        }
    }
}
