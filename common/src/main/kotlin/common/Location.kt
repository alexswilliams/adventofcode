@file:Suppress("unused", "PublicApiImplicitType")

package common

import kotlin.math.*

typealias Location = Long

infix fun Int.by(col: Int): Location = (this.toLong() shl 32) or col.toLong()
infix fun Long.by(col: Long): Location = (this shl 32) or col
infix fun Location.rowsTo(other: Location): LongRange = (min(this.row(), other.row())..max(this.row(), other.row()))
infix fun Location.colsTo(other: Location): LongRange = (min(this.col(), other.col())..max(this.col(), other.col()))
fun Location.row() = this shr 32
fun Location.col() = this and 0xffffffff
fun Location.rowInt() = (this shr 32).toInt()
fun Location.colInt() = (this and 0xffffffff).toInt()
fun Location.plusRow() = this + 0x1_00000000
fun Location.minusRow() = this - 0x1_00000000
fun Location.plusCol() = this + 1
fun Location.minusCol() = this - 1
fun Location.plusSingleRow() = plusRow()
fun Location.minusSingleRow() = minusRow()
fun Location.plusSingleCol() = plusCol()
fun Location.minusSingleCol() = minusCol()
fun Location.plusRow(amt: Long) = this + 0x1_00000000 * amt
fun Location.minusRow(amt: Long) = this - 0x1_00000000 * amt
fun Location.plusCol(amt: Long) = this + amt
fun Location.minusCol(amt: Long) = this - amt


typealias Location1616 = Int

infix fun Short.by16(col: Short): Location1616 = (this.toInt() shl 16) or col.toInt()
infix fun Int.by16(col: Int): Location1616 = (this shl 16) or col
infix fun Long.by16(col: Long): Location1616 = (this.toInt() shl 16) or col.toInt()
infix fun Location1616.rowsTo(other: Location1616): IntRange = (min(this.row(), other.row())..max(this.row(), other.row()))
infix fun Location1616.colsTo(other: Location1616): IntRange = (min(this.col(), other.col())..max(this.col(), other.col()))
fun Location1616.row() = (this shr 16) and 0xffff
fun Location1616.col() = this and 0xffff
fun Location1616.plusRow() = this + 0x1_0000
fun Location1616.minusRow() = this - 0x1_0000
fun Location1616.plusCol() = this + 1
fun Location1616.minusCol() = this - 1
fun Location1616.plusSingleRow() = plusRow()
fun Location1616.minusSingleRow() = minusRow()
fun Location1616.plusSingleCol() = plusCol()
fun Location1616.minusSingleCol() = minusCol()
fun Location1616.plusRow(amt: Int) = this + 0x1_0000 * amt
fun Location1616.minusRow(amt: Int) = this - 0x1_0000 * amt
fun Location1616.plusCol(amt: Int) = this + amt
fun Location1616.minusCol(amt: Int) = this - amt
fun Location1616.flip() = this.col() by16 this.row()

fun Location1616.manhattanTo(target: Location1616) = (row() - target.row()).absoluteValue + (col() - target.col()).absoluteValue

fun renderLocation1616s(array: IntArray): String = array.joinToString(prefix = "[", postfix = "]") { renderLocation1616(it) }
fun renderLocation1616Children(array: IntArray): Array<String> = array.map { renderLocation1616(it) }.toTypedArray()
fun renderLocation1616(it: Location1616): String = "(${it.row()},${it.col()})"
fun renderLocation(it: Location): String = "(${it.row()},${it.col()})"
fun Location1616.render() = renderLocation1616(this)
fun Location.render() = renderLocation(this)
