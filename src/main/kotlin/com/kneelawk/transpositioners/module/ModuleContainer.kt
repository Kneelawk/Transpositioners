package com.kneelawk.transpositioners.module

interface ModuleContainer {
    fun getModule(index: Int): Module?
}