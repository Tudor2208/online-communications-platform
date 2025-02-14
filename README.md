# Key Features
- **Authentication & Registration**: Users can create an account and log in.
- **Friend System**: Users can send and accept friend requests to connect with others.
- **Private Messaging**: Friends can exchange both text and voice messages in private conversations.
- **Message Status** Indicators: The platform supports "seen" and "typing" indicators to enhance communication.
- **Group Creation**: Users can create groups and invite others to join.
- **Joining Groups**: Users can join existing groups.
- **Group Messaging**: Members of a group can send both text and voice messages within the group chat.

# Architecture Overview
- **Frontend**: Developed in React, the frontend handles the user interface, including features such as authentication, friend management, group management, and conversations. It communicates with the backend via REST API requests and WebSocket connections.
- **User Management Backend**: A dedicated Spring Boot application responsible for managing users, authentication, and relationships (friends, group invitations). It uses a MySQL database to store user data.
- **Chat Management Backend**: A separate Spring Boot application that handles conversations, messages, and group management. It also uses a MySQL database to store messages and group metadata.
- **Apache Kafka**: To maintain data consistency between the two backend services, the application uses Apache Kafka.

![image](https://github.com/user-attachments/assets/becc9375-3b5f-4b9f-80b6-b940b64abfb1)

# App Views
![image](https://github.com/user-attachments/assets/0ab65ec9-eadd-46ab-8605-32e4561457f9)

![image](https://github.com/user-attachments/assets/ec3e1258-d758-4d5d-af13-48a51022af7f)



