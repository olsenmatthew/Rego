package com.reylo.rego.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class InternalStorage {

    //The following are for internal storage of message contents

    //Testing if the message content is internally stored
    public  static boolean isStoredInternally (String filename, Context context) {

        FileInputStream fileInputStream;
        BufferedReader bufferedReader;

        filename = filename + ".txt";
        StringBuilder rLine = new StringBuilder();
        String eLine = "";

        try {

            fileInputStream = context.openFileInput(filename);
            bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(fileInputStream)));

            while ((eLine = bufferedReader.readLine()) != null) {

                rLine.append(eLine);
                rLine.append(System.getProperty("line.separator"));

            }

            //if there is no text in the file, the file is empty
            if (rLine == null) {

                fileInputStream.close();
                return false;

            } else {

                fileInputStream.close();
                return true;

            }

        } catch (Exception e) {

            e.printStackTrace();
            Log.d("Could not read file ",
                    "Two reasons: file is not created yet or there is an error: " + e.toString());

        }

        return false;

    }

    //save the file and the file name under the uri
    public static void saveMessageContentInternally(String filename, String fileContents, Context context) {

        FileOutputStream outputStream;

        filename = filename + ".txt";

        try {

            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();


        } catch (Exception e) {

            e.printStackTrace();
            Log.d("Could not save file ",
                    "There is an error: " + e.toString());

        }

    }

    //Reading and returning the string contents of a saved file
    public static String getTextFileContents(String filename, Context context) {

        FileInputStream fileInputStream;
        BufferedReader bufferedReader;

        filename = filename + ".txt";
        StringBuilder rLine = new StringBuilder();
        String eLine = "";
        String rString = "";

        try {

            fileInputStream = context.openFileInput(filename);
            bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(fileInputStream)));

            while ((eLine = bufferedReader.readLine()) != null) {

                rLine.append(eLine);
                rLine.append(System.getProperty("line.separator"));

            }

            rString = ChompString.Chomp(rLine.toString());

            //if there is no text in the file, the file is empty
            if (rString == null) {

                fileInputStream.close();
                return filename;

            } else {

                fileInputStream.close();
                return rString;

            }

        } catch (Exception e) {

            e.printStackTrace();
            Log.d("Could not read file ",
                    "Two reasons: file is not created yet or there is an error: " + e.toString());

        }

        return rString;

    }

    //Does this files exist internally
    public static boolean internalFileExists(String filename, Context context) {

        File file = context.getFileStreamPath(filename);

        if(file == null || !file.exists()) {

            return false;

        }

        return true;

    }

    //save file to internal storage
    public static void saveFileInternally(StorageReference storageReference, File localFile, final Context context) {

        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                Log.d("file saved",
                        "file saved");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("Could not save file ",
                        "Two reasons: file is not created yet or there is an error: " + e.toString());

            }
        });

    }

}
