import { Request, Response } from 'express';

export const uploadImage = (req: Request, res: Response) => {
  try {
    if (!req.file) {
      return res.status(400).json({ message: 'No file uploaded' });
    }
    // multer-storage-cloudinary automatically uploads the file and adds 'path' to req.file
    res.json({ imageUrl: req.file.path });
  } catch (error: any) {
    res.status(500).json({ message: error.message });
  }
};
