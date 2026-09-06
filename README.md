# PlayerTime ⏱️

An advanced, high-performance, and lightweight playtime tracking plugin built specifically for modern Minecraft servers running **Paper 26.2** (or higher) and **Java 25**.

Unlike simple counters, **PlayerTime** breaks down player activity into detailed historical periods, allowing server administrators and players to track exactly how much time is spent on the server.

---

## ⭐ Features

* 📊 **Detailed Statistics:** Tracks playtime dynamically and categorizes it into **Today**, **This Week**, **This Month**, **This Year**, and **Total**.
* 🚀 **Modern Core:** Fully built using the modern **Paper API** and **Kyori Adventure** text engine for flawless performance.
* 💾 **Offline Caching:** Saves and reads data for offline players without causing single-tick server lag.
* 🔄 **Automatic Resets:** Smart internal date tracking automatically resets daily, weekly, monthly, and yearly stats exactly when midnight strikes.
* 🔒 **Asynchronous Saving:** Periodically flushes all player statistics to `userdata.yml` safely in a background thread.
* 🛰️ **Built-in Updater:** Features an asynchronous Update Checker that links directly to the GitHub Releases API.

---

## 🎮 Commands & Aliases

* `/playertime` *(Alias: `/pt`)* — Shows plugin information and correct command usage.
* `/playertime <player>` — Displays a full detailed playtime breakdown for the specified player.
* `/playertime <player> <day/week/month/year/all>` — Checks a specific time period for a player.

---

## 🔑 Permissions

* `playertime.use` — Allows players to use the command and check stats (Granted to OP by default). 
* *Fully compatible with **LuckPerms**!*

---

## 📋 Requirements

* **Server Core:** Paper 26.2 build.
* **Java Version:** Java 25

---

## 🛠️ Installation Guide

1. Download the latest compiled binary (`PlayerTime-1.0.jar`) from the [Releases](https://github.com/amongopin/PlayerTime/releases/tag/1.0) section.
2. Drop the `.jar` file into your server's `plugins` folder.
3. Restart your server. Do not use `/reload` as it can break background data loops (if your server was offline, just start it).
4. Configure permissions via your preferred permission manager (e.g., `/lp group default permission set playertime.use true`).

---

## 👥 Support & Bug Reports

If you encounter any bugs, errors, or have suggestions for new features, please open an official ticket in our [GitHub Issues](https://github.com/amongopin/PlayerTime/issues) tracker.
WARNING! THIS PLUGIN WAS CREATED ONLY FOR 26.2 PAPER! DON'T TRY TO USE THIS PLUGIN ON OTHER VERSIONS OF THE PAPER!!!
