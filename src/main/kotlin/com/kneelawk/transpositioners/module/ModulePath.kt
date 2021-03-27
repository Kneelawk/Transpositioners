package com.kneelawk.transpositioners.module

import net.minecraft.network.PacketByteBuf

class ModulePath private constructor(val segments: List<Int>) {
    companion object {
        val ROOT = ModulePath(listOf())

        fun readFromBuf(buf: PacketByteBuf): ModulePath {
            val count = buf.readVarInt()
            val segments = mutableListOf<Int>()
            for (i in 0 until count) {
                segments += buf.readVarInt()
            }
            return ModulePath(segments)
        }
    }

    val isRoot: Boolean
        get() = segments.isEmpty()

    val size: Int
        get() = segments.size

    val parent: ModulePath? by lazy {
        if (isRoot) {
            null
        } else {
            ModulePath(segments.subList(0, segments.size - 1))
        }
    }

    fun child(index: Int): ModulePath = ModulePath(segments + index)

    fun writeToBuf(buf: PacketByteBuf) {
        buf.writeVarInt(size)
        for (segment in segments) {
            buf.writeVarInt(segment)
        }
    }

    fun findModule(container: ModuleContainer): Module? {
        if (isRoot) {
            return if (container is Module) {
                container
            } else {
                null
            }
        } else {
            var cur: Module? = container.getModule(segments[0])
            for (segment in segments.subList(1, segments.size)) {
                if (cur == null) {
                    return null
                }
                cur = cur.getModule(segment)
            }
            return cur
        }
    }

    override fun toString(): String {
        return segments.joinToString("/", "(", ")")
    }
}