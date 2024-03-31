### Explanation

#### Server Code

1.  ServerSocket Initialization:

    -   A `ServerSocket` is initialized on port `9001`.
    -   The server waits for a client connection using `serverSocket.accept()`.
2.  Reading and Writing Streams:

    -   The server reads a message from the client using `BufferedReader`.
    -   The received message is converted to uppercase.
    -   The modified message is sent back to the client using `PrintWriter`.

#### Client Code

1.  Socket Initialization:

    -   A `Socket` is initialized to connect to the server running on `localhost` at port `9001`.
2.  Reading and Writing Streams:

    -   The client reads a message from the user using `BufferedReader` from `System.in`.
    -   The message is sent to the server using `PrintWriter`.
    -   The client reads the modified message from the server using `BufferedReader`.