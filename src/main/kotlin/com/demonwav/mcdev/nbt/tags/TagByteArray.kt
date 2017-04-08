/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.nbt.tags

import java.io.OutputStream
import java.util.Arrays

class TagByteArray(override val value: ByteArray) : NbtValueTag<ByteArray>(ByteArray::class.java) {
    override val payloadSize = value.size
    override val typeId = NbtTypeId.BYTE_ARRAY

    override fun write(stream: OutputStream, isBigEndian: Boolean) {
        val length = if (isBigEndian) {
            value.size.toBigEndian()
        } else {
            value.size.toLittleEndian()
        }

        stream.write(byteArrayOf(*length, *value))
    }

    override fun toString() = toString(StringBuilder(), 0).toString()

    override fun valueToString(sb: StringBuilder) {
        sb.append("[")
        value.joinTo(buffer = sb, separator = ", ")
        sb.append("]")
    }

    override fun valueEquals(otherValue: ByteArray): Boolean {
        return Arrays.equals(this.value, otherValue)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(this.value)
    }

    override fun valueCopy(): ByteArray {
        return Arrays.copyOf(value, value.size)
    }
}