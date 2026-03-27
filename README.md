# 🎙️ Silent Mode — Personalized Voice Responder

An Android app that automatically replies to missed calls with your personal voice message via SMS — so callers are never left wondering.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🎙️ Voice Recording | Record a personal voice message as your auto-reply |
| 📲 Auto SMS | Sends a Cloudinary-hosted audio link via SMS on missed calls |
| 💬 Transcription | AssemblyAI transcribes your voice and sends the text alongside the link |
| 🌐 Translation | Auto-translates the transcription (EN ↔ HI) |
| 👥 Contact Whitelist | Only selected contacts trigger auto-replies |
| ⚡ Pre-caching | Transcription happens at record time — SMS text sends instantly |
| 🔔 FCM Push | Sends push notifications to app users via Firebase Cloud Messaging |
| 🔒 Firestore Security | Per-UID Firestore rules — no cross-user data access |
| 🗑️ Auto-Delete | Cloudinary uploads auto-delete after 24 hours via WorkManager |
| 📊 Feedback | User feedback stored in Firestore under `/users/{uid}/feedback/` |

---

## 🏗️ Architecture

```
app/
├── audio/          # AudioRecorder
├── data/           # Room DB (contacts, feedback, cached_response)
├── remote/         # CloudinaryHelper, FirebaseHelper, SmsHelper,
│                   # TranscriptionHelper (AssemblyAI), TranslationHelper
├── service/        # ResponderService (missed call handler)
│                   # DeleteAudioWorker (WorkManager 24h cleanup)
└── ui/             # Compose screens
    ├── SplashScreen
    ├── LoginScreen / SignUpScreen / SetupPhoneScreen
    ├── OnboardingScreen      ← first-time feature tour
    ├── DashboardScreen       ← home + responder toggle
    ├── RecordAudioScreen     ← record + pre-transcribe
    ├── ContactListScreen     ← whitelist management
    └── SettingsScreen        ← preferences + feedback
```

---

## 🔧 Setup

### 1. Firebase

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package `com.example.voiceresponder`
3. Download `google-services.json` → place in `app/`
4. Enable **Email/Password** auth in Firebase Authentication
5. Create a **Firestore** database (region: `asia-south1`)
6. Apply Firestore security rules:
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /feedback/{doc} {
        allow write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### 2. Cloudinary

1. Create a free account at [cloudinary.com](https://cloudinary.com)
2. Note your **Cloud Name**, **API Key**, **API Secret**
3. Add to `app/src/main/kotlin/.../remote/Keys.kt`:
```kotlin
const val CLOUDINARY_CLOUD_NAME = "your_cloud_name"
const val CLOUDINARY_API_KEY    = "your_api_key"
const val CLOUDINARY_API_SECRET = "your_api_secret"
```

### 3. AssemblyAI

1. Get an API key at [assemblyai.com](https://www.assemblyai.com)
2. Add to `Keys.kt`:
```kotlin
const val ASSEMBLY_AI_KEY = "your_assemblyai_key"
```

### 4. Google Translate (Translation API)

Add your Google Cloud Translate API key to `Keys.kt`:
```kotlin
const val TRANSLATE_API_KEY = "your_translate_key"
```

---

## 🚀 Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew --stop && ./gradlew clean
```

APK output: `app/build/outputs/apk/release/app-release.apk`

---

## 📱 How It Works

1. **Record** your voice message in the app
2. App **transcribes + translates** it in the background and caches the result
3. **Select contacts** who should receive auto-replies
4. **Enable** the Auto Responder toggle on the Home screen
5. When a selected contact calls and you miss it:
   - SMS 1: `"📞 I missed your call. 🎙 Voice Message: https://..."`
   - SMS 2: `"💬 Hello, I missed your call..."` *(from pre-cached transcription)*

---

## 📦 Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Firebase** Auth + Firestore + FCM
- **Room** (local database)
- **WorkManager** (background tasks)
- **Cloudinary** (audio file hosting)
- **AssemblyAI** (speech-to-text)
- **Google Translate API** (EN ↔ HI translation)
- **OkHttp** (HTTP client)

---

## 📄 License

MIT License — free to use and modify.
