import { io } from "socket.io-client";

const socket = io(import.meta.env.VITE_APP_BACKEND_URL, {
  transports: ["polling"], // Force polling since Vercel doesn't support WebSockets
  withCredentials: true,
});

socket.on("connect", () => {
  console.log("✅ Connected to Socket.IO server:", socket.id);
});

socket.on("connect_error", (error) => {
  console.error("❌ Socket connection error:", error.message);
});

export default socket;
