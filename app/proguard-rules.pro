##################################
# 🛡️ Safe Proguard Rules for Compose + Lifecycle
##################################

# Compose Runtime (essential)
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Material3 (essential UI components)
-keep class androidx.compose.material3.** { *; }

# Lifecycle & ViewTree (to avoid LocalLifecycleOwner crash)
-keep class androidx.lifecycle.ViewTreeLifecycleOwner { *; }
-keep class androidx.lifecycle.ViewTreeViewModelStoreOwner { *; }

# Your app's main entry (optional but useful)
-keep class com.spender.calculator.MainActivity { *; }

# Optional: Allow R8 to remove logs
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
