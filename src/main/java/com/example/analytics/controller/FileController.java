package com.example.analytics.controller;

import com.example.analytics.exception.ResourceNotFoundException;
import com.example.analytics.model.File;
import com.example.analytics.model.User;
import com.example.analytics.payload.*;
import com.example.analytics.repository.FileRepository;
import com.example.analytics.repository.UserRepository;
import com.example.analytics.security.UserPrincipal;
import com.example.analytics.service.FileService;
import com.example.analytics.security.CurrentUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@CrossOrigin
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
	private FileRepository fileRepository;

    //private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {
      String message = "";
      try {
    	fileService.store(file);
        message = "Uploaded the file successfully: " + file.getOriginalFilename();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message));
      } catch (Exception e) {
        message = "Could not upload the file: " + file.getOriginalFilename() + "!";
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ApiResponse(false, message));
      }
    }
    
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
      String message = "";
      try {
    	  File fileDB = fileService.getFile(id);
  	    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    	  fileDB.setName(fileName);
    	  fileDB.setType(file.getContentType());
    	  fileDB.setData(file.getBytes());    	  
    	  fileService.store(file);
        message = "Updated the file successfully: " + file.getOriginalFilename();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message));
      } catch (Exception e) {
        message = "Could not update the file: " + file.getOriginalFilename() + "!";
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ApiResponse(false, message));
      }
    }
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/allfiles")
    public ResponseEntity<List<FileResponse>> getListFiles() {
      List<FileResponse> files = fileService.getAllFiles().map(dbFile -> {
        String fileDownloadUri = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/files/")
            .path(dbFile.getId().toString())
            .toUriString();
        return new FileResponse(
        		dbFile.getId(),
            dbFile.getName(),
            fileDownloadUri,
            dbFile.getType(),
            dbFile.getData().length);
      }).collect(Collectors.toList());
      return ResponseEntity.status(HttpStatus.OK).body(files);
    }
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<File> getFile(@PathVariable Long id) {
      File fileDB = fileService.getFile(id);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
          .body(fileDB);
    }
    
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete/{id}")
	  public String deleteFile(@PathVariable Long id) {
	    try {
	    	fileRepository.deleteById(id);
	    	return "Success";
		} catch (Exception e) {
			return "Error" + e;
		}
	  }
}
