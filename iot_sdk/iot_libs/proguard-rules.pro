-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
#继承activity,application,service,broadcastReceiver,contentprovider....不进行混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#这个主要是在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes Exceptions,InnerClasses,...

-keep class **.R$* {
 *;
}

-keepclassmembers class * {
    void *(*Event);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#// natvie 方法不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#----------------------------------------------------------------------------

#----------------------------------------------------------------------------
#---------------------------------------------------------------------------------------------------
#---------------------------------实体类---------------------------------
-keep class com.abupdate.iot_libs.info.** { *; }
-keep class com.abupdate.iot_libs.policy.** { *; }
-keep class com.abupdate.iot_libs.constant.** { *; }
-keep class com.abupdate.iot_libs.OtaAgentPolicy { *; }
-keep class com.abupdate.iot_libs.MqttAgentPolicy { *; }
-keep class com.abupdate.iot_libs.IndirectOtaAgentPolicy { *; }
-keep class com.abupdate.iot_libs.service.OtaService { public *; }
-keep class com.abupdate.iot_libs.utils.SPFTool { *; }
-keep class com.abupdate.iot_libs.report.ReportManager { *; }
-keep class com.abupdate.iot_libs.engine.LogManager { *; }

-keep class com.abupdate.iot_libs.OtaAgentPolicy$Builder{public *;}

##接口部分
#-keep class ICheckVersionCallback { *; }
#-keep class IDownloadListener { *; }
#-keep class IRebootUpgradeCallBack { *; }
#-keep class IRegisterListener { *; }
#-keep class IReportResultCallback { *; }
#-keep class ILoginCallback { *; }
#-keep class ILogoutCallback { *; }
#-keep class IReportDeviceStatusCallback { *; }
#-keep class MessageListener { *; }
#-keep class OtaListener { *; }
#-keep class IStatusListener { *; }
-keep class com.abupdate.iot_libs.inter.**{*;}

-keep class com.abupdate.iot_libs.security.FotaException { *; }


# support-v4
-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }


# support-v7
-dontwarn android.support.v7.**
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }
