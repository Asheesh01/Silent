const express = require('express');
const app = express();
const cookieparser = require('cookie-parser')
const cors = require('cors');
require('dotenv').config();
require('./connection');
console.log("✅ Loaded PORT from env:", process.env.PORT);
const PORT = process.env.PORT || 5000
const {Server} = require('socket.io');
const http = require("http")
const server = http.createServer(app)

const io = new Server(server, {
    cors: {
        origin: "http://localhost:5173",
        methods: ['GET', 'POST'],
        credentials: true
    }
})


app.use(express.json());
app.use(cookieparser());
app.use(cors({
    credentials: true,
    origin: "http://localhost:5173"
}))

io.on('connection', (socket) => {
    console.log(" User Connected:", socket.id)

    socket.on("joinConversation", (conversationId) => {
        console.log(`User joined Conversation ID: ${conversationId}`)
        socket.join(conversationId)
    })
    
    socket.on("sendMessage", (convId, messageDetail) => {
        console.log(" Message sent to conversation:", convId)
        io.to(convId).emit("recieveMessage", messageDetail)
    })
    
    socket.on('disconnect', () => {
        console.log(' User disconnected:', socket.id)
    })
     socket.on("deleteMessage", (convId, messageId) => {
        console.log("Message deleted from conversation:", convId);
        io.to(convId).emit("messageDeleted", messageId);
    });
})

const UserRoutes = require('./routes/user');
const postroutes = require('./routes/post');
const Notificationroute = require('./routes/Notificartion');
const CommentRoutes = require("./routes/comment");
const conversastionroute = require('./routes/conversation');
const messageroute = require('./routes/message');

//  REMOVED - These are frontend imports and should NOT be in backend
// const { useEffect } = require('react');
// const { default: socket } = require('../Linkedin-Frontend/socket');

app.use('/api/auth', UserRoutes);
app.use('/api/post', postroutes);
app.use('/api/Notification', Notificationroute)
app.use("/api/comment", CommentRoutes);
app.use("/api/conversation", conversastionroute);
app.use("/api/message", messageroute);

server.listen(PORT, () => {
    console.log(`Backend server running on port ${PORT}`)
    console.log(`Socket.IO server ready at http://localhost:${PORT}`)
})