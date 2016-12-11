package parkhomov.andrew.upload2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener /*  implementing click listener */ {
    //a constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;

    //Buttons
    private Button buttonChoose;

    //ImageView
    private ImageView imageView, imageView2, imageView3;
    private StorageReference storageReference;
    private StorageReference riversRef;
    private ImageButton imageButtonaddPhoto;
    private ViewFlipper viewFlipper;
    private ArrayList<ImageView> imageViewList;
    private int imageCounter = 0;


    //a Uri object to store file path
    private Uri filePath;

    private String filename;
    private float lastX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting views from layout
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        imageButtonaddPhoto = (ImageButton) findViewById(R.id.imageButton);
        imageButtonaddPhoto.setEnabled(false);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);

        imageViewList = new ArrayList<>();
        //imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_fiber_new_black_24dp));
        imageViewList.add(imageView);
        imageViewList.add(imageView3);
        imageViewList.add(imageView2);
        //attaching listener
        buttonChoose.setOnClickListener(this);
        imageButtonaddPhoto.setOnClickListener(this);
        //buttonUpload.setOnClickListener(this);
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            //downloadFile(filePath);
            String path = filePath.getPath();

            if (path.length() > 0) {
                filename = path.substring(path.lastIndexOf("/") + 1,path.length());
                //Toast.makeText(getApplicationContext(), filename, Toast. LENGTH_LONG).show();
            }
            try {
                // resize image
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                // reduce image weight
//                float originalWidth = bitmap.getWidth();
//                float originalHeight = bitmap.getHeight();
//
//                Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)originalWidth/2, (int)originalHeight/2, true);
//
//                ByteArrayOutputStream blob = new ByteArrayOutputStream();
//                resized.compress(Bitmap.CompressFormat.JPEG, 50, blob);
//                byte[] bitmapdata = blob.toByteArray();
//                Bitmap f = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
//
//                TextView tv = (TextView) findViewById(R.id.textView);
//                tv.setText("First image size:      " + bitmap.getByteCount() + "\nSecond image size: "+
//                        f.getByteCount());
//                imageView.setImageBitmap(bitmap);
//                imageView2.setImageBitmap(f);
                uploadDownloadFile(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(imageCounter != 0){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    float currentX = event.getX();

                    if(lastX < currentX){
                        if(viewFlipper.getDisplayedChild() == 0) {
                            break;
                        }


                        viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

                        // Current screen goes out from right.

                        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                        viewFlipper.showNext();
                    }

                    if(lastX > currentX){
                        if(viewFlipper.getDisplayedChild() == 1){
                            break;
                        }

                        viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);
                        // Current screen goes out from left.

                        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                        viewFlipper.showPrevious();
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        //if the clicked button is choose
        if (view == buttonChoose) {
            showFileChooser();
        }
        if (view == imageButtonaddPhoto){
            imageCounter++;
            showFileChooser();
        }
    }

//    private Bitmap resizeImage (Bitmap bitmap) {
//        float originalWidth = bitmap.getWidth();
//        float originalHeight = bitmap.getHeight();
//
//        Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)originalWidth/2, (int)originalHeight/2, true);
//
//        ByteArrayOutputStream blob = new ByteArrayOutputStream();
//        resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
//        resized.recycle();
//        resized = null;
//        byte[] bitmapdata = blob.toByteArray();
//
//        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
//    }

    private void uploadDownloadFile(Bitmap bitmap){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();

        storageReference = FirebaseStorage.getInstance().getReference();
        riversRef = storageReference.child("images").child(filePath.getLastPathSegment());

        // reduce image weight
        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,(int)originalWidth/2, (int)originalHeight/2, true);

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 50, blob);
        resized.recycle();
        resized = null;
        byte[] bitmapdata = blob.toByteArray();

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//        byte[] data = baos.toByteArray();

        UploadTask uploadTask = riversRef.putBytes(bitmapdata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                progressDialog.dismiss();
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                ImageView im = imageViewList.get(imageCounter);
                if(imageCounter != 0){
                    viewFlipper.showPrevious();
                }else{
                    imageButtonaddPhoto.setEnabled(true);
                    buttonChoose.setEnabled(false);
                }


                Picasso.with(MainActivity.this).load(downloadUri).into(im);
            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //calculating progress percentage
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                //displaying percentage in progress dialog
                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        });
    }

    public void downloadFileOld(Uri uriData) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        storageReference = FirebaseStorage.getInstance().getReference();
        riversRef = storageReference.child("images").child(uriData.getLastPathSegment());
        riversRef.putFile(uriData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //if the upload is successfull
                        //hiding the progress dialog
                        //progressDialog.dismiss();

                        //and displaying a success toast
                        progressDialog.dismiss();

                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        Picasso.with(MainActivity.this).load(downloadUri).resize(800, 800).centerInside().into(imageView);
                        //Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //if the upload is not successfull
                        //hiding the progress dialog
                        //progressDialog.dismiss();

                        //and displaying error message
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //calculating progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });
    }
}
