import { Request, Response } from 'express';
import Chat from '../models/Chat';
import Message from '../models/Message';
import User from '../models/User';

export const accessChat = async (req: Request, res: Response) => {
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ message: "UserId param not sent with request" });
  }

  // @ts-ignore
  const currentUserId = req.user._id;

  var isChat = await Chat.find({
    $and: [
      { participants: { $elemMatch: { $eq: currentUserId } } },
      { participants: { $elemMatch: { $eq: userId } } },
    ],
  })
    .populate("participants", "-password")
    .populate({
      path: "lastMessage",
      populate: {
        path: "chatId",
        populate: {
          path: "participants",
          select: "username profilePic email isOnline lastOnline"
        }
      }
    });

  // @ts-ignore
  isChat = await User.populate(isChat, {
    path: "lastMessage.sender",
    select: "username profilePic email isOnline lastOnline",
  });

  if (isChat.length > 0) {
    res.send(isChat[0]);
  } else {
    var chatData = {
      participants: [currentUserId, userId],
    };

    try {
      const createdChat = await Chat.create(chatData);
      const FullChat = await Chat.findOne({ _id: createdChat._id }).populate(
        "participants",
        "-password"
      );
      res.status(200).json(FullChat);
    } catch (error: any) {
      res.status(400).json({ message: error.message });
    }
  }
};

export const fetchChats = async (req: Request, res: Response) => {
  try {
    // @ts-ignore
    Chat.find({ participants: { $elemMatch: { $eq: req.user._id } } })
      .populate("participants", "-password")
      .populate({
        path: "lastMessage",
        populate: {
          path: "chatId",
          populate: {
            path: "participants",
            select: "username profilePic email isOnline lastOnline"
          }
        }
      })
      .sort({ updatedAt: -1 })
      .then(async (results: any) => {
        // @ts-ignore
        results = await User.populate(results, {
          path: "lastMessage.sender",
          select: "username profilePic email isOnline lastOnline",
        });
        res.status(200).send(results);
      });
  } catch (error: any) {
    res.status(400).json({ message: error.message });
  }
};

export const sendMessage = async (req: Request, res: Response) => {
  const { content, chatId, imageUrl } = req.body;

  if ((!content && !imageUrl) || !chatId) {
    return res.status(400).json({ message: "Invalid data passed into request" });
  }

  // @ts-ignore
  const currentUserId = req.user._id;

  var newMessage = {
    sender: currentUserId,
    text: content,
    imageUrl: imageUrl,
    chatId: chatId,
  };

  try {
    var message = await Message.create(newMessage);

    message = await message.populate("sender", "username profilePic");
    message = await message.populate("chatId");
    // @ts-ignore
    message = await User.populate(message, {
      path: "chatId.participants",
      select: "username profilePic email isOnline lastOnline",
    });

    await Chat.findByIdAndUpdate(req.body.chatId, { lastMessage: message });

    res.json(message);
  } catch (error: any) {
    res.status(400).json({ message: error.message });
  }
};

export const allMessages = async (req: Request, res: Response) => {
  try {
    const messages = await Message.find({ chatId: req.params.chatId })
      .populate("sender", "username profilePic email isOnline lastOnline")
      .populate({
        path: "chatId",
        populate: {
          path: "participants",
          select: "username profilePic email isOnline lastOnline"
        }
      });
    res.json(messages);
  } catch (error: any) {
    res.status(400).json({ message: error.message });
  }
};
