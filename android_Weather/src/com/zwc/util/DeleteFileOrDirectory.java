package com.zwc.util;

import java.io.File;

public class DeleteFileOrDirectory {
	public static boolean delete(File file) {
		boolean flag = false;
		if (file.exists()) {

			if (file.isFile())
			{
				if (!deleteFile(file))
					return false;
			}
			else
			{
				if (!deleteDirectory(file))
					return false;
			}
			
			flag = true;
		}

		return flag;

	}

	private static boolean deleteFile(File file) {
		if (file.exists()) {
			return file.delete();
		}
		else 
		{	
			//该文件不存在
			return false;
		}

	}

	private static boolean deleteDirectory(File file) {
		if (file.exists())
		{
			File[] files = file.listFiles();
			for (int i = 0; files != null && i < files.length; i++) {
				if (files[i].isFile())
				{
					if (!deleteFile(files[i]))
						return false;
				}
				else
				{
					if (!deleteDirectory(files[i]))
						return false;
				}
				
			}
			//最后再删除该目录
			file.delete();
			
			return true;
		}
		else 
		{
			return false;
		}

	}
	
}

