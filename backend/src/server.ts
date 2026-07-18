import { createServer } from 'http';
import { Server } from 'socket.io';
import app from './app';
import connectDB from './config/db';
import dotenv from 'dotenv';
import User from './models/User';

dotenv.config();

connectDB();

const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: {
    origin: '*',
  },
});

io.on('connection', (socket) => {
  console.log(`Socket connected: ${socket.id}`);

  socket.on('setup', async (userData) => {
    socket.join(userData._id);
    socket.data.userId = userData._id; // Store userId in socket
    socket.emit('connected');

    // Update DB and broadcast online status
    try {
      await User.findByIdAndUpdate(userData._id, { isOnline: true });
      socket.broadcast.emit('user online', userData._id);
    } catch (err) {
      console.error(err);
    }
  });

  socket.on('join chat', (room) => {
    socket.join(room);
    console.log('User Joined Room: ' + room);
  });

  socket.on('typing', (room) => socket.in(room).emit('typing'));
  socket.on('stop typing', (room) => socket.in(room).emit('stop typing'));

  socket.on('new message', (newMessageRecieved) => {
    var chat = newMessageRecieved.chatId;

    if (!chat.participants) return console.log('chat.participants not defined');

    chat.participants.forEach((user: any) => {
      if (user._id == newMessageRecieved.sender._id) return;

      socket.in(user._id).emit('message recieved', newMessageRecieved);
    });
  });

  socket.on('disconnect', async () => {
    console.log(`Socket disconnected: ${socket.id}`);
    const userId = socket.data.userId;
    if (userId) {
      try {
        await User.findByIdAndUpdate(userId, { 
          isOnline: false, 
          lastOnline: Date.now() 
        });
        io.emit('user offline', userId);
      } catch (err) {
        console.error(err);
      }
    }
  });
});

const PORT = process.env.PORT || 5000;

httpServer.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
