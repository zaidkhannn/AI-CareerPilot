<div align="center">

# 🚀 AI CareerPilot

### AI-Powered Career Guidance & Placement Preparation App for Android

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![API](https://img.shields.io/badge/Min%20SDK-24-blue.svg)](https://developer.android.com)
[![Groq](https://img.shields.io/badge/AI-Groq%20LLaMA-purple.svg)](https://groq.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)



</div>

---

## 📱 Overview

**CareerPilot AI is a smart Android application designed to help students and job seekers prepare for their careers and secure better opportunities. Powered by Groq AI, the platform provides personalized career guidance, interview preparation, resume analysis, technical assessments, and an intelligent AI assistant that answers career-related questions in real time.

The application combines multiple career development tools into a single platform, enabling users to track their progress, improve their skills, prepare for interviews, and plan their journey toward their dream job.

---

## ✨ Features

| Feature | Description |
|--------|-------------|
| 🎙️ **Voice Interview** | Full Speech-to-Text + Text-to-Speech pipeline for natural voice interaction |
| 🤖 **AI Interviewer** | Powered by Groq's LLaMA 3.1 model for intelligent, contextual questions |
| 📈 **Adaptive Difficulty** | Smart progression from easy → medium → hard based on your responses |
| 💼 **FAANG-Style Simulation** | Interview patterns modeled after Google, Amazon, Meta, Apple, Netflix |
| 📊 **Real-time Feedback** | Instant AI-generated feedback on your answers |
| 🗺️ **Career Roadmap** | Personalized career path planning and guidance |
| 📝 **Resume Analyzer** | AI-assisted resume review and improvement suggestions |
| 🏆 **Tech Assessment** | Technical skill evaluation and practice |

---

## 🛠️ Tech Stack

- **Language**: Java
- **IDE**: Android Studio
- **AI**: [Groq API](https://groq.com) (LLaMA 3.1 8B Instant)
- **Voice Input**: Android `SpeechRecognizer` API
- **Voice Output**: Android `TextToSpeech` API
- **Networking**: OkHttp
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable version)
- Android device or emulator (API 24+)
- A [Groq API key](https://console.groq.com) (free tier available)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/zaidkhannn/AI-CareerPilot.git
   cd AI-CareerPilot
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File → Open** and navigate to the cloned folder

3. **Configure your Groq API Key** ⚠️

   Create or edit the `local.properties` file in the **root of the project** (same level as `build.gradle`):

   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   GROQ_API_KEY=your_groq_api_key_here
   ```

   > **How to get a Groq API key:**
   > 1. Visit [console.groq.com](https://console.groq.com)
   > 2. Sign up for a free account
   > 3. Navigate to **API Keys** and create a new key
   > 4. Copy and paste it into `local.properties`

4. **Sync and Run**
   - Click **Sync Project with Gradle Files** in Android Studio
   - Select your device/emulator
   - Click ▶️ **Run**

---

## 🔐 Security

This project follows Android security best practices:

- ✅ API keys are stored in `local.properties` (git-ignored)
- ✅ Keys are accessed via `BuildConfig` at compile time
- ✅ No secrets are hardcoded in source files
- ✅ `local.properties` is excluded from version control

**Never commit your `local.properties` file!** It is already added to `.gitignore`.

---

## 📁 Project Structure

```
AI-CareerPilot/
├── app/
│   └── src/main/java/com/techiguru/aicareerpilot/
│       ├── SplashActivity.java          # App entry point
│       ├── LoginActivity.java           # Authentication
│       ├── RegisterActivity.java        # User registration
│       ├── MainActivity.java            # Dashboard
│       ├── InterviewSetupActivity.java  # Interview configuration
│       ├── InterviewSimulatorActivity.java # Core voice interview engine
│       ├── AiTestActivity.java          # AI chat testing
│       ├── CareerRoadmapActivity.java   # Career path planning
│       ├── ResumeAnalyzerActivity.java  # Resume review
│       ├── TechAssessmentActivity.java  # Technical skills test
│       ├── DreamJobPlannerActivity.java # Job goal planning
│       ├── MentorConnectActivity.java   # Mentor matching
│       ├── ProjectSuggestionsActivity.java # Project ideas
│       ├── ProfileActivity.java         # User profile
│       └── GroqApiService.java          # Groq AI integration layer
├── local.properties                     # ⚠️ NOT committed (add your API key here)
├── .gitignore
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Zaid Khan** — [@zaidkhannn](https://github.com/zaidkhannn)

---

<div align="center">
  <i>Built with ❤️ to help developers ace their interviews</i>
</div>
