package com.example.funstuff01.utils.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.icu.util.Calendar
import android.net.Uri
import com.example.funstuff01.R
import com.example.funstuff01.utils.getCurrentDateTime
import com.example.funstuff01.utils.toString
import com.example.funstuff01.utils.toast
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

object GeneratePdfUtils {


    // Set DPI to 72 (standard for PDF)
    private const val DPI = 72
    private const val WIDTH_INCHES = 8.27f
    private const val HEIGHT_INCHES = 11.69f
    private const val MARGIN_INCHES = 1f

    private const val WIDTH_PIXELS = (WIDTH_INCHES * DPI).toInt()
    private const val HEIGHT_PIXELS = (HEIGHT_INCHES * DPI).toInt()
    private const val MARGIN_PIXELS = (MARGIN_INCHES * DPI)
    private const val LINE_HEIGHT_PIXELS = 15f



    fun generateShareCertificate(
        context: Context,
        optionHolder: String,
        chatWiseId: String,
        registeredMobile: String,
        shareCount: Int,
        onSuccess: (Uri) -> Unit,
        onFailure: (errorMsg: String?) -> Unit
    ) {
        try {
            val pdfPath = context.filesDir.absolutePath + File.separator + "example2.pdf"
            val pdfFile = File(pdfPath)
            if (pdfFile.parentFile?.exists() == false) {
                pdfFile.parentFile?.mkdirs()
            }




            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                WIDTH_PIXELS,
                HEIGHT_PIXELS,
                1
            ).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            paint.isAntiAlias = true

            // Load the bitmap (replace R.drawable.ic_logo with your actual drawable resource)
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.chatwise_app_icon)
            // Scale the bitmap to the desired size (optional)
            val bitmapSize = WIDTH_PIXELS / 15
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmapSize, bitmapSize, false)

            // Draw the bitmap at the top-right corner
            val bitmapX = WIDTH_PIXELS - scaledBitmap.width - MARGIN_PIXELS // Adjust for some margin from the right
            val bitmapY = MARGIN_PIXELS // Adjust for margin from the top
            canvas.drawBitmap(scaledBitmap, bitmapX, bitmapY, paint)

            // Draw the title
            paint.textSize = 12f
            paint.isFakeBoldText = true
            paint.isUnderlineText = true
            canvas.drawText("Share Option Certificate", 200f, 100f, paint)

            // Draw the company details and terms
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.isUnderlineText = false
            var yPosition = 150f
            val content = """
                ChatWise UK Limited in pursuant of its 2024 User Stock Option Plan (the “USOP”) grants the Optionsholder 
                the option to receive the number of shares of the common stock set out below ( the “Award”). 
                This Award is subject to all the terms and conditions set forth in this notice, in the USOP document 
                and any conditions described on the ChatWise website.
            """.trimIndent()

            yPosition = drawMultiLineText(content, canvas, yPosition, paint)
            yPosition += LINE_HEIGHT_PIXELS * 2


            // date
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateOfGrant = formatter.format(date)

            // Define a fixed x position for values to align
            val labelX = MARGIN_PIXELS
            val valueX = 200f // Adjust this value based on your layout needs

            // Draw the placeholders and data for the option holder, chatWise ID, etc.
            val lineHeightShareDetails = LINE_HEIGHT_PIXELS * 1.25f

            canvas.drawText("OptionsHolder:", labelX, yPosition, paint)
            canvas.drawText(optionHolder, valueX, yPosition, paint)
            yPosition += lineHeightShareDetails

            canvas.drawText("ChatWise ID:", labelX, yPosition, paint)
            canvas.drawText(chatWiseId, valueX, yPosition, paint)
            yPosition += lineHeightShareDetails

            canvas.drawText("Registered Mobile:", labelX, yPosition, paint)
            canvas.drawText(registeredMobile, valueX, yPosition, paint)
            yPosition += lineHeightShareDetails

            // Draw a square border around "Number of shares:"
            val shareText = "Number of shares:"
            val shareValueText = shareCount.toString()

            // Define the padding inside the box
            val padding = LINE_HEIGHT_PIXELS / 2

            // Calculate the rectangle coordinates for the border
            val rectLeft = labelX - padding
            val rectTop = yPosition - LINE_HEIGHT_PIXELS * 0.8f
            val rectRight = valueX + padding + MARGIN_PIXELS * 1.5f
            val rectBottom = yPosition + padding

            // Set the paint for drawing the rectangle (border)
            paint.style = Paint.Style.STROKE // Use stroke for border
            paint.strokeWidth = 0f // Border thickness (Hairline)
            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint)

            // Set the paint back to fill for text drawing
            paint.style = Paint.Style.FILL

            // Now draw the text inside the border
            canvas.drawText(shareText, labelX, yPosition, paint)
            paint.isFakeBoldText = true
            canvas.drawText(shareValueText, valueX, yPosition, paint)
            paint.isFakeBoldText = false
            yPosition += lineHeightShareDetails


//            canvas.drawText("Number of shares:", labelX, yPosition, paint)
//            paint.isFakeBoldText = true
//            canvas.drawText(shareCount.toString(), valueX, yPosition, paint)
//            paint.isFakeBoldText = false
//            yPosition += lineHeightShareDetails

            canvas.drawText("Date of the Grant:", labelX, yPosition, paint)
            canvas.drawText(dateOfGrant, valueX, yPosition, paint)
            yPosition += lineHeightShareDetails

            canvas.drawText("Exercise Price:", labelX, yPosition, paint)
            canvas.drawText("Nil", valueX, yPosition, paint)
            yPosition += lineHeightShareDetails

            canvas.drawText("Term/Expiration Date:", labelX, yPosition, paint)
            canvas.drawText("1 year after the IPO", valueX, yPosition, paint)

            yPosition += LINE_HEIGHT_PIXELS * 2


            // Draw Vesting schedule title
            paint.isFakeBoldText = true
            canvas.drawText("Vesting Schedule:", MARGIN_PIXELS, yPosition, paint)
            paint.isFakeBoldText = false
            yPosition += LINE_HEIGHT_PIXELS * 1.2f

            // Draw the vesting schedule section
            val vestingContent = """
                The option subject to this Attrition shall vest in accordance with the following 
                vesting schedule provided that Optionsholder has:
            """.trimIndent()
            yPosition = drawMultiLineText(vestingContent, canvas, yPosition, paint)



            val vestingContentBulletPoint1 = """
                - used ChatWise app at least once a month till the date IPO is announced
            """.trimIndent()
            yPosition += LINE_HEIGHT_PIXELS * 0.3f
            yPosition = drawMultiLineText(vestingContentBulletPoint1, canvas, yPosition, paint, x = MARGIN_PIXELS * 1.5f)

            val vestingContentBulletPoint2 = """
                - has not breached company policies to the extent that the registered phone number has been 
                permanently barred (only extreme cases).
            """.trimIndent()
            yPosition += LINE_HEIGHT_PIXELS * 0.3f
            yPosition = drawMultiLineText(vestingContentBulletPoint2, canvas, yPosition, paint, x = MARGIN_PIXELS * 1.5f)


            val vestingContentArrowPoint = """
                ➢  Entirety of shares vest 3 months before the IPO date
            """.trimIndent()
            yPosition += LINE_HEIGHT_PIXELS * 0.3f
            yPosition = drawMultiLineText(vestingContentArrowPoint, canvas, yPosition, paint, x = MARGIN_PIXELS * 1.5f)



            yPosition += LINE_HEIGHT_PIXELS

            // Draw the exercise price section
            val shareValue = """
                The value of shares will depend on various factors including market conditions and predominantly 
                on the number of active users at the time of the IPO.
            """.trimIndent()
            yPosition = drawMultiLineText(shareValue, canvas, yPosition, paint)


            yPosition += LINE_HEIGHT_PIXELS * 1.2f

            // Draw Exercise Price title
            paint.isFakeBoldText = true
            canvas.drawText("Exercise Price:", MARGIN_PIXELS, yPosition, paint)
            paint.isFakeBoldText = false
            yPosition += LINE_HEIGHT_PIXELS * 1.2f

            val exerciseContent = """
                Exercise Price for this option holder is set to zero i.e. no cost will be required to pay 
                by the Optionsholder in any currency to receive shares. Optionsholder will however be required 
                to undertake a KYC (Know Your Customer) as per the government rules. At this stage, these 
                Options are linked to the registered mobile number and an OTP will be required to confirm ownership.
                
            """.trimIndent()
            yPosition = drawMultiLineText(exerciseContent, canvas, yPosition, paint)


            yPosition += LINE_HEIGHT_PIXELS * 2

            // Signed and dated section
            canvas.drawText("Signed and dated", MARGIN_PIXELS, yPosition, paint)
            yPosition += LINE_HEIGHT_PIXELS
            // Load the bitmap (replace R.drawable.ic_logo with your actual drawable resource)
            val bitmapSign = BitmapFactory.decodeResource(context.resources, R.drawable.josh_sign)
            // Scale the bitmap to the desired size (optional)
            val scaledBitmapSign = Bitmap.createScaledBitmap(bitmapSign, bitmapSize * 2, bitmapSize, false)
            canvas.drawBitmap(scaledBitmapSign, MARGIN_PIXELS, yPosition, paint)

            yPosition += bitmapSize + LINE_HEIGHT_PIXELS
            canvas.drawText("Gagan Gulati, Chief Executive Officer", MARGIN_PIXELS, yPosition, paint)
            yPosition += LINE_HEIGHT_PIXELS * 2

            canvas.drawText("ChatWise UK Limited", MARGIN_PIXELS, yPosition, paint)
            pdfDocument.finishPage(page)

            try {
                pdfDocument.writeTo(FileOutputStream(pdfFile))
            } catch (e: IOException) {
                e.printStackTrace()
                onFailure(e.message)
                return
            }

            FileUtils.saveFileToAppFolder(
                context = context,
                file = File(pdfPath),
                outputFileName = "chatwise-shares_${Calendar.getInstance().timeInMillis}",
                onSuccess = { fileUri -> onSuccess(fileUri) },
                onFailure = onFailure
            )

            pdfDocument.close()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.message)
        }
    }

    private fun drawMultiLineText(
        text: String,
        canvas: Canvas,
        y: Float,
        paint: Paint,
        maxWidth: Float = WIDTH_PIXELS.toFloat(),
        x: Float = MARGIN_PIXELS,
    ): Float {
        val lines = text.split("\n")
        var yPosition = y

        for (line in lines) {
            var currentLine = line.trim()

            while (currentLine.isNotEmpty()) {
                // Break the text into a line that fits within maxWidth
                val count = paint.breakText(currentLine, true, maxWidth, null)
                val lineToDraw = currentLine.substring(0, count)
                canvas.drawText(lineToDraw, x, yPosition, paint)
                yPosition += LINE_HEIGHT_PIXELS

                // Update the current line, removing the part that has been drawn
                currentLine = currentLine.substring(count).trim()
            }
        }
        return yPosition
    }


    fun generatePdf3(
        context: Context,
        userName: String,
        shareCount: Int,
        chatWiseUserId: String,
        userPhoneNumber: String,
    ) {
        context.toast("Generating PDF NEW")

        val pdfPath = context.filesDir.absolutePath + "image" + File.separator + "example2.pdf"
        val pdfFile = File(pdfPath)
        if (pdfFile.parentFile?.exists() == false) {
            pdfFile.parentFile?.mkdirs()
        }
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(PdfWriter(pdfPath))
        val document = Document(pdfDocument)

        // Load the drawable resource and convert it to Bitmap
//        val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.chatwise_app_icon)!! // Replace with your drawable resource ID
//        val bitmap = (drawable as BitmapDrawable).bitmap

//        val chatWiseLogoSize = (document.pdfDocument.firstPage.pageSize.width / 10).toInt()
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.chatwise_app_icon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false) // Adjust the size as needed



        val imageData = ImageDataFactory.create(bitmapToByteArray(scaledBitmap))
        val image = Image(imageData)
        image.setHorizontalAlignment(HorizontalAlignment.RIGHT)
        document.add(image)

        // Add the header
        document.add(Paragraph("\nShare Option Certificate")
            .setBold()
            .setUnderline()
            .setFontSize(16f)
            .setTextAlignment(TextAlignment.CENTER)
        )

        // Add the date
        document.add(Paragraph("\nDate: 1st August, 2024")
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.LEFT)
        )

        // Add the greeting
        document.add(Paragraph("\nDear $userName,\nWelcome to ChatWise.\n\n")
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.LEFT)
        )


        val date = getCurrentDateTime()
        val dateInString = date.toString("dd/MM/yyyy")

        // Add the main content
        val mainContent = "" +
                "Attached certificate confirms that below number of shares have been allotted " +
                "to you as of today:\n\n" +
                "Date: $dateInString\n" +
                "Number of Shares: $shareCount\n" +
                "ChatWise ID: $chatWiseUserId\n" +
                "Mobile Number: $userPhoneNumber\n\n" +
                "Before the IPO, the company will allow all option holders to claim shares for free " +
                "i.e., at no cost to you. You will be asked to provide documents to confirm your " +
                "identity to satisfy KYC (Know Your Customer).\n\n"

        document.add(Paragraph(mainContent)
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.LEFT)
        )

        // Add the closing
        document.add(Paragraph("ChatWise Team")
            .setFontSize(14f)
            .setTextAlignment(TextAlignment.LEFT)
        )

        // Close the document
        document.close()

        FileUtils.saveFileToAppFolder(
            context = context,
            file = File(pdfPath),
            onSuccess = {
                context.toast("PDF generated successfully")
            },
            onFailure = {
                context.toast("Failed to save in public folder")
            }
        )
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}