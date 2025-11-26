# Ticket Reservation System

> **Project Status:** In active development. Some features are still in progress.

This project is a complete theatre ticket reservation system built in Java.  
It includes:

- A desktop client with a Swing user interface  
- Two backend servers (one using Java RMI and one using TCP sockets)  
- Shared model classes for shows, performances, users, and reservations  
- A clean Maven architecture for consistent building and packaging

The goal of the project is to demonstrate a multi-tier architecture using Java,  
with a graphical client that communicates with backend services to manage  
reservations, availability, user actions, and show information.

---

## Features

### Client (Swing GUI)
- Login and user registration  
- Browse available shows  
- View show and performance details  
- Make reservations  
- View existing reservations  
- Admin features for managing shows and performances  

---

## Server 1 — RMI Server
- Exposes remote methods used by the client  
- Handles user actions, reservations, and system logic  
- Uses `ReservationInterface` for clean remote invocation

---

## Server 2 — TCP Server
- Stores shows and performances using serialized `.dat` files  
- Accepts multiple clients through threads  
- Uses a simple command-based text protocol  
- Provides a lightweight alternative backend

---

## Technologies Used

- Java 17+
- Maven (project build, dependencies, modular structure)
- Java RMI
- TCP sockets and threading
- Java Swing
- Object serialization for data storage

---

## Project Structure

```
/client        → Swing client application  
/server1       → RMI server  
/server2       → TCP server  
/common        → Shared model classes  
```

Each module builds its own JAR using Maven.

---

## Build Instructions

Clone the repository:

```bash
git clone https://github.com/your-username/your-repo.git
cd your-repo
```

Build using Maven:

```bash
mvn clean install
```

---

## Run Instructions

### 1. Start Server 2 (TCP)
```bash
java -jar server2/target/server2.jar
```

### 2. Start Server 1 (RMI)
```bash
java -jar server1/target/server1.jar
```

### 3. Launch the Client
```bash
java -jar client/target/client.jar
```

Make sure both servers are running before launching the client.

---

## Notes

- No external database is required.  
- All persistent data is stored using `.dat` serialization.  
- Compatible with any OS that supports Java.

---

## License

MIT (or your preferred license)
