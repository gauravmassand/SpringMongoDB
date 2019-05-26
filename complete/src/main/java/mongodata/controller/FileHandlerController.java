package mongodata.controller;

import com.amazonaws.services.s3.model.S3Object;
import mongodata.Customer;
import mongodata.InserService;
import mongodata.InsertController;
import mongodata.dataImports.ExcelReader;
import mongodata.objects.FundRaisingSumm;
import mongodata.service.AmazonS3ClientService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileHandlerController {

    @Autowired
    private AmazonS3ClientService amazonS3ClientService;

    @Autowired
    InserService inserService;

    @Autowired
    ExcelReader excelReader;


    @PostMapping
    public Map<String, String> uploadFile(@RequestPart(value = "file") MultipartFile file) throws Exception {

        ArrayList<FundRaisingSumm> result = excelReader.getDataFfromFile(file);
        inserService.insertDataArray(result);
        file.getOriginalFilename();
        this.amazonS3ClientService.uploadFileToS3Bucket(file, true);

        Map<String, String> response = new HashMap<>();
        response.put("message", "file [" + file.getOriginalFilename() + "] uploading request submitted successfully.");

        return response;
    }



    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(){
        S3Object don = amazonS3ClientService.downloadFileFromS3Bucket("2016-2017-TallyData.xlsx", "trrainfiles");
        InputStream in = don.getObjectContent();
        byte[] bytes =null;
        try {
            //FileUtils.copyInputStreamToFile(in, file);
            bytes = IOUtils.toByteArray(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "2016-2017-TallyData.xlsx" + "\"")
                .body(bytes);
    }

    @DeleteMapping
    public Map<String, String> deleteFile(@RequestParam("file_name") String fileName)
    {
        this.amazonS3ClientService.deleteFileFromS3Bucket(fileName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "file [" + fileName + "] removing request submitted successfully.");

        return response;
    }
}