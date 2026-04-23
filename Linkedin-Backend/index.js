//Backend
const express = require("express");
const app = express();
const cookieParser = require("cookie-parser");
const cors = require("cors");
require("dotenv").config();
require("./connection"); // Ensure your DB connection file exports mongoose.connect()
const http = require("http");
const { Server } = require("socket.io");
const server = http.createServer(app);
// ✅ PORT setup
const PORT = process.env.PORT || 5000;
// ✅ Allowed frontend URLs (local + production)
const allowedOrigins = [
  "http://localhost:5173",
  "http://localhost:5174",
  "https://vercel-frontend-kohl-gamma.vercel.app",
  "https://vercel-backend-1-13r6.onrender.com", // Add this
];
// ✅ Global middleware
app.use(
  cors({
    origin: allowedOrigins,
    credentials: true,
  })
);
app.use(express.json());
app.use(cookieParser());
// ✅ Socket.IO setup
const io = new Server(server, {
  cors: {
    origin: allowedOrigins,
    methods: ["GET", "POST"],
    credentials: true,
  },
});
io.on("connection", (socket) => {
  console.log("✅ User Connected:", socket.id);

  socket.on("joinConversation", (conversationId) => {
    console.log(`📥 User joined Conversation ID: ${conversationId}`);
    socket.join(conversationId);
  });

  socket.on("sendMessage", (convId, messageDetail) => {
    console.log(`📤 Message sent to conversation: ${convId}`);
    io.to(convId).emit("receiveMessage", messageDetail);
  });
  socket.on("deleteMessage", (convId, messageId) => {
    console.log(`🗑 Message deleted from conversation: ${convId}`);
    io.to(convId).emit("messageDeleted", messageId);
  });

  socket.on("disconnect", () => {
    console.log("❌ User disconnected:", socket.id);
  });
});

// ✅ Health check route
app.get("/", (req, res) => {
  res.send("✅ Backend is running successfully!");
});

// ✅ API routes
app.use("/api/auth", require("./routes/user"));
app.use("/api/post", require("./routes/post"));
app.use("/api/Notification", require("./routes/Notificartion"));
app.use("/api/comment", require("./routes/comment"));
app.use("/api/conversation", require("./routes/conversation"));
app.use("/api/message", require("./routes/message"));

// ✅ Start the server
server.listen(PORT, () => {
  console.log(`🚀 Backend server running on port ${PORT}`);
  console.log(`📡 Socket.IO ready at http://localhost:${PORT}`);
});
