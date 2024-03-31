# ChatServer

## Overview

The ChatServer is a multithreaded chat room server implemented in Java. When a client connects to the server, the server requests a screen name. The server keeps requesting a name until a unique one is received. After a client submits a unique name, the server acknowledges it and then broadcasts all messages from that client to all other clients in the chat room. Messages broadcasted are prefixed with "MESSAGE ".

This server is designed as a teaching example to illustrate a simple chat server, and there are a few features that are left out but would be useful in a production-ready version.

## Features

1. **Unique Screen Names**: The server ensures that each client has a unique screen name.
2. **Message Broadcasting**: Messages from a client are broadcasted to all other clients.
3. **Multithreaded**: The server is capable of handling multiple clients simultaneously using multithreading.
4. **Thread Safety**: Ensures thread safety when accessing shared resources.

## How it Works

### Main Method

The main method starts the server by listening on a specific port (PORT = 9001) and spawning handler threads for each client that connects.

```java
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
```
## Handler Class

### Description

The `Handler` class is responsible for dealing with a single client and broadcasting its messages. The handler:

- Requests a unique screen name from the client.
- Acknowledges the name and registers the client's output stream.
- Broadcasts messages from the client to all other clients.

```java
private static class Handler implements Runnable {
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Handler(Socket socket) {
        this.socket = socket;
    }

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
```

## Thread Safety
The code includes thread safety by using synchronized blocks when accessing the shared names HashSet.

```java
synchronized (names) {
    // Access and modify the names HashSet
}
```