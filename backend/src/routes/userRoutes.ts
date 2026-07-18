import express from 'express';
import { searchUsers, blockUser, updateProfile } from '../controllers/userController';
import { protect } from '../middleware/authMiddleware';

const router = express.Router();

router.route('/').get(protect, searchUsers);
router.route('/profile').put(protect, updateProfile);
router.route('/block').post(protect, blockUser);

export default router;
