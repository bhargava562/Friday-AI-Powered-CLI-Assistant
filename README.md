# 🤖 Friday – Your Personal AI Assistant for PC 🧠💻

> Designed to assist, remind, inform, and empower.  
> Built with ❤️ using **Java + Spring Boot + AI Tools**.

---

## ✨ Features at a Glance

| Feature        | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| 🌦️ Weather      | Get real-time weather updates using OpenWeatherMap                         |
| 🕒 Time         | Shows you the current time instantly                                        |
| 🗞 News         | Top headlines delivered from GNews                                          |
| 😂 Jokes        | Lighten your mood with random jokes                                         |
| 💹 Stocks       | Track stock prices live via Alpha Vantage                                   |
| 📚 Dictionary   | Find meanings and definitions instantly                                     |
| 💡 Knowledge    | Ask factual questions, solved using Wolfram Alpha                          |
| 💱 Currency     | Convert currencies with live exchange rates                                |
| 🔔 Reminders    | Set one-time or daily reminders with **desktop notifications**              |
| 📧 Email        | Send emails using simple prompts (subject, recipient, description)         |
| 🚀 App Launcher | Launch installed applications like Chrome, VLC, Word etc. via voice/text   |
| 📅 Calendar     | *(Planned)* Google Calendar CRUD integration                                |

---

## 🧠 Powered By

| Tech / API                 | Role                         |
|---------------------------|------------------------------|
| `Spring Boot`             | Backend Framework            |
| `Gemini AI`               | AI & NLP Response Engine     |
| `GNews API`               | News Data                    |
| `OpenWeatherMap`          | Weather Forecasts            |
| `Wolfram Alpha`           | General Knowledge Answers    |
| `Alpha Vantage`           | Stock Market Data            |
| `BurntToast (PowerShell)` | Desktop Reminder Notifications |
| `Spring Mail`             | SMTP Email Sending           |

---

## 🗣️ How to Talk to Friday

Just type natural language queries like:
weather in chennai
set reminder to drink water at 8:00 pm
tell me a joke
convert 5 usd to inr
mail to "someone@example.com" subject: "Hello" description: "This is from Friday"
launch chrome
what is the capital of japan


Friday will understand and do the job. ✅

---

## 📁 Reminder System

- Reminders are saved in a `reminders.json` file.
- Desktop notifications are sent **even when Friday is not running**, using a background **Task Scheduler** + `ReminderRunner`.
- Supports:
  - One-time reminders ✅
  - Daily recurring reminders ✅

---

## 🖥️ Designed For

- **Windows 10/11**
- With **Java 17+** installed
- Desktop environment with PowerShell

---

## 🧩 Architecture Overview

+---------------------+
| User Query (CLI) |
+---------------------+
|
v
+-------------------------+
| FridayService.java |
+-------------------------+
| | | |
v v v v
NLP Tools Gemini Mail
Router (10+) Fallback API

---

## 🔐 Security & Storage

- Reminders stored in JSON, path-configurable  
- Environment variables used to secure API Keys  
- Gmail credentials stored securely in `application.properties`  

---

## 🎯 What's Coming Next?

- [ ] ✅ Full Google Calendar integration  
- [ ] 🧠 Learn your app usage pattern  
- [ ] 🪄 Voice Command Support  
- [ ] 📊 Analytics Dashboard for Friday  

---

## 🧬 Tech Stack

- Java 17+
- Spring Boot
- Maven
- Jackson (JSON)
- PowerShell (for notifications)
- AI via Gemini + APIs

---

## 🔎 Project Inspiration

Inspired by **Iron Man’s Friday** – to make your PC smarter, proactive and more helpful through automation and AI.

---

🧠 *Built by Bhargava.A — because your computer should talk back to you.*
