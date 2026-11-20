# Repair Formula Log

ဖုန်း Motherboard repair case မှတ်တမ်း app — Shortcut formula ထုတ်ပေးပြီး Gemini prompt ရေးပေးသည်။

- `index.html` ကို browser နဲ့ ဖွင့်ရုံပါပဲ (file တစ်ခုတည်း)
- Claude artifact အဖြစ်ဖွင့်ရင် online မှတ်တမ်း (db) သုံးမယ်၊ ကျန်တဲ့နေရာမှာ browser localStorage ထဲ သိမ်းမယ်
- GitHub Pages (https://htet-dotcom.github.io/repair-formula-log/) မှာဖွင့်ရင် **Google Drive Sync** သုံးလို့ရ — Drive ထဲမှာ `repair-formula-log.json` ဖိုင်အဖြစ် သိမ်းမယ်
  - Google Cloud OAuth Web Client ID လို (Authorized JavaScript origin: `https://htet-dotcom.github.io`)
  - `index.html` ထဲက `GD_CLIENT_ID` မှာ ထည့်ထားရင် ဖုန်းတိုင်းမှာ ထပ်ထည့်စရာမလို
