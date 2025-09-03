1. Introduction
  LoopLink is a Kotlin Multiplatform (KMP) project designed to enable peer-to-peer (P2P) communication and data exchange between devices connected to the same local network. It leverages service discovery protocols, direct WebSocket communication, and a shared multiplatform codebase to ensure consistent functionality across Android mobile devices and JVM-based desktop platforms (Windows, macOS, Linux).
2. Conceptual Design
   2.1 Core Concept

LoopLink’s objective is to provide seamless P2P connectivity by allowing devices to act both as servers and clients within a decentralized local network architecture.

2.2 Architecture

Decentralized: Devices function as both client and server.

Service Discovery: Devices advertise their presence and detect others on the same network.

Direct Communication: Discovered devices establish WebSocket-based connections for message exchange.

2.3 Platform Targets

Android OS (mobile).

JVM desktop systems (Windows, macOS, Linux).

2.4 Key Technologies

Kotlin Multiplatform (KMP): Shared logic across platforms.

Jetpack Compose Multiplatform: Shared UI framework.

Ktor Networking Library: WebSocket-based client-server communication.

Local Network Service Discovery:

JmDNS (Java implementation of mDNS/DNS-SD) for JVM.

Android NSD API for mobile devices.

2.5 High-Level Interaction Flow

User launches LoopLink.

The application starts a Ktor server and registers itself as a service on the LAN.

The application simultaneously searches for other LoopLink services.

Discovered peers are presented in the UI.

The user selects a peer to initiate a WebSocket connection.

Messages or data are exchanged over the established channel.

3. Methodology
   3.1 Development Paradigm

Kotlin Multiplatform (KMP): Shared business logic with platform-specific implementations using expect/actual.

Iterative Development: Implemented incrementally in six stages—project setup, service discovery, server configuration, client configuration, UI integration, and WebSocket messaging.

3.2 Networking Strategy

Service Discovery:

Abstracted via LocalNetworkServiceDiscovery in commonMain.

Implementations: JmDNS (JVM) and NSD (Android).

Communication Protocol: WebSockets for bidirectional real-time communication.

Server: Implemented using Ktor’s embeddedServer with shared configuration.

Client: Configured using HttpClient with WebSocket support and CIO engine.

3.3 User Interface

Compose Multiplatform: Unified UI code (App.kt) across Android and Desktop.

Reactive UI: State management through Kotlin Flows and Compose State integrated with PeerDiscoveryViewModel.

3.4 Data Persistence

SQLDelight:

Schema-defined tables (e.g., LoopLinkUser).

Generates type-safe APIs.

Platform-specific SQLite drivers integrated via DriverFactory.

3.5 Concurrency

Extensive use of Kotlin Coroutines for asynchronous service discovery, networking, and server tasks.

4. Literature and Background Study
   4.1 Peer-to-Peer Networking

Advantages: Decentralization, scalability in LAN environments.

Challenges: Discoverability, NAT traversal (excluded from current scope).

4.2 Local Area Network (LAN) Service Discovery

mDNS/DNS-SD: Standard protocols for zero-configuration networking.

Bonjour/JmDNS: Common implementations in Apple and Java ecosystems.

Android NSD: Platform-provided DNS-SD API for service discovery.

4.3 WebSocket Protocol

Enables full-duplex communication over a single TCP connection.

More efficient for real-time communication compared to traditional HTTP.

4.4 Kotlin Multiplatform (KMP)

Supports code reuse across Android, Desktop, iOS, and Web.

Implements expect/actual mechanism for platform-specific functionality.

4.5 Compose Multiplatform

Declarative UI framework supporting multiple platforms.

Ensures consistency and reduces duplicated UI effort.

4.6 Ktor Framework

Lightweight, asynchronous networking library.

Supports client/server WebSockets, HTTP, and plugins for extensibility.

4.7 SQLDelight

Type-safe database APIs generated from SQL schema.

Enables multiplatform database support with shared Kotlin interfaces.

5. Data Collection
   5.1 Service Discovery Data

ServiceInfo model containing:

instanceName, serviceType, hostAddress, port, attributes.

Attributes include deviceId, platform, and model for peer identification.

5.2 Communication Data

WebSocket-based message exchange (currently text).

Future extensions: JSON-based structured data, binary messages, file transfer.

5.3 Local Database

LoopLinkUser Table: Stores user identifiers and names.

Accessed via SQLDelight-generated APIs.

6. Research and Development Methodology
   6.1 Problem Decomposition

Modularization of discovery, server, client, UI, and database subsystems.

6.2 Abstraction for Portability

Platform-specific APIs encapsulated through expect/actual.

Example abstractions:

LocalNetworkServiceDiscovery

createHttpClientEngine

DriverFactory

6.3 Modular Design

Network: org.asv.looplink.network

Discovery: org.asv.looplink.network.discovery

Database: org.asv.looplink.database

UI: App.kt

6.4 Reactive Programming

Streams managed via Kotlin Flows for real-time service discovery updates.

6.5 Dependency Management

Gradle with libs.versions.toml for dependency versioning.

6.6 Troubleshooting and Iteration

Addressing Android-specific permission issues.

Ensuring WebSocket engine compatibility across platforms.

Iterative refinement and testing of service discovery mechanisms.

7. Project Feasibility
   7.1 Technical Feasibility

Implemented and tested:

Service discovery.

WebSocket-based client/server communication.

Basic Compose-based UI.

Utilizes robust, mature libraries (Ktor, SQLDelight, Compose, JmDNS, NSD).

7.2 Tools and Ecosystem

Development in Android Studio with KMP support.

Dependency management via Gradle.

7.3 Benefits

Code Reuse: Significant shared logic across Android and Desktop.

Maintainability: Single codebase reduces overhead.

Consistent UX: Shared UI layer improves user experience across platforms.

7.4 Limitations

Local Network Only: No NAT traversal or internet-based communication.

Basic UI: Functional but not fully optimized.

Error Handling: Limited robustness in production scenarios.

Security: No encryption or authentication currently implemented.

Background Operation: Android server tied to MainActivity; requires Foreground Service for robustness.

8. Conclusion

LoopLink demonstrates the feasibility of building a multiplatform P2P communication system using Kotlin Multiplatform, Ktor, Compose Multiplatform, and SQLDelight. Its modular design and shared codebase highlight the benefits of multiplatform development while addressing key challenges in service discovery and real-time communication.