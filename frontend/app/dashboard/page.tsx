"use client";
import { useEffect } from "react";
import {AppDispatch, useAppSelector} from "@/lib/store";
import { getAllRootFolder } from "@/lib/apiCall";
import { get } from "http";
import { useState } from "react";
import FolderList from "@/components/FolderList";
import {useDispatch} from "react-redux";
import { setStorageID } from '@/lib/features/todos/storageSlice';
import { NewFolder } from "@/components/NewFolder";


export default function Page() {
  const  dispatch=useDispatch<AppDispatch>();
  const [folderList, setFolderList] = useState([]);
  // @ts-ignore
  const storageID: string | null = useAppSelector(
    (state) => state.storageSlice.storageID
  );

  useEffect(() => {
    if (storageID != null) {
      dispatch(setStorageID(storageID))
    }
    if (storageID !== null) {
      console.log("storage id : " + storageID);
      const response = getAllRootFolder(storageID)
        .then((value) => {
          setFolderList(value.rootFolders);
          console.log(value);
        })
        .catch((e) => {
          console.log(e);
        });
      console.log(response);
    }
  });
  return (
    <>
      <NewFolder storageID={storageID}/>
      {folderList !== null && (
          <FolderList folders={folderList} />
      )}
    </>
  );
}
