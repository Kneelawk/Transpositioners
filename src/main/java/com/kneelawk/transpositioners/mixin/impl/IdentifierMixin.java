package com.kneelawk.transpositioners.mixin.impl;

import com.kneelawk.transpositioners.TPConstants;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixes into the Identifier.split logic to detect if the identifier is attempting to parse a namespaced shader
 * identifier for a Transpositioners shader. If so, this returns the correct identifier namespace and path.
 */
@Mixin(Identifier.class)
public class IdentifierMixin {
    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private static void shaderSplit(String id, char delimiter, CallbackInfoReturnable<String[]> cir) {
        String prefix = TPConstants.SHADER_CHECK_PREFIX;
        if (id.startsWith(prefix) && delimiter == ':') {
            String path = id.substring(prefix.length());
            cir.setReturnValue(new String[]{TPConstants.MOD_ID, TPConstants.SHADER_PREFIX + path});
        }
    }
}
