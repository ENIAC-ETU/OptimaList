package com.eniac.optimalist.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.utils.OCRRawItem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Word;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {
    Button btn_gallery;
    Button btn_camera;
    public static final int SELECT_PICTURE=1;
    private String selectedImagePath;
    ImageView img;
    private static final String TAG = "ImageActivity";
    private static ProgressDialog progressDialog;
    private static final int MAX_DIMENSION = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image2);
        btn_gallery = (Button) findViewById(R.id.button_image);
        btn_camera = (Button) findViewById(R.id.button_camera);
        img= (ImageView) findViewById(R.id.imageView);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
    @Override

    public void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(resultCode==RESULT_OK)
        {
            if(requestCode==SELECT_PICTURE)
            {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                img.setImageURI(selectedImageUri);

                try {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Görüntü işleniyor. Bu işlem yaklaşık 1 dakika sürecektir. Lütfen bekleyiniz...");
                    progressDialog.show();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    new CloudVisionTask(getBaseContext(), scaleBitmapDown(bitmap, MAX_DIMENSION)).execute();
                } catch (Exception e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private class CloudVisionTask extends AsyncTask<Object, Void, TextAnnotation> {
        private Context context;
        private Bitmap bitmap;

        private CloudVisionTask(Context context, Bitmap bitmap) {
            this.context = context;
            this.bitmap = bitmap;
        }

        @Override
        protected TextAnnotation doInBackground(Object... params) {
            try {
                //Init protocol
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                //Instantiate Vision
                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(new VisionRequestInitializer(context.getString(R.string.google_maps_key)));
                Vision vision = builder.build();

                //Create request
                BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{

                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                    Image base64EncodedImage = new Image();

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    base64EncodedImage.encodeContent(imageBytes);
                    annotateImageRequest.setImage(base64EncodedImage);

                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                        Feature labelDetection = new Feature();
                        labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                        labelDetection.setMaxResults(10);
                        add(labelDetection);
                    }});

                    add(annotateImageRequest);
                }});

                //Add properties
                Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                annotateRequest.setDisableGZipContent(true);
                BatchAnnotateImagesResponse response = annotateRequest.execute();

                return response.getResponses()
                        .get(0).getFullTextAnnotation();
            }
            catch (Exception e) {

                Log.e("ImageActivity", "failed to make API request because " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(TextAnnotation text) {
            super.onPostExecute(text);

            Log.d("ImageActivity", text.toString());
            List<OCRRawItem> ocrRawItems = new ArrayList<>();

            List<Block> blocks = text.getPages().get(0).getBlocks();
            for (int i = 0; i < blocks.size(); i++) {
                List<Paragraph> paragraphs = blocks.get(i).getParagraphs();
                for (int k = 0; k < paragraphs.size(); k++) {
                    StringBuilder line = new StringBuilder("");
                    List<Word> words = paragraphs.get(k).getWords();
                    for (int l = 0; l < words.size(); l++) {
                        List<Symbol> symbols = words.get(l).getSymbols();
                        for (int m = 0; m < symbols.size(); m++) {
                            Symbol symbol = symbols.get(m);
                            line.append(symbol.getText());
                            if (symbol.getProperty() != null && symbol.getProperty().getDetectedBreak() != null) {
                                switch (symbol.getProperty().getDetectedBreak().getType()) {
                                    case "SPACE":
                                        line.append(" ");
                                        break;
                                    case "EOL_SURE_SPACE":
                                        line.append(" ");
                                        Log.d(TAG, line.toString() + symbol.getBoundingBox().getVertices().toString());
                                        ocrRawItems.add(new OCRRawItem(
                                                line.toString(),
                                                symbol.getBoundingBox().getVertices().get(0).getX(),
                                                symbol.getBoundingBox().getVertices().get(0).getY()));
                                        line = new StringBuilder("");
                                        break;
                                    case "LINE_BREAK":
                                        Log.d(TAG, line.toString() + symbol.getBoundingBox().getVertices().toString());
                                        ocrRawItems.add(new OCRRawItem(
                                                line.toString(),
                                                symbol.getBoundingBox().getVertices().get(0).getX(),
                                                symbol.getBoundingBox().getVertices().get(0).getY()));
                                        line = new StringBuilder("");
                                        break;
                                }
                            }
                        }
                    }
                }
            }

            List<String> parsedLines = new ArrayList<>();
            int threshold = 10;
            for (int i = 0; i < ocrRawItems.size(); i++) {
                OCRRawItem item = ocrRawItems.get(i);
                StringBuilder line = new StringBuilder(item.getText());
                for (int k = i+1; k < ocrRawItems.size(); k++) {
                    OCRRawItem item2 = ocrRawItems.get(k);
                    if (Math.abs(item.getCoordY() - item2.getCoordY()) < threshold) {
                        if (item.getCoordX() < item2.getCoordX()) {
                            line.append(" ");
                            line.append(item2.getText());
                        }
                        else {
                            line.insert(0, " ");
                            line.insert(0, item2.getText());
                        }
                        item2.setUsed(true);
                    }
                }

                if (!item.isUsed()) {
                    parsedLines.add(line.toString());
                }
            }

            String result = TextUtils.join("\n", parsedLines);
            Log.d(TAG, result);
            setContentView(R.layout.ocr_result);
            EditText ocrResult = (EditText) findViewById(R.id.ocrResult);
            ocrResult.setText(result);
            progressDialog.dismiss();
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}
