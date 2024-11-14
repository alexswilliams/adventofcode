import common.TwoPartChallenge
import org.junit.platform.commons.util.ClassFilter
import org.junit.platform.commons.util.ReflectionUtils
import kotlin.streams.asSequence

fun allChallengesUnder(pkgName: String): List<TwoPartChallenge> {
    return ReflectionUtils.streamAllClassesInPackage(pkgName, ClassFilter.of { TwoPartChallenge::class.java.isAssignableFrom(it) }).asSequence()
        .mapNotNull { it.kotlin.objectInstance }
        .filterIsInstance<TwoPartChallenge>()
        .sortedBy { it::class.simpleName?.replace("Day", "")?.toIntOrNull() ?: 0 }
        .toList()
}
