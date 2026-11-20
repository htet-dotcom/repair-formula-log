# Repair Formula Log

ဖုန်း Motherboard repair case မှတ်တမ်း app — Shortcut formula ထုတ်ပေးပြီး Gemini prompt ရေးပေးသည်။

- `index.html` ကို browser နဲ့ ဖွင့်ရုံပါပဲ (file တစ်ခုတည်း)
- Claude artifact အဖြစ်ဖွင့်ရင် online မှတ်တမ်း (db) သုံးမယ်၊ ကျန်တဲ့နေရာမှာ browser localStorage ထဲ သိမ်းမယ်
- GitHub Pages (https://htet-dotcom.github.io/repair-formula-log/) မှာဖွင့်ရင် **Google Drive Sync** သုံးလို့ရ — Drive ထဲမှာ `repair-formula-log.json` ဖိုင်အဖြစ် သိမ်းမယ်
  - Google Cloud OAuth Web Client ID လို (Authorized JavaScript origin: `https://htet-dotcom.github.io`)
  - `index.html` ထဲက `GD_CLIENT_ID` မှာ ထည့်ထားရင် ဖုန်းတိုင်းမှာ ထပ်ထည့်စရာမလို
- PWA: ဖုန်း Chrome မှာ ⋮ → "Add to Home screen / Install app" နှိပ်ရင် app icon နဲ့ ဖွင့်လို့ရ၊ အင်တာနက်မရှိလည်း ပွင့်တယ်

## Android app (APK)

`android/` ထဲမှာ native Android app project ရှိတယ် — `index.html` ကို app ထဲ ထည့်ထားလို့ အင်တာနက်မရှိလည်း ပွင့်တယ်။

- Build: `cd android && ./gradlew assembleRelease` → `android/app/build/outputs/apk/release/app-release.apk`
  (build တိုင်း root က `index.html` + `icons/` ကို app ထဲ အလိုလို ကူးထည့်တယ် — web ကို ပြင်ရင် app ကို ပြန် build ရုံပဲ)
- Release signing: keystore properties ဖိုင် (storeFile, storePassword, keyAlias, keyPassword) ကို repo အပြင်မှာထားပြီး
  `RFL_SIGNING=<path>` env var နဲ့ build ပါ။ မထည့်ရင် debug key နဲ့ sign လုပ်တယ်။
- ပုံ Save → Gallery ထဲက `Pictures/RepairLog`၊ ပုံထည့် → ဖုန်း file/gallery picker၊ Gemini link → browser မှာ ပွင့်တယ်။
- **Google Drive Sync (app ထဲမှာ)**: Google က app ထဲမှာ web sign-in popup ကို ပိတ်ထားလို့ Android native sign-in သုံးတယ်။
  Google Cloud Console (Web Client ID ရှိတဲ့ project တစ်ခုတည်း) → Credentials → Create OAuth client ID → **Android**
  - Package name: `com.htet.repairlog`
  - SHA-1: APK ကို sign လုပ်တဲ့ key ရဲ့ SHA-1 (`keytool -list -v -keystore <keystore>`)
  ဒါလုပ်ပြီးရင် app ထဲက "Google Drive နဲ့ Sync ချိတ်" ခလုတ်က အလုပ်လုပ်မယ်၊ web နဲ့ တူတဲ့ `repair-formula-log.json` ကို share သုံးမယ်။
