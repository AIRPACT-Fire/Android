package lar.wsu.edu.airpact_fire;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/* @Todo ---------------------------------------
 *      - Save data as JSON (let web team know structure)
 *      - Send data to server (web team )
  ---------------------------------------------*/

// Acting as the ImageCaptureActivity
public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    ImageView mImageView;
    Button mCameraButton, mUploadButton;
    EditText mEditText;
    TextView mDebugText;
    String mCurrentPhotoPath;
    // [Not real server URL]
    String mServerURL = "192.168.1.1:8000";
    String mUser = "root";

    // Temp method for sending http post request to server
    public void sendPOSTRequest() throws IOException, JSONException {
        URL url;
        String description;
        String response;
        Bitmap image;
        JSONObject jsonSend;
        JSONObject jsonRecieve;

        url = new URL (mServerURL);
        image = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        description = mEditText.getText().toString();

        // Create JSONObject here
        jsonSend = new JSONObject();
        jsonSend.put("user", mUser);
        jsonSend.put("description", description);
        jsonSend.put("image", image);

        // Establish connection and read/write from/to server
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            // Write json to url as byte[]
            out.write(jsonSend.toString().getBytes());

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            // TODO See if the below in.toString() is valid to do
            jsonRecieve = new JSONObject(in.toString());
        } finally {
            urlConnection.disconnect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set action bar stuff
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Attach views to variables
        mImageView = (ImageView) findViewById(R.id.captured_image_thumbnail);
        mCameraButton = (Button) findViewById(R.id.capture_image_button);
        mUploadButton = (Button) findViewById(R.id.upload_data_button);
        mEditText = (EditText) findViewById(R.id.description_edit_text);
        mDebugText = (TextView) findViewById(R.id.debug_text_display);

        // Create button event listeners
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        mUploadButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
                uploadData();
           }
        });
    }

    // Converts image to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        return stream.toByteArray();
    }

    // Create an image file with collision resistant title to public directory
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    // Takes picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                mEditText.setText("Error occurred while creating the file.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                // TODO For some reason, the below line was causing "data" to be null in our onActivityResult method
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Receives taken picture as thumbnail
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

            mDebugText.setText("Captured and saved image as '" + mCurrentPhotoPath + "'");
        }
    }

    // Upload JSON data to server
    private void uploadData()
    {
        packageDataToJSON();
        // TODO
        mDebugText.setText("\nPackaging up data in JSON and uploading to server.");
    }

    // Packages up all data into JSON object
    private void packageDataToJSON()
    {
        // TODO
    }
}
