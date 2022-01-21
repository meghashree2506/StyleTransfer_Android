package com.example.style_transfer;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Style#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Style extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TensorFlowInferenceInterface inferenceInterface;
    public int SELECT_PICTURE=200;
    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";

    private static final String INPUT_NODE = "input";
    private static final String STYLE_NODE = "style_num";
    private static final String OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid";

    private int selectedStyle = 1;

    private Uri fileUri = null;
    private int OPEN_CAMERA_FOR_CAPTURE = 0x1;

    private static final int NUM_STYLES = 26;
    private float[] styleVals = new float[NUM_STYLES];

    private int desiredSize = 256;
    private int[] intValues = new int[desiredSize * desiredSize];

    private float[] floatValues = new float[desiredSize * desiredSize * 3];
    public Bitmap bitmap;



    ImageView cameraImageView,style1,style2,style3;
    FloatingActionButton selectImage,saveImage,apply,sign_out;





    public Style() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Style.
     */
    // TODO: Rename and change types and number of parameters
    public static Style newInstance(String param1, String param2) {
        Style fragment = new Style();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            setHasOptionsMenu(true);
        }

    }

    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }

        return newBitmap;
    }


    private void stylizeImage(Bitmap bitmap) {
//        cameraImageView.setImageBitmap(bitmap);

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(INPUT_NODE, floatValues, 1, bitmap.getWidth(), bitmap.getHeight(), 3);
        inferenceInterface.feed(STYLE_NODE, styleVals, NUM_STYLES);

        // Execute the output node's dependency sub-graph.
        inferenceInterface.run(new String[] {OUTPUT_NODE}, false);

        // Copy the data from TensorFlow back into our array.
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }

        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        cameraImageView.setImageBitmap(bitmap);
        cameraImageView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                fileUri = data.getData();
//                Toast.makeText(getApplicationContext(),fileUri.getPath(),Toast.LENGTH_SHORT).show();
                if (null != fileUri) {
                    // update the preview image in the layout
                    cameraImageView.setImageURI(fileUri);
                    bitmap=((BitmapDrawable)cameraImageView.getDrawable()).getBitmap();
//                    Toast.makeText(getApplicationContext(),bitmap.toString(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View Clayout=inflater.inflate(R.layout.fragment_style, container, false);
        inferenceInterface = new TensorFlowInferenceInterface(getActivity().getAssets(), MODEL_FILE);
        final Matrix matrix=new Matrix();
        matrix.postScale(1f, 1f);
//        matrix.postRotate(90);

        cameraImageView=(ImageView)Clayout.findViewById(R.id.camera_image);
        apply=(FloatingActionButton) Clayout.findViewById(R.id.button);
        selectImage=(FloatingActionButton) Clayout.findViewById(R.id.selectImage);
        style1=(ImageView)Clayout.findViewById(R.id.style_1);
        style2=(ImageView)Clayout.findViewById(R.id.style_2);
        style3=(ImageView)Clayout.findViewById(R.id.style_3);
        saveImage=(FloatingActionButton) Clayout.findViewById(R.id.saveImage);



        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
            }

        });

        style1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                styleVals = new float[NUM_STYLES];
                selectedStyle = 1;
                styleVals[11] = 0.50f;
            }
        });
        style2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                styleVals = new float[NUM_STYLES];
                selectedStyle = 2;
                styleVals[19] = 0.50f;
            }
        });
        style3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                styleVals = new float[NUM_STYLES];
                selectedStyle = 3;
                styleVals[24] = 0.50f;
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                cameraImageView.invalidate();
                BitmapDrawable drawable = (BitmapDrawable) cameraImageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                Log.v("Sytle","in style");
                Toast.makeText(getActivity().getApplicationContext(),bitmap.toString(),Toast.LENGTH_LONG).show();
                bitmap=Bitmap.createScaledBitmap(bitmap, desiredSize, desiredSize, false);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,true);
                //float time0 = (float) System.currentTimeMillis();
                stylizeImage(bitmap);

                cameraImageView.setVisibility(View.VISIBLE);

            }
        });

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a storage reference from our app
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();


// Create a reference to 'images/mountains.jpg'
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                StorageReference userImagesRef = storageRef.child("images/users/"+user.getUid().toString()+"/"+date.toString()+".jpeg");

// While the file names are the same, the references point to different files
                userImagesRef.getName().equals(userImagesRef.getName());    // true
                userImagesRef.getPath().equals(userImagesRef.getPath());    // false

                // Get the data from an ImageView as bytes
                cameraImageView.setDrawingCacheEnabled(true);
                cameraImageView.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) cameraImageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = userImagesRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getActivity().getApplicationContext(),"Error uploading image!!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Toast.makeText(getActivity().getApplicationContext(),"Successfully uploaded the image!!",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


        return Clayout;

    }

}