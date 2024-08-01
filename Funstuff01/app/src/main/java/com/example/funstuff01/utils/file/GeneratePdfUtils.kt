package com.example.funstuff01.utils.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.ContextCompat
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

object GeneratePdfUtils {

    fun generatePdf(
        context: Context,
        text: String,
    ) {
        context.toast("Generating PDF")

        // Create a new PdfDocument
        val pdfDocument = PdfDocument()

        // Create a PageDescription with width and height
        val a4DocumentRatio = 1f / 1.41f
        val pageWidth = 595
        val pageHeight = (pageWidth / a4DocumentRatio).toInt()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        // Start a page
        val page = pdfDocument.startPage(pageInfo)

        // Create a canvas to draw on the page
        val canvas: Canvas = page.canvas

        // Load the image
        val chatWiseLogoSize = pageWidth / 10
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.chatwise_app_icon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, chatWiseLogoSize, chatWiseLogoSize, false) // Adjust the size as needed

        // Draw the image on the canvas
        val offsetForCenterBitmap = (pageWidth / 2f) - (chatWiseLogoSize / 2f)
        canvas.drawBitmap(scaledBitmap, offsetForCenterBitmap, 50f, null) // Adjust the position as needed

        // Set up paint for text
        val paint = Paint()
        paint.textSize = 16f // Adjust text size as needed
        paint.isAntiAlias = true

        // Draw the text on the canvas
        canvas.drawText(text, 50f, 200f, paint) // Adjust the position as needed

        // Finish the page
        pdfDocument.finishPage(page)

        // Save the document
        val file = File(context.filesDir, "image" + File.separator + "example.pdf")
        if (file.parentFile?.exists() == false) {
            file.parentFile?.mkdirs()
        }
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            context.toast("Failed to generate PDF")
        }

        // Close the document
        pdfDocument.close()

        FileUtils.saveFileToAppFolder(
            context = context,
            file = file,
            onSuccess = {
                context.toast("PDF generated successfully")
            },
            onFailure = {
                context.toast("Failed to save in public folder")
            }
        )
    }



    fun generatePdf2(context: Context) {
        context.toast("Generating PDF NEW")

        val pdfPath = context.filesDir.absolutePath + "image" + File.separator + "example2.pdf"
        val pdfFile = File(pdfPath)
        if (pdfFile.parentFile?.exists() == false) {
            pdfFile.parentFile?.mkdirs()
        }
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(PdfWriter(pdfPath))
        val document = Document(pdfDocument)

        // Load the drawable resource and convert it to Bitmap
        val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.chatwise_app_icon)!! // Replace with your drawable resource ID
        val bitmap = (drawable as BitmapDrawable).bitmap
        val imageData = ImageDataFactory.create(bitmapToByteArray(bitmap))
        val image = Image(imageData)
        image.setHorizontalAlignment(HorizontalAlignment.CENTER)
        document.add(image)

        // Add the header
        document.add(
            Paragraph("Chatwise UK Limited\n20-22, Wenlock Road, London N1 7GU, UK")
            .setBold()
            .setFontSize(12f)
            .setHorizontalAlignment(HorizontalAlignment.CENTER)
        )

        // Add the subject
        document.add(Paragraph("\nSubject: Android Developer Role\n\n")
            .setBold()
            .setFontSize(14f)
        )

        // Add the date
        document.add(Paragraph("20/06/2024\n\n"))

        // Add the greeting
        document.add(Paragraph("Dear Abizer,\n\n"))

        // Add the main content
        val mainContent = "Thanks for your hard work and commitment to ChatWise. " +
                "We are pleased to offer you a role as Android Developer. Your roles and responsibilities " +
                "will change slightly with a wider scope of work – your team leader will have a conversation with you.\n\n" +
                "Salary = 35,000 per month (effective from 15th June 24)\n" +
                "Fund raising bonus = Rs 1 Lakh to be paid after the fund raising is complete " +
                "(subject to ChatWise successfully completing the funding round in line with our plans)\n" +
                "Salary post fund-raising = Around 6 Lakhs\n\n" +
                "Fund Raising – Company has already started its fund raising round. It is expected to go for another 2-3 months " +
                "(can be much sooner, previous stage was completed in 1 month). We will keep you updated.\n\n"

        document.add(Paragraph(mainContent))

        // Add the closing
        document.add(Paragraph("ChatWise Human Resources Team\nCHATWISE UK LIMITED"))

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