-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keep public class com.google.googlenav.capabilities.CapabilitiesController*

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class * implements android.os.Parcelable {
    static *** CREATOR;
}

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/simplification/variable