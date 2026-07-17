import express from 'express';
import { registerUser, loginUser, forgotPassword, verifyOTPAndResetPassword } from '../controllers/authController';

const router = express.Router();

router.post('/register', registerUser);
router.post('/login', loginUser);
router.post('/forgot-password', forgotPassword);
router.post('/reset-password', verifyOTPAndResetPassword);

export default router;
