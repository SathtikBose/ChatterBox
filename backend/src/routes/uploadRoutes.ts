import express from 'express';
import { uploadImage } from '../controllers/uploadController';
import { protect } from '../middleware/authMiddleware';
import { upload } from '../config/cloudinary';

const router = express.Router();

router.post('/', protect, upload.single('image'), uploadImage);

export default router;
