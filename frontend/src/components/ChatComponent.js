import React, { useState, useEffect, useRef } from "react";
import "font-awesome/css/font-awesome.min.css";
import "../css/ChatComponent.css";
import EmojiPicker from "emoji-picker-react";

const ChatComponent = ({ conversation, closeChat, userId1 }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [editingMessageId, setEditingMessageId] = useState(null);
  const [editingText, setEditingText] = useState("");
  const chatBoxRef = useRef(null);
  const webSocketRef = useRef(null);
  const [isEmojiPickerVisible, setIsEmojiPickerVisible] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [audioBlob, setAudioBlob] = useState(null);
  const mediaRecorderRef = useRef(null);
  const [isTyping, setIsTyping] = useState(false);
  const typingTimeoutRef = useRef(null);

  const handleEmojiClick = (emojiObject) => {
    setNewMessage((prevMessage) => prevMessage + emojiObject.emoji);
    setIsEmojiPickerVisible(false);
  };

  const toggleEmojiPicker = () => {
    setIsEmojiPickerVisible((prev) => !prev);
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;

      const chunks = [];
      mediaRecorder.ondataavailable = (event) => chunks.push(event.data);
      mediaRecorder.onstop = () => {
        const blob = new Blob(chunks, { type: "audio/webm" });
        setAudioBlob(blob);
      };

      mediaRecorder.start();
      setIsRecording(true);
    } catch (error) {
      console.error("Error starting recording:", error);
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
    }
  };

  const sendAudioMessage = async () => {
    if (!audioBlob) return;
  
    const storedUser = JSON.parse(localStorage.getItem("user"));
    const loggedInUserId = storedUser?.id;
  
    if (!loggedInUserId) {
      console.error("User not logged in");
      return;
    }
  
    const formData = new FormData();
    formData.append("file", audioBlob);
    formData.append("senderId", loggedInUserId);
  
    // Append the recipientId or groupId based on the conversation type
    if (conversation.privateConversation) {
      formData.append("recipientId", conversation.friendId);
    } else {
      formData.append("groupId", conversation.groupId);
    }
  
    try {
      const endpoint = conversation.privateConversation
        ? "http://188.24.17.70:8081/api/v1/messages/audio"
        : "http://188.24.17.70:8081/api/v1/group-messages/audio";
  
      const response = await fetch(endpoint, {
        method: "POST",
        body: formData,
        headers: {
          Authorization: `Bearer ${storedUser?.token}`,
        },
      });
  
      if (response.ok) {
        setAudioBlob(null); // Clear the audio blob after successful send
      } else {
        console.error("Error uploading audio:", response.statusText);
      }
    } catch (error) {
      console.error("Error uploading audio:", error);
    }
  };
  
  useEffect(() => {
    const fetchMessages = async () => {
      const storedUser = JSON.parse(localStorage.getItem("user"));
      const token = storedUser?.token;

      if (!token) {
        console.error("Token not found.");
        return;
      }

      try {
        const endpoint = conversation.privateConversation
          ? `http://188.24.17.70:8081/api/v1/messages/conversation?userId1=${userId1}&userId2=${conversation.friendId}`
          : `http://188.24.17.70:8081/api/v1/group-messages/${conversation.groupId}`;

        const response = await fetch(endpoint, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const data = await response.json();

        if (!conversation.privateConversation) {
          const messagesWithSenderNames = await Promise.all(
            data.map(async (msg) => {
              const userResponse = await fetch(
                `http://188.24.17.70:8080/api/v1/users/${msg.senderId}`,
                {
                  headers: {
                    Authorization: `Bearer ${token}`,
                  },
                }
              );
              const userData = await userResponse.json();
              return {
                ...msg,
                senderFirstName: userData.firstName,
                senderLastName: userData.lastName,
              };
            })
          );
          setMessages(messagesWithSenderNames);
        } else {
          setMessages(data);
        }
      } catch (error) {
        console.error("Error fetching messages:", error);
      }
    };

    fetchMessages();

    const wsUrl = "ws://188.24.17.70:8081/chat";
    const ws = new WebSocket(wsUrl);
    webSocketRef.current = ws;

    ws.onopen = () => {
      console.log("WebSocket connected");

      // Send userId immediately after connection
      const storedUser = JSON.parse(localStorage.getItem("user"));
      const userId = storedUser?.id;
      if (userId) {
        ws.send(
            JSON.stringify({
              type: "SUBSCRIBE",
              senderId: storedUser.id,
            })
        );
        console.log("User ID sent:", userId);
      } else {
        console.error("User ID not found in localStorage.");
      }
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Message received from server:", data);

      if (data.type === "DIRECT_MESSAGE_CREATE" && data.message) {
        console.log("Direct message received:", data.message);
        console.log("User ID:", userId1);
        if (data.message.senderId === conversation.friendId || data.message.senderId === userId1 ||
            data.message.sender.id === conversation.friendId || data.message.sender.id === userId1) {
          if (!(data.message.senderId === conversation.friendId || data.message.senderId === userId1)) {
            data.message.senderId = data.message.sender.id;
          }
          setMessages((prevMessages) => [...prevMessages, data.message]);
        }
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (data.message.senderId !== storedUser.id) {
          sendSeenEvent();
        }
      } else if (data.type === "DIRECT_MESSAGE_PATCH" && data.message) {
        console.log("Direct message edited:", data.message);
        setMessages((prevMessages) =>
          prevMessages.map((msg) =>
            msg.id === data.message.id ? { ...msg, text: data.message.text, edited: true } : msg
          )
        );
      } else if (data.type === "DIRECT_MESSAGE_DELETE" && data.message) {
        console.log("Direct messages:", messages);
        console.log("Direct message deleted:", data.message);
        setMessages((prevMessages) =>
          prevMessages.filter((msg) => msg.id !== data.message)
        );
      } else if (data.type === "GROUP_MESSAGE_CREATE" && data.message) {
        console.log("Group message received:", data.message);
        if (data.message.groupId === conversation.groupId) {
          setMessages((prevMessages) => [...prevMessages, data.message]);
        }
      } else if (data.type === "GROUP_MESSAGE_PATCH" && data.message) {
        console.log("Group message edited:", data.message);
        setMessages((prevMessages) =>
          prevMessages.map((msg) =>
            msg.id === data.message.id ? { ...msg, text: data.message.text, edited: true } : msg
          )
        );
      } else if (data.type === "GROUP_MESSAGE_DELETE" && data.message) {
        console.log("Group message deleted:", data.message);
        setMessages((prevMessages) =>
          prevMessages.filter((msg) => msg.id !== data.message)
        );
      } else if (data.type === "TYPING") {
        const { senderId, isTyping } = data.message;
        if (senderId === conversation.friendId) {
          setIsTyping(isTyping);
        }
      } else if (data.type === "SEEN") {
        const { senderId } = data.message;
        console.log(`Messages seen by user ${senderId}`);
        setMessages((prevMessages) =>
            prevMessages.map((msg) =>
                msg.senderId === senderId && !msg.seen
                    ? { ...msg, seen: true }
                    : msg
            )
        );
      }
    };

    ws.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    ws.onclose = () => {
      console.log("WebSocket closed");
    };

    return () => {
      ws.close(); // Cleanup WebSocket on component unmount
    };
  }, [conversation, userId1]);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  const handleMessageChange = (e) => {
    setNewMessage(e.target.value);

    sendTypingEvent(true);

    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {
      sendTypingEvent(false);
    }, 2000);
  };

  const sendTypingEvent = (isTyping) => {
    webSocketRef.current.send(
        JSON.stringify({
          type: "TYPING",
          senderId: userId1,
          recipientId: conversation.friendId,
          content: isTyping,
        })
    );
  };

  const sendSeenEvent = () => {
    webSocketRef.current.send(
        JSON.stringify({
          type: "SEEN",
          senderId: userId1,
          recipientId: conversation.friendId,
        })
    );
  };

  const sendMessage = async () => {
    const storedUser = JSON.parse(localStorage.getItem("user"));
    const loggedInUserId = storedUser?.id;

    if (newMessage.trim()) {
      // const timestamp = Date.now();
      //
      // setMessages([
      //   ...messages,
      //   {
      //     senderId: loggedInUserId,
      //     text: newMessage,
      //     timestamp: timestamp,
      //     edited: false,
      //   },
      // ]);
      setNewMessage("");

      try {
        const endpoint = conversation.privateConversation
          ? "http://188.24.17.70:8081/api/v1/messages"
          : `http://188.24.17.70:8081/api/v1/group-messages`;

        const body = conversation.privateConversation
          ? {
              text: newMessage,
              senderId: loggedInUserId,
              recipientId: conversation.friendId,
            }
          : {
              text: newMessage,
              senderId: loggedInUserId,
              groupId: conversation.groupId,
            };

        const response = await fetch(endpoint, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${storedUser?.token}`,
          },
          body: JSON.stringify(body),
        });

        if (!response.ok) {
          console.error("Error sending message:", response.statusText);
        }
      } catch (error) {
        console.error("Error sending message:", error);
      }
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && newMessage.trim()) {
      sendMessage();
    }
  };

  const handleEditMessage = async (messageId) => {
    const storedUser = JSON.parse(localStorage.getItem("user"));
    const token = storedUser?.token;

    if (editingText.trim()) {
      try {
        const endpoint = conversation.privateConversation
          ? `http://188.24.17.70:8081/api/v1/messages/${messageId}`
          : `http://188.24.17.70:8081/api/v1/group-messages/${messageId}`;

        const response = await fetch(endpoint, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ text: editingText }),
        });

        if (response.ok) {
          setMessages(
            messages.map((msg) =>
              msg.id === messageId ? { ...msg, text: editingText, edited: true } : msg
            )
          );
          setEditingMessageId(null);
          setEditingText("");
        } else {
          console.error("Error editing message:", response.statusText);
        }
      } catch (error) {
        console.error("Error editing message:", error);
      }
    }
  };

  const handleDeleteMessage = async (messageId) => {
    const storedUser = JSON.parse(localStorage.getItem("user"));
    const token = storedUser?.token;

    try {
      const endpoint = conversation.privateConversation
        ? `http://188.24.17.70:8081/api/v1/messages/${messageId}`
        : `http://188.24.17.70:8081/api/v1/group-messages/${messageId}`;

      const response = await fetch(endpoint, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        setMessages(messages.filter((msg) => msg.id !== messageId));
      } else {
        console.error("Error deleting message:", response.statusText);
      }
    } catch (error) {
      console.error("Error deleting message:", error);
    }
  };

  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp);
    return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
  };

  return (
    <div className="chat-component">
      <button className="close-btn" onClick={closeChat}>
        &times;
      </button>
      <h3>
        Chat with{" "}
        {conversation.privateConversation
          ? `${conversation.firstName} ${conversation.lastName}`
          : `Group: ${conversation.groupName}`}
      </h3>
      <div className="chat-box" ref={chatBoxRef}>
        {messages.length === 0 ? (
          <div className="no-messages">Start the conversation...</div>
        ) : (
          messages.map((msg) => (
            <div
              key={msg.id}
              className={`message ${
                msg.senderId === JSON.parse(localStorage.getItem("user"))?.id
                  ? "sent"
                  : "received"
              }`}
            >
              <div className="message-text">
                {editingMessageId === msg.id ? (
                  <input
                    type="text"
                    value={editingText}
                    onChange={(e) => setEditingText(e.target.value)}
                    onKeyDown={(e) =>
                      e.key === "Enter" && handleEditMessage(msg.id)
                    }
                    autoFocus
                  />
                ) : (
                  <>
                    {conversation.privateConversation ? (
                      msg.text
                    ) : (
                      <>
                        {msg.senderId !== JSON.parse(localStorage.getItem("user"))?.id && (
                          <strong id="sender-name">{msg.senderFirstName} {msg.senderLastName}: </strong>
                        )}
                        {msg.text}
                      </>
                    )}
                    {msg.edited && <span className="edited-text"> (Edited)</span>}
                  </>
                )}
                
                {msg.audioData && (
                  <div className="audio-message">
                    <audio controls>
                      <source
                        src={
                          conversation.privateConversation
                            ? `http://188.24.17.70:8081/api/v1/messages/audio/${msg.id}` 
                            : `http://188.24.17.70:8081/api/v1/group-messages/audio/${msg.id}` 
                        }
                        type="audio/mpeg"
                      />
                      Your browser does not support the audio element.
                    </audio>
                  </div>
                )}

              </div>
              <div className="message-time">{formatTimestamp(msg.timestamp)}</div>
              {msg.senderId === JSON.parse(localStorage.getItem("user"))?.id && (
                <div className="message-actions">
                  {!msg.audioData && (
                    <button
                      onClick={() => {
                        setEditingMessageId(msg.id);
                        setEditingText(msg.text);
                      }}
                    >
                      <i id="chat-edit" className="fa fa-edit"></i>
                    </button>
                  )}
                  {/* Always show delete button */}
                  <button onClick={() => handleDeleteMessage(msg.id)}>
                    <i id="chat-delete" className="fa fa-trash"></i>
                  </button>
                </div>
              )}
            </div>
          ))
        )}
        {isTyping && <div className="typing-indicator">Typing...</div>}
      </div>
     
      <div className="message-input">
        <input
          type="text"
          value={newMessage}
          onChange={handleMessageChange}
          onKeyDown={handleKeyPress}
          placeholder="Type a message"
        />
        <div className="emoji-picker">
          <button onClick={toggleEmojiPicker}>
            <i className="fa fa-smile-o"></i>
          </button>
          {isEmojiPickerVisible && (
            <div className="emoji-dropdown">
              <EmojiPicker onEmojiClick={handleEmojiClick} />
            </div>
          )}
        </div>
        <button onClick={sendMessage}>
          <i className="fa fa-paper-plane"></i>
        </button>
        <div className="audio-controls">
          {isRecording ? (
            <button onClick={stopRecording} className="stop-recording">
              <i className="fa fa-stop"></i> Stop
            </button>
          ) : (
            <button onClick={startRecording} className="start-recording">
              <i className="fa fa-microphone"></i> Record
            </button>
          )}
          {audioBlob && (
            <button onClick={sendAudioMessage} className="send-audio">
              <i className="fa fa-paper-plane"></i> Send Audio
            </button>
          )}
        </div>
      </div>
    </div>
  );
};  

export default ChatComponent;
