package com.bearguard.mobile.service

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * matt/2026-08-15: the on-device Android equivalent of TesseractOcrProvider on the Windows side.
 * ML Kit instead of Tesseract -- tess4j (Bearguard-Win's binding) is a desktop JNI wrapper and
 * doesn't run on Android at all. ML Kit's model runs fully on-device via Play Services (no network
 * call per read, no per-request cost), which is the standard Android-native equivalent.
 *
 * Crop-then-recognize, same shape as every TL/BR PointData pair throughout Bearguard-Win's
 * Routine classes -- callers pass a region, get text back. Existing calibrated crop coordinates
 * from the Windows side are directly reusable as long as both are reading the same 720x1280 MuMu
 * resolution; each will need its own fresh calibration on the tablet later (see
 * bearguard-mobile-tablet-calibration memory).
 */
object OcrHelper {
    private const val TAG = "BearGuardMobile"
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Crops [source] to the rectangle described by (left, top) - (right, bottom) in bitmap pixel
     * coordinates, runs on-device text recognition, and delivers the flattened recognized text
     * (or null on failure) to [onResult]. Runs async on ML Kit's own executor; onResult may be
     * called from a background thread.
     */
    fun readText(source: Bitmap, left: Int, top: Int, right: Int, bottom: Int, onResult: (String?) -> Unit) {
        val l = left.coerceIn(0, source.width - 1)
        val t = top.coerceIn(0, source.height - 1)
        val w = (right - l).coerceIn(1, source.width - l)
        val h = (bottom - t).coerceIn(1, source.height - t)

        val crop = try {
            Bitmap.createBitmap(source, l, t, w, h)
        } catch (e: Exception) {
            Log.w(TAG, "OCR crop failed: ${e.message}")
            onResult(null)
            return
        }

        val image = InputImage.fromBitmap(crop, 0)
        recognizer.process(image)
            .addOnSuccessListener { result -> onResult(result.text) }
            .addOnFailureListener { e ->
                Log.w(TAG, "OCR recognition failed: ${e.message}")
                onResult(null)
            }
    }
}
