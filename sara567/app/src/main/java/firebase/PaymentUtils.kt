package firebase

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
//isme sirf qr pay wala h baki kuch nhi
object PaymentUtils {
    fun createUpiQRCode(
        upiId: String,
        amount: Double,
        name: String = "Admin",
        note: String = "Add Funds"
    ): Bitmap? {
        val uriString = "upi://pay?pa=$upiId&pn=$name&am=$amount&cu=INR&tn=$note&tr=${System.currentTimeMillis()}"
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(uriString, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}