import { Request, Response } from 'express';
import User from '../models/User';

export const searchUsers = async (req: Request, res: Response) => {
  try {
    const { query } = req.query;
    if (!query) return res.status(400).json({ message: 'Query is required' });

    // @ts-ignore
    const currentUserId = req.user._id;

    const users = await User.find({
      username: { $regex: query, $options: 'i' },
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
