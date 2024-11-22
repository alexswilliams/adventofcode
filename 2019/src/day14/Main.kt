package day14

import common.*
import kotlin.math.*
import kotlin.test.*

data class Formula(val consumes: Chemicals, val produces: String, val productionQuantity: Long)
data class RequiredChemicals(val required: Chemicals, val waste: Chemicals)
typealias Chemicals = Map<String, Long>


fun main() {
    runTests()

    val formulae = "day14/input.txt".fromClasspathFileToLines().asFormulae()

    val (oreRequired, _) = formulae.reduceToOre(mapOf("FUEL" to 1L))
    println("Part 1: Ore required for 1 fuel: $oreRequired")
    assertEquals(1_920_219, oreRequired)

    val fuelProduced = formulae.oreToFuel()
    println("Part 2: Fuel produced: $fuelProduced")
    assertEquals(1_330_066, fuelProduced)
}


private fun Set<Formula>.oreToFuel(oreQuantity: Long = 1_000_000_000_000L): Long {
    tailrec fun evaluateHighestNotExceeding(range: LongRange): Long {
        val midpoint = range.midpoint
        val (oreUsed, _) = reduceToOre(mapOf("FUEL" to midpoint))

        //println("Ore to Fuel: range: $range, midpoint: $midpoint, oreUsed: $oreUsed")

        if (range.size < 2) {
            return if (oreUsed > oreQuantity) (midpoint - 1)
            else midpoint
        }

        return if (oreUsed > oreQuantity) evaluateHighestNotExceeding(range.first..midpoint)
        else evaluateHighestNotExceeding(midpoint..range.last)
    }

    return evaluateHighestNotExceeding(1L..oreQuantity)
}

private val LongRange.midpoint: Long get() = (first + last) / 2
private val LongRange.size: Long get() = (last - first).absoluteValue


private tailrec fun Set<Formula>.reduceToOre(
    chemicals: Chemicals,
    waste: Chemicals = emptyMap()
): Pair<Long, Chemicals> {
    val oreCount = chemicals["ORE"]
    if (chemicals.size == 1 && oreCount != null) return oreCount to waste

    // Pick a chemical at random
    val anyChemical = chemicals.keys.first { it != "ORE" }
    val requiredQuantity = chemicals[anyChemical] ?: 0

    // Take as much of it out of the waste bucket as we can
    val fromWaste = waste[anyChemical] ?: 0
    val adjustedQuantity = (requiredQuantity - fromWaste).coerceAtLeast(0)
    val adjustedWaste = when (adjustedQuantity) {
        requiredQuantity -> waste
        0L -> waste.minus(anyChemical).plus(anyChemical to (fromWaste - requiredQuantity))
        else -> waste.minus(anyChemical)
    }

    return if (adjustedQuantity == 0L) {
        // If it all came from waste, remove the chemical from the list of chemicals and continue
        reduceToOre(chemicals.minus(anyChemical), adjustedWaste)
    } else {
        // Otherwise work out how to build that chemical, and add those requirements to the work list
        val (requirements, nextWaste) = findConsumedChemicalsFor(anyChemical, adjustedQuantity)
        reduceToOre(
            chemicals.minus(anyChemical).mergeWith(requirements),
            adjustedWaste.mergeWith(nextWaste)
        )
    }
}

private fun Chemicals.mergeWith(other: Chemicals): Chemicals {
    if (other.isEmpty()) return this
    val (anyChemical, additionalQuantity) = other.entries.first()
    val existingQuantity = this[anyChemical] ?: 0
    return this.minus(anyChemical)
        .plus(anyChemical to (existingQuantity + additionalQuantity))
        .mergeWith(other.minus(anyChemical))
}


private fun Set<Formula>.findConsumedChemicalsFor(chemical: String, quantity: Long): RequiredChemicals {
    val producingFormula = this.first { it.produces == chemical }
    val requiredInvocations =
        (quantity + producingFormula.productionQuantity - 1) / producingFormula.productionQuantity

    return RequiredChemicals(
        required = producingFormula.consumes.mapValues { it.value * requiredInvocations },
        waste = ((producingFormula.productionQuantity * requiredInvocations) - quantity)
            .let { if (it == 0L) emptyMap() else mapOf(chemical to it) }
    )
}


private fun List<String>.asFormulae(): Set<Formula> = map(String::asFormula).toSet()

private fun String.asFormula(): Formula {
    val (lhs, rhs) = split("=>", limit = 2)
    val consumed = lhs.split(",")
    val produces = rhs.asChemical()
    return Formula(
        consumes = consumed.associate(String::asChemical),
        produces = produces.first,
        productionQuantity = produces.second
    )
}

private fun String.asChemical() =
    trim().split(" ", limit = 2)
        .let { (quantity, name) -> name.trim() to quantity.trim().toLong() }


private fun runTests() {
    val input1 = "10 ORE => 10 A"
    val input2 = """10 ORE => 10 A
        1 ORE => 1 B
        7 A, 1 B => 1 C
        7 A, 1 C => 1 D
        7 A, 1 D => 1 E
        7 A, 1 E => 1 FUEL""".trimIndent()
    val input3 = """9 ORE => 2 A
        8 ORE => 3 B
        7 ORE => 5 C
        3 A, 4 B => 1 AB
        5 B, 7 C => 1 BC
        4 C, 1 A => 1 CA
        2 AB, 3 BC, 4 CA => 1 FUEL""".trimIndent()
    val input4 = """157 ORE => 5 NZVS
        165 ORE => 6 DCFZ
        44 XJWVT, 5 KHKGT, 1 QDVJ, 29 NZVS, 9 GPVTF, 48 HKGWZ => 1 FUEL
        12 HKGWZ, 1 GPVTF, 8 PSHF => 9 QDVJ
        179 ORE => 7 PSHF
        177 ORE => 5 HKGWZ
        7 DCFZ, 7 PSHF => 2 XJWVT
        165 ORE => 2 GPVTF
        3 DCFZ, 7 NZVS, 5 HKGWZ, 10 PSHF => 8 KHKGT""".trimIndent()
    val input5 = """2 VPVL, 7 FWMGM, 2 CXFTF, 11 MNCFX => 1 STKFG
        17 NVRVD, 3 JNWZP => 8 VPVL
        53 STKFG, 6 MNCFX, 46 VJHF, 81 HVMC, 68 CXFTF, 25 GNMV => 1 FUEL
        22 VJHF, 37 MNCFX => 5 FWMGM
        139 ORE => 4 NVRVD
        144 ORE => 7 JNWZP
        5 MNCFX, 7 RFSQX, 2 FWMGM, 2 VPVL, 19 CXFTF => 3 HVMC
        5 VJHF, 7 MNCFX, 9 VPVL, 37 CXFTF => 6 GNMV
        145 ORE => 6 MNCFX
        1 NVRVD => 8 CXFTF
        1 VJHF, 6 MNCFX => 4 RFSQX
        176 ORE => 6 VJHF""".trimIndent()
    val input6 = """171 ORE => 8 CNZTR
        7 ZLQW, 3 BMBT, 9 XCVML, 26 XMNCP, 1 WPTQ, 2 MZWV, 1 RJRHP => 4 PLWSL
        114 ORE => 4 BHXH
        14 VRPVC => 6 BMBT
        6 BHXH, 18 KTJDG, 12 WPTQ, 7 PLWSL, 31 FHTLT, 37 ZDVW => 1 FUEL
        6 WPTQ, 2 BMBT, 8 ZLQW, 18 KTJDG, 1 XMNCP, 6 MZWV, 1 RJRHP => 6 FHTLT
        15 XDBXC, 2 LTCX, 1 VRPVC => 6 ZLQW
        13 WPTQ, 10 LTCX, 3 RJRHP, 14 XMNCP, 2 MZWV, 1 ZLQW => 1 ZDVW
        5 BMBT => 4 WPTQ
        189 ORE => 9 KTJDG
        1 MZWV, 17 XDBXC, 3 XCVML => 2 XMNCP
        12 VRPVC, 27 CNZTR => 2 XDBXC
        15 KTJDG, 12 BHXH => 5 XCVML
        3 BHXH, 2 VRPVC => 7 MZWV
        121 ORE => 7 VRPVC
        7 XCVML => 6 RJRHP
        5 BHXH, 4 VRPVC => 5 LTCX""".trimIndent()

    fun testFormulaParsing() {
        val parsed1 = input1.asFormula()
        assertEquals(Formula(mapOf("ORE" to 10L), "A", 10L), parsed1)
        val parsed2 = input2.lines().asFormulae()
        assertEquals(
            setOf(
                Formula(mapOf("ORE" to 10L), "A", 10L),
                Formula(mapOf("ORE" to 1L), "B", 1L),
                Formula(mapOf("A" to 7L, "B" to 1L), "C", 1L),
                Formula(mapOf("A" to 7L, "C" to 1L), "D", 1L),
                Formula(mapOf("A" to 7L, "D" to 1L), "E", 1L),
                Formula(mapOf("A" to 7L, "E" to 1L), "FUEL", 1L)
            ), parsed2
        )
    }
    testFormulaParsing()

    val parsed1 = input1.asFormula()
    val parsed2 = input2.lines().asFormulae()
    val parsed3 = input3.lines().asFormulae()
    val parsed4 = input4.lines().asFormulae()
    val parsed5 = input5.lines().asFormulae()
    val parsed6 = input6.lines().asFormulae()

    fun testChemicalRequirementSingleStep() {
        val requiredForC = parsed2.findConsumedChemicalsFor("C", 3L)
        assertEquals(RequiredChemicals(mapOf("A" to 21L, "B" to 3L), waste = emptyMap()), requiredForC)
        val requiredForM15A = parsed2.findConsumedChemicalsFor("A", 15L)
        assertEquals(RequiredChemicals(mapOf("ORE" to 20L), waste = mapOf("A" to 5L)), requiredForM15A)
    }
    testChemicalRequirementSingleStep()


    fun testOreRequirements() {
        val oreNeeded0ore5 = emptySet<Formula>().reduceToOre(mapOf("ORE" to 5L))
        assertEquals(5, oreNeeded0ore5.first)

        val oreNeeded2a10 = parsed2.reduceToOre(mapOf("A" to 10L))
        assertEquals(10, oreNeeded2a10.first)
        val oreNeeded2a5 = parsed2.reduceToOre(mapOf("A" to 5L))
        assertEquals(10, oreNeeded2a5.first)
        val oreNeeded2a15 = parsed2.reduceToOre(mapOf("A" to 15L))
        assertEquals(20, oreNeeded2a15.first)
        val oreNeeded2b1 = parsed2.reduceToOre(mapOf("B" to 1L))
        assertEquals(1, oreNeeded2b1.first)
        val oreNeeded2c1 = parsed2.reduceToOre(mapOf("C" to 1L))
        assertEquals(11, oreNeeded2c1.first)
        val oreNeeded2fuel1 = parsed2.reduceToOre(mapOf("FUEL" to 1L))
        assertEquals(31, oreNeeded2fuel1.first)

        val oreNeeded3 = parsed3.reduceToOre(mapOf("FUEL" to 1L))
        assertEquals(165, oreNeeded3.first)
        val oreNeeded4 = parsed4.reduceToOre(mapOf("FUEL" to 1L))
        assertEquals(13312, oreNeeded4.first)
        val oreNeeded5 = parsed5.reduceToOre(mapOf("FUEL" to 1L))
        assertEquals(180_697, oreNeeded5.first)
        val oreNeeded6 = parsed6.reduceToOre(mapOf("FUEL" to 1L))
        assertEquals(2_210_736, oreNeeded6.first)
    }
    testOreRequirements()

    fun testOreToFuel() {
        val fuelProduced4 = parsed4.oreToFuel()
        assertEquals(82_892_753, fuelProduced4)
        val fuelProduced5 = parsed5.oreToFuel()
        assertEquals(5_586_022, fuelProduced5)
        val fuelProduced6 = parsed6.oreToFuel()
        assertEquals(460_664, fuelProduced6)
    }
    testOreToFuel()
}

