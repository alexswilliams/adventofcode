import org.junit.platform.commons.util.*

inline fun <reified T> allChallengesUnder(pkgName: String): List<T> {
    return ReflectionUtils.streamAllClassesInPackage(pkgName, ClassFilter.of { T::class.java.isAssignableFrom(it) }).toList()
        .mapNotNull { it.kotlin.objectInstance }
        .filterIsInstance<T>()
        .sortedBy { it::class.simpleName?.replace("Day", "")?.toIntOrNull() ?: 0 }
}
