package com.webapp.intelligentworkspace.service;

import com.azure.storage.blob.BlobServiceClient;
import com.webapp.intelligentworkspace.model.entity.File;
import com.webapp.intelligentworkspace.model.entity.Folder;
import com.webapp.intelligentworkspace.model.entity.Storage;
import com.webapp.intelligentworkspace.model.entity.User;
import com.webapp.intelligentworkspace.model.response.StorageResponse;
import com.webapp.intelligentworkspace.repository.FileRepository;
import com.webapp.intelligentworkspace.repository.FolderRepository;
import com.webapp.intelligentworkspace.repository.StorageRepository;
import com.webapp.intelligentworkspace.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class FileService {

    private final BlobServiceClient blobServiceClient;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final StorageService storageService;
    private final BlobStorageService blobStorageService;
    private  final String base64EncodedKey = "9fWSwatmHu2KQwLoDA1rSQ==";

    public FileService(BlobServiceClient blobServiceClient, FileRepository fileRepository, FolderRepository folderRepository, UserRepository userRepository, StorageRepository storageRepository, StorageService storageService, BlobStorageService blobStorageService) {
        this.blobServiceClient = blobServiceClient;
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
        this.storageService = storageService;
        this.blobStorageService = blobStorageService;
    }

    public String uploadFile(MultipartFile file, Long storageId, Long folderId, Integer userId) {
        try {
            Storage storage = storageRepository.findById(storageId).orElseThrow(() -> new RuntimeException("Storage not found"));
            Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new RuntimeException("Folder not found"));
            StorageResponse storageResponse= storageService.updateCapacity(storageId,file.getSize());
            if(storageResponse.isStatus()==false) {
                return storageResponse.getMessage();
            }
            // Use the BlobStorageService's upload method
            String uploadResult = blobStorageService.upload(file, userId);
            if (uploadResult != null) {
                return uploadResult;
            }
            File fileEntity = new File();

            fileEntity.setFile_name(file.getOriginalFilename());
            fileEntity.setFile_url(blobServiceClient.getBlobContainerClient(userId.toString()).getBlobClient(file.getOriginalFilename()).getBlobUrl());
            fileEntity.setSize(file.getSize());
            fileEntity.setFolder(folder);
            fileEntity.setStorage(storage);
            fileEntity.setIsDeleted(false);

            fileRepository.save(fileEntity);

            folder.addFile(fileEntity);
            folderRepository.save(folder);
            return "Uploaded Successfully, file saved in the database";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload file";
        }
    }

    public String deleteFile(Long fileId, Integer userId,Long storageId) {
        try {
            File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
            blobStorageService.delete(userId.toString(), file.getFile_name());
            storageService.deleteCapacity(storageId, file.getSize());
            fileRepository.delete(file);

            return "Deleted Successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to delete file";
        }
    }

    public byte[] getFileData(String fileName, Integer userId, Long fileId) {
        File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
//        if(file.getIsDeleted()){
//            return null;
//        }
        return blobStorageService.getFile(fileName,userId);
    }

    public byte[] getSharedFile(String sharedCode, String fileName, Long fileId) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] decodedKey= Base64.getDecoder().decode(base64EncodedKey);
        SecretKey key= new SecretKeySpec(decodedKey,"AES");
        Cipher cipher= Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(sharedCode));
        String decryptedString= new String(decryptedBytes);
        System.out.println(decryptedString);

        int userId= Integer.parseInt(decryptedString.split("\\.")[2]);
        File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));

        return blobStorageService.getFile(fileName,userId);
    }

    public List<File> listFilesInFolder(Long folderId) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new RuntimeException("Folder not found"));
        return new ArrayList<>(folder.getFiles());
    }

    public String renameFile(Long fileId, String newFileName) {
        try {
            File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
            file.setFile_name(newFileName);
            fileRepository.save(file);
            return "Renamed Successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to rename file";
        }
    }

    // soft delete == move to bin

    public void moveToTrash(Long fileId){
        System.out.println("move to trash folder with id : "+fileId);
        File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        if(file.getIsDeleted()){
            return;
        }else {
            System.out.println("set true to move to bin");
            file.setIsDeleted(true);
            file.setFolder(null);
            fileRepository.save(file);
        }
    }

    public List<File> getFileInBin(Long storageId){
        Storage storage = storageRepository.findById(storageId).orElseThrow(() -> new RuntimeException("Storage not found"));
        if(storage==null){
            return null;
        }
        else return fileRepository.findByStorageAndIsDeletedIsTrue(storage);
    }

}

