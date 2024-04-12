package com.passport.mrz.controller;

import com.passport.mrz.service.image.process.ImageProcess;
import com.passport.mrz.model.CodeRequest;
import com.passport.mrz.model.MrzInfo;
import com.passport.mrz.service.parser.MrzParserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MrzController {

    private final MrzParserService mrzParserService;
    private final ImageProcess imageProcess;

    public MrzController(MrzParserService mrzParserService, ImageProcess imageProcess) {
        this.mrzParserService = mrzParserService;
        this.imageProcess = imageProcess;
    }

    @PostMapping("/pars")
    public MrzInfo parsMrzCode(@RequestBody CodeRequest codeRequest){

        return mrzParserService.parse(codeRequest.getCode());
    }

    @GetMapping("/process")
    public String getMrzFromImage(){

        return imageProcess.getMrz("");
    }
}
