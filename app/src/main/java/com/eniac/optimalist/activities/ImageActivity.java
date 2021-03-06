package com.eniac.optimalist.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.Manifest;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.adapters.OCRAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.services.RecommendationService;
import com.eniac.optimalist.utils.OCRParsedItem;
import com.eniac.optimalist.utils.OCRRawItem;
import com.eniac.optimalist.utils.PermissionUtils;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ImageActivity extends AppCompatActivity {
    Button btn_gallery;
    Button btn_camera;
    public static final int SELECT_PICTURE=1;
    private String selectedImagePath;
    ImageView img;
    private static final String TAG = "ImageActivity";
    private static ProgressDialog progressDialog;
    private static final int MAX_DIMENSION = 1200;
    private ListView lv;
    private OCRAdapter ocrAdapter;
    private String date = "";
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final String FILE_NAME = "temp.jpg";
    DBHelper db;
    public AutoCompleteTextView text;

    private static final String[] items = new String[] {
            "Kahve","Yumurta","Süt","Domates", "Peynir"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image2);
        db = DBHelper.getInstance(this);
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
                startCamera();
            }
        });

    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(resultCode == RESULT_OK) {
            Uri selectedImageUri = null;
            if(requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                //selectedImagePath = getPath(selectedImageUri);
            }
            else if (requestCode == CAMERA_IMAGE_REQUEST) {
                selectedImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            }

            if (selectedImageUri != null) {
                Log.d(TAG, selectedImageUri.toString());
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
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

            int threshold = 10;
            List<OCRParsedItem> parsedItems = new ArrayList<>();
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
                    String s = line.toString().toLowerCase();
                    if (s.contains("tarih") | s.contains("tarıh")) {
                        Pattern p = Pattern.compile("\\d\\d[\\.|/]\\d\\d[\\.|/]\\d\\d\\d?\\d?");
                        Matcher m = p.matcher(s);
                        if (m.find()) {
                            date = s.substring(m.start(), m.end());
                            Log.d("PatternMatch", "Date: " + date);
                        }
                    }
                    else if (s.contains("topkdv") || s.contains("toplam")) {
                        break;
                    }
                    else if (s.contains("*")) {
                        String[] splitLine = s.split("\\*");
                        Pattern p = Pattern.compile("(%)|([^\\s]*[0o1][18])");
                        Matcher m = p.matcher(splitLine[0]);
                        if (m.find()) {
                            String productName = line.toString().substring(0, m.start()).trim();
                            String price = splitLine[1].replace(',', '.').replaceAll("[^\\d.]", "");
                            Log.d("PatternMatch", "Name: " + productName + ", Price: " + price);
                            parsedItems.add(new OCRParsedItem(productName, price, "Seçilmedi"));
                        }
                    }
                }
            }

            String parsedResult = TextUtils.join("\n", parsedItems);
            Log.d(TAG, parsedResult);
            setContentView(R.layout.ocr_result);
            lv = (ListView) findViewById(R.id.listView);
            ocrAdapter = new OCRAdapter(context, parsedItems);
            lv.setAdapter(ocrAdapter);
            final EditText dateText = (EditText) findViewById(R.id.ocrDate);
            final EditText listName = (EditText) findViewById(R.id.ocrListName);
            dateText.setText(date);
            Button acceptOCR = (Button) findViewById(R.id.acceptOCR);

            acceptOCR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PatternMatch", "Date: " + dateText.getText() + ", Parsed items: " + OCRAdapter.ocrParsedItemList.toString());

                    ShoppingList list = createShoppingListFromOCR(listName.getText().toString(), OCRAdapter.ocrParsedItemList, dateText.getText().toString());
                    setResult((int)list.getId());
                    finish();



                }
            });

            Button add_item_OCR = (Button) findViewById(R.id.add_item_OCR);

            add_item_OCR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                add_OCR_item_dialog();

                }
            });


            progressDialog.dismiss();
        }
    }


    private void add_OCR_item_dialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_ocr_item_dialog, null);


        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ImageActivity.this);
        alertDialogBuilderUserInput.setView(view);

        //final EditText inputItemName = view.findViewById(R.id.add_ocr_item);

        text=(AutoCompleteTextView) view.findViewById(R.id.add_ocr_item);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, items);
        text.setAdapter(adapter);
        text.setThreshold(1);

        final EditText inputItemName = text;


        final EditText inputPrice = view.findViewById(R.id.ocr_price_input);

        String[] categories={
                "Kategori Seçiniz...",
                "Meyve, Sebze",
                "Et, Balık",
                "Süt, Kahvaltılık",
                "Gıda, Şekerleme",
                "İçecek",
                "Deterjan, Temizlik",
                "Kağıt, Kozmetik",
                "Bebek, Oyuncak",
                "Ev, Pet"};

        final Spinner categorySpinner = (Spinner) view.findViewById(R.id.add_ocr_item_category);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        TextView dialogTitle = view.findViewById(R.id.add_ocr_item_dialog_title);
        dialogTitle.setText(getString(R.string.new_item));


        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("iptal",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                alertDialog.dismiss();

                if (inputPrice.getText().toString().trim().isEmpty()) {
                    inputPrice.setText("0");
                }

                add_OCR_item(inputItemName.getText().toString(), inputPrice.getText().toString(),(String) categorySpinner.getSelectedItem());


            }
        });

    }

    private void add_OCR_item(String itemName, String price, String category){

        OCRParsedItem new_ocr_item = new OCRParsedItem(itemName, price, category);
        OCRAdapter.ocrParsedItemList.add(new_ocr_item);

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

    public ShoppingList createShoppingListFromOCR(String name, List<OCRParsedItem> parsedItems, String date){
        //creates a new shopping list

        String[] dateArr = date.split("\\.", 3);
        String formattedDate = dateArr[2] + "-" + dateArr[1] + "-" + dateArr[0] + " 00:00:00";
        // inserting shopping list in db and getting
        // newly inserted shopping list id
        long id = db.insertShoppingList(name,0, formattedDate);

        // get the newly inserted shopping list from db
        ShoppingList list = db.getShoppingList(id);

        for(OCRParsedItem item:parsedItems) {
            long itemId = db.insertItemList(item.getName(),1 , Float.parseFloat(item.getPrice()),item.getCategory(),list.getId(), formattedDate);
        }
        return list;
    }
}
