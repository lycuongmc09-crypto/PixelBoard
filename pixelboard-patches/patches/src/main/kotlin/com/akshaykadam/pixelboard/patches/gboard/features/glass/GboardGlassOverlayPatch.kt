/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.patches.gboard.features.glass

import com.akshaykadam.pixelboard.patches.gboard.shared.GboardMethodTarget
import com.akshaykadam.pixelboard.patches.gboard.shared.findMutableMethodOrNull
import com.akshaykadam.pixelboard.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import com.akshaykadam.pixelboard.patches.gboard.shared.isMethodReference
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallId
import com.akshaykadam.pixelboard.patches.shared.Constants.COMPATIBILITY_GBOARD
import com.akshaykadam.pixelboard.patches.shared.addInstructions
import com.akshaykadam.pixelboard.patches.shared.bytecodePatch

// Gboard 18.6.3: Lflz; is the Rambler keyboard header. W(ViewGroup, boolean) hides
// the header slots; its first parameter is the container that hosts the voice
// toolbar. Passing it to the runtime lets Phase 1 decorate that container with
// a frosted overlay without needing any new resource ids.
private val ramblerHeaderHideSlots = GboardMethodTarget(
    classType = "Lflz;",
    name = "W",
    parameterTypes = listOf("Landroid/view/ViewGroup;", "Z"),
    returnType = "V",
)

internal val gboardGlassOverlayPatch = bytecodePatch(
    description = "Phase 1 liquid glass: add a frosted overlay to the Rambler voice toolbar container.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val method = findMutableMethodOrNull(ramblerHeaderHideSlots) ?: return@execute
        val implementation = method.implementation ?: return@execute
        val call = RuntimeCallId.GLASS_OVERLAY_RUNTIME_INSTALL
        if (implementation.instructions.any { it.isMethodReference(RuntimeAbiCatalog.abi(call).reference) }) {
            return@execute
        }
        // Instance method: p0 = this, p1 = ViewGroup, p2 = boolean.
        method.addInstructions(0, RuntimeCallEmitter.invoke(call, "p1"))
    }
}
