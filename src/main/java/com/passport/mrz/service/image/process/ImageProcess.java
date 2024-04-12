package com.passport.mrz.service.image.process;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.springframework.stereotype.Service;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

@Service
public class ImageProcess {

    public String getMrz(String imageUrl){

        BytePointer outText;

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
        }

        // Open input image with leptonica library
        lept.PIX image = pixRead("src/main/resources/templates/img_1.png");
        api.SetImage(image);
        // Get OCR result
        outText = api.GetUTF8Text();

        // Destroy used object and release memory
        api.End();
        outText.deallocate();
        pixDestroy(image);


        return outText.getString();

    }
}
