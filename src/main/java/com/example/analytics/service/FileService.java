package com.example.analytics.service;

import com.example.analytics.model.File;
import com.example.analytics.repository.FileRepository;
import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	  @Autowired
	  private FileRepository fileRepository;
	
	  public File store(MultipartFile file) throws IOException {
	    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
	    File FileDB = new File(fileName, file.getContentType(), file.getBytes());
	    return fileRepository.save(FileDB);
	  }
	  
	  public File getFile(Long id) {
	    return fileRepository.findById(id).get();
	  }
	  
	  public Stream<File> getAllFiles() {
	    return fileRepository.findAll().stream();
	  }

}