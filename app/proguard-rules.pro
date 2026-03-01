# General attributes rules
-keepattributes SourceFile,LineNumberTable,Signature

# Repackage classes
-repackageclasses ''

# Keep necessary class members
-keepnames public class * extends android.app.Activity
-keepnames public class * extends android.app.Application