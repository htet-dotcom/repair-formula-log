# Android app (APK)

GitHub Pages app ကို Chrome ပေါ်မှာ app အဖြစ်ဖွင့်ပေးတဲ့ Trusted Web Activity (TWA)။
App code ကို `../index.html` ကနေပဲ ယူတာမို့ web ကို ပြင်တိုင်း APK ပြန်ထုတ်စရာမလို။

- ထည့်ရန်: `dist/RepairLog.apk` ကို ဖုန်းမှာ download → ဖွင့် → Install (Unknown apps ခွင့်ပြုရမယ်)
- Chrome ဖုန်းထဲမှာ ရှိရမယ်
- URL bar ဖျောက်ချင်ရင် `assetlinks.json` ကို `https://htet-dotcom.github.io/.well-known/assetlinks.json` မှာ တင်ရမယ်
  (GitHub မှာ `htet-dotcom.github.io` ဆိုတဲ့ repo လုပ်ပြီး `.well-known/assetlinks.json` ထည့်)

ပြန် build ရန် (Android SDK + JDK 17+):

```
echo sdk.dir=/path/to/android-sdk > local.properties
RL_KEYSTORE=/path/repairlog.keystore RL_KS_PASS=... gradle assembleRelease
```
Update ထုတ်ရင် `app/build.gradle` ထဲက `versionCode` ကို +1 တိုးပြီး **keystore အဟောင်းနဲ့ပဲ** sign ရမယ်။
