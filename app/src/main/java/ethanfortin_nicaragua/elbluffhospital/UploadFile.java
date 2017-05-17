package ethanfortin_nicaragua.elbluffhospital;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class UploadFile extends AppCompatActivity {

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private ImageView ivImage;
    private DatePicker datePicker;
    private String stDate, userChosenTask;
    private TextView tvDate;
    private Button chooseImage, chooseDate, upload;
    private Uri filePath;
    private boolean gallery;
    private boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        chooseImage = (Button)findViewById(R.id.image);
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked on button.");
                selectImage();
            }
        });

        chooseDate = (Button)findViewById(R.id.date);
        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });

        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ready) uploadMultipart(gallery);
            }
        });

        ivImage = (ImageView)findViewById(R.id.ivImage);
        tvDate = (TextView)findViewById(R.id.date_string);
    }

    private void selectDate() {
        LayoutInflater inflater = LayoutInflater.from(UploadFile.this);
        final View subView = inflater.inflate(R.layout.dialog_select_date, null);
        datePicker = (DatePicker) subView.findViewById(R.id.datePicker);

        final AlertDialog builder = new AlertDialog.Builder(this)
                .setView(subView)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int day = datePicker.getDayOfMonth();
                        int month = datePicker.getMonth() + 1;
                        int year = datePicker.getYear();
                        stDate = year + "-" + month + "-" + day;
                        tvDate.setText(stDate);
                        tvDate.setVisibility(View.VISIBLE);
                        dialog.cancel();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })

                .create();
        builder.show();
    }

    private void selectImage() {
        final CharSequence[] items = { "Tomar un foto", "Eligir de galería", "Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadFile.this);
        builder.setTitle("Eligir imagen");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(UploadFile.this);
                if (items[item].equals("Tomar un foto")) {
                    userChosenTask = "Tomar un foto";
                    gallery = false;
                    if(result) cameraIntent();

                } else if (items[item].equals("Eligir de galería")) {
                    userChosenTask = "Eligir de galería";
                    gallery = true;
                    if(result) galleryIntent();

                } else if (items[item].equals("Calcelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(photoFile != null)
            {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Image_" + timeStamp + "_";

        File storageDirectory = getExternalFilesDir("");
        File image = File.createTempFile(imageFileName, ".jpg",storageDirectory);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChosenTask.equals("Tomar un foto"))
                        cameraIntent();
                    else if(userChosenTask.equals("Eligir de galería"))
                        galleryIntent();
                } else {
                    System.out.println("DENIED");
                }
                break;
        }
    }

    private void onCaptureImageResult(Intent data) {

        File myPath = getExternalFilesDir(null);

        filePath = Uri.parse("file://" + myPath.listFiles()[myPath.listFiles().length - 1].getAbsolutePath());
        System.out.println("FilePath = " + filePath);
        Bitmap bm = null;
        try {
            bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivImage.setImageBitmap(bm);
        ready = true;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            filePath = data.getData();
            System.out.println("FilePath = " + filePath);
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
        ready = true;
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    public void uploadMultipart(boolean gallery) {
        // To name the image, use combination of the date and the patient's invisible ID
        // TODO add code to get date from activity, and id from bundle

        Intent intent = getIntent();
        int id = intent.getIntExtra("invpid", -1);
        String date = "2017-05-05";

        //getting the actual path of the image
        String path;
        if(gallery) {
            path = getPath(filePath);
        } else {
            path = filePath.getPath();
        }

        System.out.println("filePath = " + filePath);
        System.out.println("path = " + path);
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, ConnVars.URL_UPLOAD)
                    .addFileToUpload(path, "image") // Adding file
                    .addParameter("date", date) // Adding text parameter to the request
                    .addParameter("id", String.valueOf(id))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); // Starting the upload

            Toast.makeText(this, "Image successfully uploaded.", Toast.LENGTH_SHORT).show();

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
