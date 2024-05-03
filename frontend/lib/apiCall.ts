import { jwtDecode, JwtPayload } from "jwt-decode";
import axios from "axios";
import {Simulate} from "react-dom/test-utils";
import error = Simulate.error;
import {h} from "preact";
import exp from "constants";


export const api = axios.create({
  baseURL: "http://localhost:8888/api",
  withCredentials: true,
});

export const getHeader = async () => {
  let accessToken = localStorage.getItem("access_token");
  // @ts-ignore
  const decoded: JwtPayload = jwtDecode(accessToken); // Type assertion for decoded data
  // @ts-ignore
  const isExpired = Date.now() >= decoded.exp * 1000; // Check expiration in milliseconds
  console.log(isExpired)
  if (isExpired) {
    const userId = localStorage.getItem("userId");
    const response = await api.get(`/auth/newAccessToken?userId=${userId}`);
    localStorage.removeItem("access_token");
    localStorage.setItem("access_token", response.data.accessToken);
  }

  accessToken = localStorage.getItem("access_token");
  return {
    Authorization: `Bearer ${accessToken}`,
    // "Content-Type": "application/json",
  };
};

export const getAllRootFolder = async (storageId: string) => {
  try {
    const response = await api.get(`/folder/rootFolders/${storageId}`, {
      headers: await getHeader(),
    });
    return response.data;
  } catch (error) {
    console.log(error);
  }
};

export const createRootFolder = async (storageId: string, folder: Object)=>{
  try{
    console.log(storageId)
    const response = await api.post(`/folder/root_folder/create/${storageId}`,
    folder,
    {
      headers: await getHeader()
    },
  )

    console.log(response.data)
    return response.data;

  }catch (e) {
    console.log(e)
  }
}

export const sendMailPassword= async (email:string)=>{
  try{
    let user={
      email: email
    }
    const response = await  api.post(`/auth/resetPassword`, user )
    return response.data
  }catch (e) {
    console.log(e)
  }
}

export const deleteFolder = async (storageId: string, folderId: number) => {
  try {
    const headers = await getHeader(); // Getting headers asynchronously
    const response = await api.delete(`/folder/delete/${storageId}?folderId=${folderId}`, {
      headers: headers,
    });

    console.log("Folder deleted successfully:", response.data);
    return response.data;
  } catch (error) {
    console.error("Error deleting folder:", error);

    throw error;
  }
};
export  const updateFolder= async (storageId: string, folderId: string, newFolder: object)=>{
  try {
    const  headers= await getHeader();
    console.log(newFolder)
    // @ts-ignore
    const response = await api.patch(`/folder/update/${storageId}/${folderId}`,newFolder,{
      headers:headers
    })
    return response.data
  }catch (error) {
    console.error("Error deleting folder:", error);

    throw error;
  }

}

export  const openFolder = async (storageId: string | null, folderId: string)=>{
  try {
    const  headers= await getHeader();
    const response= await api.get(`/folder/getFolder/${storageId}?folderId=${folderId}`,{
      headers: headers
    } )

    return response.data

  }catch (e) {
    console.error("Error :", e)
    throw e;
  }
}

export const createFolder = async (storageId:string, parentFolderId:string, newFolder:object)=>{
  try{
    console.log("storaeg Id"+storageId)
    const response = await api.post(`/folder/create/${storageId}/${parentFolderId}`,
        newFolder,
        {
          headers: await getHeader()
        },
    )

    console.log(response.data)
    return response.data;

  }catch (e) {
    console.log(e)
  }
}

export const uploadFile = async (userId:string,folderId:string,storageId:string,file: string | Blob)=>{
  try {
    console.log("upload file ")
    console.log(file)
    const form= new FormData()
    form.append("file",file)

    const header= await  getHeader();
    const  response= await api.post(`/file/upload/${userId}/${folderId}/${storageId}`,form,{
      headers: header
    })
    return response.data
  }catch (e) {
    console.error(e)
    throw e;
  }
}


export const getCapacity = async (storageId:number)=>{
  try {
    const header = await getHeader();
    const response = await api.get(`/storage/currentCapacity?storageId=${storageId}`,{
      headers: header,

    });
    console.log(response.data)

    return response.data;

  } catch (error) {
    console.log(error)

  }
}

export const softDelete= async (fileId:string)=>{
  try {
    console.log(fileId)
    const header= await getHeader();
    const response= await api.patch(`/file/softDelete?fileId=${fileId}`,{},{
      headers: header
    })
    return response.data
  }catch (e){
    console.log(e)
    throw  e;
  }
}

export const getFile= async (fileId:string,fileName:string,userId:string|null)=>{
  try {
    const header= await  getHeader();
    const response= await  api.get(`file/read/${userId}/${fileName}?fileId=${fileId}`,{
      headers:header,
      responseType: 'blob'
    })
    return response.data
  }catch (e){
    console.log(e)
    throw e
  }
}


export const deletedFile= async (storageId:string)=>{
  try {
    const header = await getHeader();
    const response = await api.get(`file/trash/${storageId}`,{
      headers: header,
    });
    console.log(response.data)
    return response.data;
  } catch (error) {
    console.log(error)

  }
}

export const deletePermanently = async (fileId:string, storageId:string, userId:string)=>{
  try {
    const header= await getHeader();
    const response= await api.delete(`file/delete/${fileId}/${storageId}?userId=${userId}`,{
      headers: header
    })
    return response.data

  } catch (error) {
    console.log(error)

  }
}
