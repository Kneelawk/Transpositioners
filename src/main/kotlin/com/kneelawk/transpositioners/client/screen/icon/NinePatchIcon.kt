package com.kneelawk.transpositioners.client.screen.icon

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import kotlin.math.min

/**
 * No this can't load actual *.9.png files. All this does is it scales the images like they were loaded from 9patches.
 */
class NinePatchIcon(
    private val textureId: Identifier,
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val pieceX: Int,
    private val pieceY: Int,
    pieceWidth: Int,
    pieceHeight: Int,
    private val leftWidth: Int,
    private val rightWidth: Int,
    private val topHeight: Int,
    private val bottomHeight: Int,
    private val tiling: Boolean
) : ResizableIcon {
    private val endX = pieceX + pieceWidth
    private val endY = pieceY + pieceHeight

    private val tileWidth = pieceWidth - leftWidth - rightWidth
    private val tileHeight = pieceHeight - topHeight - bottomHeight

    init {
        if (tileWidth < 1) {
            throw IllegalArgumentException("leftWidth + rightWidth must be less than pieceWidth")
        }
        if (tileHeight < 1) {
            throw IllegalArgumentException("topHeight + bottomHeight must be less than pieceHeight")
        }
    }

    private val pieceU1 = pieceX.toFloat() / imageWidth.toFloat()
    private val pieceV1 = pieceY.toFloat() / imageHeight.toFloat()
    private val pieceU2 = endX.toFloat() / imageWidth.toFloat()
    private val pieceV2 = endY.toFloat() / imageHeight.toFloat()
    private val leftRightU = (pieceX + leftWidth).toFloat() / imageWidth.toFloat()
    private val rightLeftU = (endX - rightWidth).toFloat() / imageWidth.toFloat()
    private val topBottomV = (pieceY + topHeight).toFloat() / imageHeight.toFloat()
    private val bottomTopV = (endY - bottomHeight).toFloat() / imageHeight.toFloat()

    override val minWidth = leftWidth + rightWidth
    override val minHeight = topHeight + bottomHeight

    @Environment(EnvType.CLIENT)
    override fun paint(
        matrices: MatrixStack, consumers: VertexConsumerProvider, x: Int, y: Int, width: Int, height: Int
    ) {
        paint(x, y, width, height) { x, y, width, height, texture, u1, v1, u2, v2 ->
            IconRenderingUtils.texturedRect(matrices.peek().model, consumers, x, y, width, height, texture, u1, v1, u2, v2, -1, 1f)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun paint(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        paint(x, y, width, height) { x, y, width, height, texture, u1, v1, u2, v2 ->
            ScreenDrawing.texturedRect(matrices, x, y, width, height, texture, u1, v1, u2, v2, -1)
        }
    }

    private inline fun paint(
        x: Int, y: Int, width: Int, height: Int,
        texturedRect: (Int, Int, Int, Int, Identifier, Float, Float, Float, Float) -> Unit
    ) {
        val centerWidth = width - leftWidth - rightWidth
        val centerHeight = height - topHeight - bottomHeight
        val rightLeft = x + width - rightWidth
        val bottomTop = y + height - bottomHeight

        // draw the 4 corners
        texturedRect(
            x, y, leftWidth, topHeight, textureId, pieceU1, pieceV1, leftRightU, topBottomV
        )
        texturedRect(
            rightLeft, y, rightWidth, topHeight, textureId, rightLeftU, pieceV1, pieceU2, topBottomV
        )
        texturedRect(
            x, bottomTop, leftWidth, bottomHeight, textureId, pieceU1, bottomTopV, leftRightU, pieceV2
        )
        texturedRect(
            rightLeft, bottomTop, rightWidth, bottomHeight, textureId, rightLeftU, bottomTopV, pieceU2, pieceV2
        )

        if (tiling) {
            val tilesX = (centerWidth + tileWidth - 1) / tileWidth
            val tilesY = (centerHeight + tileHeight - 1) / tileHeight

            for (tileXIndex in 0 until tilesX) {
                val localTileX = tileXIndex * tileWidth
                val curTileWidth = min(tileWidth, centerWidth - localTileX)
                val curTileU2 = (pieceX + leftWidth + curTileWidth).toFloat() / imageWidth.toFloat()
                texturedRect(
                    leftWidth + localTileX + x, y, curTileWidth, topHeight, textureId, leftRightU, pieceV1, curTileU2,
                    topBottomV
                )
                texturedRect(
                    leftWidth + localTileX + x, bottomTop, curTileWidth, bottomHeight, textureId, leftRightU,
                    bottomTopV, curTileU2, pieceV2
                )
            }

            for (tileYIndex in 0 until tilesY) {
                val localTileY = tileYIndex * tileHeight
                val curTileHeight = min(tileHeight, centerHeight - localTileY)
                val curTileV2 = (pieceY + topHeight + curTileHeight).toFloat() / imageHeight.toFloat()
                texturedRect(
                    x, topHeight + localTileY + y, leftWidth, curTileHeight, textureId, pieceU1, topBottomV, leftRightU,
                    curTileV2
                )
                texturedRect(
                    rightLeft, topHeight + localTileY + y, rightWidth, curTileHeight, textureId, rightLeftU, topBottomV,
                    pieceU2, curTileV2
                )

                for (tileXIndex in 0 until tilesX) {
                    val localTileX = tileXIndex * tileWidth
                    val curTileWidth = min(tileWidth, centerWidth - localTileX)
                    val curTileU2 = (pieceX + leftWidth + curTileWidth).toFloat() / imageWidth.toFloat()
                    texturedRect(
                        leftWidth + localTileX + x, topHeight + localTileY + y, curTileWidth, curTileHeight, textureId,
                        leftRightU, topBottomV, curTileU2, curTileV2
                    )
                }
            }
        } else {
            if (centerWidth > 0) {
                texturedRect(
                    leftWidth + x, y, centerWidth, topHeight, textureId, leftRightU, pieceV1, rightLeftU, topBottomV
                )
                texturedRect(
                    leftWidth + x, bottomTop, centerWidth, bottomHeight, textureId, leftRightU, bottomTopV, rightLeftU,
                    pieceV2
                )
            }

            if (centerHeight > 0) {
                texturedRect(
                    x, topHeight + y, leftWidth, centerHeight, textureId, pieceU1, topBottomV, leftRightU, bottomTopV
                )
                texturedRect(
                    rightLeft, topHeight + y, rightWidth, centerHeight, textureId, rightLeftU, topBottomV, pieceU2,
                    bottomTopV
                )

                if (centerWidth > 0) {
                    texturedRect(
                        leftWidth + x, topHeight + y, centerWidth, centerHeight, textureId, leftRightU, topBottomV,
                        rightLeftU, bottomTopV
                    )
                }
            }
        }
    }
}