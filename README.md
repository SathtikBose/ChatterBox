# ChatterBox

ChatterBox is a modern, real-time, full-stack chat application designed with a sleek UI and powerful backend architecture. It features instant messaging, photo sharing, user authentication, and a WhatsApp-like reply system.

## 🚀 Features

### Mobile App (Android)
- **Modern UI/UX**: Built entirely with Jetpack Compose featuring a beautiful neon-themed Light/Dark mode.
- **Real-time Messaging**: Instant message delivery using Socket.io.
- **Rich Media**: Send and receive photos (integrated with Cloudinary and Coil).
- **Advanced Chat Features**:
  - Swipe-to-reply with visual context indicators (WhatsApp style).
  - Online/Offline status indicators.
  - "Typing..." indicators.
- **User Management**:
  - Secure login/signup.
  - Update profile pictures and passwords.
  - Block/unblock users.

### Backend (Node.js)
- **RESTful API**: Built with Express.js and TypeScript.
- **Real-time Engine**: Socket.io for bi-directional communication.
- **Database**: MongoDB with Mongoose for structured, scalable data storage.
- **Security**: 
  - JWT (JSON Web Tokens) for secure authentication.
  - Passwords hashed via bcryptjs.
  - Rate limiting and Helmet for API protection.
- **Storage**: Cloudinary integration for handling profile pictures and chat image uploads.

## 🛠️ Tech Stack

### Frontend
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Networking**: Retrofit2 & OkHttp
- **Real-time**: Socket.io-client
- **Image Loading**: Coil
- **Architecture**: MVVM (Model-View-ViewModel)

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Language**: TypeScript
- **Database**: MongoDB
- **Real-time**: Socket.io
- **Authentication**: JWT & bcryptjs
- **Cloud Storage**: Cloudinary & Multer

## 📦 Getting Started

### Prerequisites
- Android Studio (for the frontend)
- Node.js (v18+)
- MongoDB connection string
- Cloudinary account credentials

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file in the `backend` directory based on `.env.example` and add your credentials:
   ```env
   PORT=5000
   MONGO_URI=your_mongodb_uri
   JWT_SECRET=your_jwt_secret
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   ```
4. Start the development server:
   ```bash
   npm run dev
   ```

### Frontend Setup
1. Open the `frontend` folder in **Android Studio**.
2. Sync the Gradle files.
3. Update the backend API base URL in `ApiService.kt` and `SocketManager.kt` to point to your local backend (e.g., `http://10.0.2.2:5000` for emulator) or your deployed Render URL.
4. Build and run the app on an emulator or physical device.

## 📄 License
This project is licensed under the MIT License.
