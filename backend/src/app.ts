import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
// @ts-ignore
import xss from 'xss-clean';
import authRoutes from './routes/auth';
import userRoutes from './routes/userRoutes';
import chatRoutes from './routes/chatRoutes';
const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(xss());

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
});
app.use(limiter);

app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/chats', chatRoutes);

app.get('/', (req, res) => {
  res.send('API is running...');
});

export default app;
