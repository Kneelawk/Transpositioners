package com.kneelawk.transpositioners.net

interface NetEntityScope<Self : NetEntityScope<Self>> : NetServerPositionedScope<Self>, NetClientScope<Self> {
}