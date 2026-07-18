import { Request, Response } from 'express';
import User from '../models/User';

export const searchUsers = async (req: Request, res: Response) => {
  try {
    const searchQuery = req.query.search || req.query.query;
    if (!searchQuery) return res.status(400).json({ message: 'Search term is required' });

    // @ts-ignore
    const currentUserId = req.user._id;

    const users = await User.find({
      username: { $regex: searchQuery, $options: 'i' },
      _id: { $ne: currentUserId }
    } as any).select('-password');

    res.json(users);
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const blockUser = async (req: Request, res: Response) => {
  try {
    const { userIdToBlock } = req.body;
    // @ts-ignore
    const currentUserId = req.user._id;

    const user = await User.findById(currentUserId);
    if (!user) return res.status(404).json({ message: 'User not found' });

    if (!user.blockedUsers.includes(userIdToBlock)) {
      user.blockedUsers.push(userIdToBlock);
      await user.save();
    }

    res.json({ message: 'User blocked successfully' });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const updateProfile = async (req: Request, res: Response) => {
  try {
    // @ts-ignore
    const currentUserId = req.user._id;
    const { username, profilePic } = req.body;

    const user = await User.findById(currentUserId);
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    if (username) user.username = username;
    if (profilePic) user.profilePic = profilePic;

    await user.save();

    res.json({
      _id: user._id,
      username: user.username,
      email: user.email,
      profilePic: user.profilePic,
    });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};

export const changePassword = async (req: Request, res: Response) => {
  try {
    // @ts-ignore
    const currentUserId = req.user._id;
    const { oldPassword, newPassword } = req.body;

    if (!oldPassword || !newPassword) {
      return res.status(400).json({ message: 'Please provide both old and new passwords' });
    }

    const user = await User.findById(currentUserId);
    if (!user) return res.status(404).json({ message: 'User not found' });

    // @ts-ignore
    const isMatch = await user.matchPassword(oldPassword);
    if (!isMatch) return res.status(401).json({ message: 'Invalid old password' });

    user.password = newPassword;
    await user.save();

    res.json({ message: 'Password updated successfully' });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};
