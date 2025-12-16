# Group Chat Application (JavaFX + Sockets)

A real-time multi-client group chat app built with Java TCP sockets and a JavaFX UI. The server broadcasts messages to all connected clients and supports text + Base64 image sharing.

## Features
- Multi-client chat using ServerSocket/Socket
- One thread per client (ClientHandler)
- Broadcast messages to all users
- JavaFX chat UI with message bubbles, timestamps, and auto-scroll
- Image sharing using Base64 ([IMG] tag)

## How to Run
1. Run `Server.java` (starts on port 5000)
2. Run `ChatAppFX.java` (opens multiple client windows and connects to the server)
