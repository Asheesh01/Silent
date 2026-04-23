import { io } from "socket.io-client";

const BACKEND_URL = import.meta.env.VITE_APP_BACKEND_URL || "https://vercel-backend-1-13r6.onrender.com";

const socket = io(BACKEND_URL, {
  withCredentials: true,
  transports: ['websocket', 'polling'], // Try WebSocket first, fallback to polling
  reconnection: true,
  reconnectionAttempts: 10, // Increased for better reliability
  reconnectionDelay: 2000,
  timeout: 20000, // 20 seconds timeout
});

socket.on("connect", () => {
  console.log("✅ Connected to Socket.IO server:", socket.id);
});

socket.on("connect_error", (error) => {
  console.error("❌ Socket connection error:", error.message);
});

socket.on("disconnect", (reason) => {
  console.log("⚠️ Disconnected:", reason);
  if (reason === "io server disconnect") {
    // Server disconnected, manually reconnect
    socket.connect();
  }
});

socket.on("reconnect", (attemptNumber) => {
  console.log("🔄 Reconnected after", attemptNumber, "attempts");
});

socket.on("reconnect_attempt", (attemptNumber) => {
  console.log("🔄 Reconnection attempt:", attemptNumber);
});

socket.on("reconnect_error", (error) => {
  console.error("❌ Reconnection error:", error.message);
});

socket.on("reconnect_failed", () => {
  console.error("❌ Reconnection failed after all attempts");
});

export default socket;