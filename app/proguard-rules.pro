# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep public class android.** {*;}
-keep public class com.rosan.dhizuku.App {*;}
-keep public class com.rosan.dhizuku.ui.activity.** extends android.app.Activity
-keep public class com.rosan.dhizuku.data.process.model.impl.** {
    public static void main(java.lang.String[]);
}
#-keep public class com.rosan.installer.data.process.model.impl.** extends com.rosan.dhizuku.data.process.repo.ProcessRepo {
#public static void main(java.lang.String[]);
#}
#-keep public class com.rosan.installer.** extends android.app.Service
#-keep public class com.rosan.installer.** extends android.content.BroadcastReceiver
#-keep public class com.rosan.installer.** extends android.content.ContentProvider
#-keep class androidx.core.content.FileProvider {*;}
#-keep interface androidx.core.content.FileProvider$PathStrategy {*;}

-keep class rikka.shizuku.ShizukuProvider

-dontwarn **