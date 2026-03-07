package com.example.halo.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object QRCodeGenerator {
    fun generate(content: String, size: Int = 512): ImageBitmap? {
        try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val formatWidth = bitMatrix.width
            val formatHeight = bitMatrix.height
            val pixels = IntArray(formatWidth * formatHeight)
            
            for (y in 0 until formatHeight) {
                val offset = y * formatWidth
                for (x in 0 until formatWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }
            
            val bitmap = Bitmap.createBitmap(formatWidth, formatHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, formatWidth, 0, 0, formatWidth, formatHeight)
            return bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
