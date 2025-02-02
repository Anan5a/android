// INTENTION_TEXT: Add @RequiresApi(LOLLIPOP) Annotation
// INSPECTION_CLASS: com.android.tools.idea.lint.inspections.AndroidLintNewApiInspection
// DEPENDENCY: RequiresApi.java -> android/support/annotation/RequiresApi.java
// INTENTION_NOT_AVAILABLE

import android.graphics.drawable.VectorDrawable
import kotlin.reflect.KClass

annotation class SomeAnnotationWithClass(val cls: KClass<*>)

@SomeAnnotationWithClass(<caret>VectorDrawable::class)
class VectorDrawableProvider {
}