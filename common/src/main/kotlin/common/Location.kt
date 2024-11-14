package common

typealias Location = Long

infix fun Int.by(col: Int): Location = (this.toLong() shl 32) or col.toLong()
infix fun Long.by(col: Long): Location = (this shl 32) or col
fun Location.row() = this shr 32
fun Location.col() = this and 0xffffffff
fun Location.rowInt() = (this shr 32).toInt()
fun Location.colInt() = (this and 0xffffffff).toInt()
fun Location.plusRow() = this + 0x1_00000000
fun Location.minusRow() = this - 0x1_00000000
fun Location.plusCol() = this + 1
fun Location.minusCol() = this - 1
