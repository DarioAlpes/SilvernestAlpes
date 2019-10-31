-dontwarn kotlin.**
-dontnote kotlin.**

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}