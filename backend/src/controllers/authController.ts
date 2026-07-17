import { Request, Response } from 'express';
import User from '../models/User';
import generateToken from '../utils/generateToken';
import { Resend } from 'resend';

// Initialize Resend
const resend = new Resend(process.env.RESEND_API_KEY || 're_123');

export const registerUser = async (req: Request, res: Response) => {
  try {
    const { username, email, password } = req.body;

    const userExists = await User.findOne({ $or: [{ email }, { username }] });

    if (userExists) {
      return res.status(400).json({ message: 'User already exists' });
    }

    const user = await User.create({
      username,
      email,
      password,
    });

    if (user) {
      res.status(201).json({
        _id: user._id,
        username: user.username,
        email: user.email,
        profilePic: user.profilePic,
        token: generateToken(user._id.toString()),
      });
    } else {
      res.status(400).json({ message: 'Invalid user data' });
    }
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const loginUser = async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    const user: any = await User.findOne({ email });

    if (user && (await user.matchPassword(password))) {
      res.json({
        _id: user._id,
        username: user.username,
        email: user.email,
        profilePic: user.profilePic,
        token: generateToken(user._id.toString()),
      });
    } else {
      res.status(401).json({ message: 'Invalid email or password' });
    }
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const forgotPassword = async (req: Request, res: Response) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });

    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    const otpCode = Math.floor(100000 + Math.random() * 900000).toString();
    user.otp = {
      code: otpCode,
      expiresAt: new Date(Date.now() + 10 * 60 * 1000) // 10 mins
    };
    await user.save();

    await resend.emails.send({
      from: 'onboarding@resend.dev',
      to: user.email,
      subject: 'Chatter Box Password Reset OTP',
      html: `<p>Your OTP for password reset is: <strong>${otpCode}</strong></p>`
    });

    res.json({ message: 'OTP sent to email' });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const verifyOTPAndResetPassword = async (req: Request, res: Response) => {
  try {
    const { email, otp, newPassword } = req.body;
    const user = await User.findOne({ email });

    if (!user || !user.otp || !user.otp.code || !user.otp.expiresAt) {
      return res.status(400).json({ message: 'Invalid request' });
    }

    if (user.otp.code !== otp) {
      return res.status(400).json({ message: 'Invalid OTP' });
    }

    if (new Date() > user.otp.expiresAt) {
      return res.status(400).json({ message: 'OTP expired' });
    }

    user.password = newPassword;
    user.otp = { code: undefined, expiresAt: undefined };
    await user.save();

    res.json({ message: 'Password reset successful' });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};
