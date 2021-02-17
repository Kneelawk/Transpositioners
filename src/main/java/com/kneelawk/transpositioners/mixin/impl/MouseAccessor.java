package com.kneelawk.transpositioners.mixin.impl;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    void setY(double y);
}
