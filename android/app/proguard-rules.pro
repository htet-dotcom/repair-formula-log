# JavaScript bridge methods are called by name from the page.
-keepclassmembers class com.htet.repairlog.WebBridge {
    @android.webkit.JavascriptInterface <methods>;
}
