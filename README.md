# Java Group Chat Application

A real-time multi-client group chat application developed using **Java, JavaFX, TCP Socket Programming, and Multithreading**. The application follows a client-server architecture where multiple users can connect to a central server and communicate simultaneously through a graphical chat interface.

## Project Overview

This project was developed to demonstrate practical implementation of networking, concurrent programming, and graphical user interface development in Java.

A central server listens for incoming client connections and manages communication between connected users. Each connected client can send messages that are broadcast to other users in real time.

The user interface was developed using JavaFX to provide a chat-style experience with message display, timestamps, automatic scrolling, and image-sharing functionality.

## Features

- Real-time communication between multiple clients
- Client-server architecture using Java TCP sockets
- Multiple simultaneous client connections using multithreading
- Message broadcasting to connected users
- JavaFX graphical user interface
- Chat-style message display
- Message timestamps
- Automatic chat scrolling
- Image sharing using Base64 encoding
- Separate server-side and client-side application logic

## Technologies Used

- Java
- JavaFX
- TCP Socket Programming
- Multithreading
- Object-Oriented Programming (OOP)
- Base64 Encoding

## Project Structure

```text
src/
├── group/
│   └── chatting/
│       └── application/
│           ├── ChatAppFX.java
│           └── Server.java
│
└── icons/
    ├── 3.png
    ├── 3icon.png
    ├── groupdp2.PNG
    ├── phone.png
    └── video.png

Screenshots/
├── sc1.png
└── sc3.png

README.md
