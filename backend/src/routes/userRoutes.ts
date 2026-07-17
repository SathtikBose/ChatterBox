import express from 'express';
import { searchUsers, blockUser } from '../controllers/userController';
import { protect } from '../middleware/authMiddleware';

const router = express.Router();

router.route('/').get(protect, searchUsers);
router.route('/block').post(protect, blockUser);

export default router;
