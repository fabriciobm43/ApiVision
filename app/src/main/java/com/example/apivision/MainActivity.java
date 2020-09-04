package com.example.apivision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Vision vision;
    ImageView imageView;
    Button btnProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(),  null);
        visionBuilder.setVisionRequestInitializer(new
                VisionRequestInitializer("AIzaSyB5MkIB5lNnQH1kC1tZ3ATeEsv7z66moKs"));
        vision = visionBuilder.build();

        /*imageView=(ImageView) findViewById(R.id.imgFamilia);
        btnProgress=(Button) findViewById(R.id.button);
        final Bitmap mybitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.familia);
        imageView.setImageBitmap(mybitmap);
        final Paint rectPaint= new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        final Bitmap tempBitmap=Bitmap.createBitmap(mybitmap.getWidth(),mybitmap.getHeight(), Bitmap.Config.RGB_565);
        final Canvas canvas= new Canvas(tempBitmap);
        btnProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setMode(FaceDetector.FAST_MODE)
                        .build();
                if (!faceDetector.isOperational()){
                    Toast.makeText(MainActivity.this,"face detector", Toast.LENGTH_SHORT).show();
                    return;
                }
                Frame frame = new Frame.Builder().setBitmap(mybitmap).build();
                SparseArray<Face> sparseArray = faceDetector.detect(frame);
                for(int i=0; i<sparseArray.size();i++){
                    Face face= sparseArray.valueAt(i);
                    float x1=face.getPosition().x;
                    float y1= face.getPosition().y;
                    float x2= x1+face.getWidth();
                    float y2 = y1+face.getHeight();
                    RectF rectF= new RectF(x1,y1,x2,y2);
                    canvas.drawRoundRect(rectF,2,2,rectPaint);

                }
                imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
            }
        });*/

    }




    public Image getImageToProcess(){
        ImageView imagen = (ImageView)findViewById(R.id.imgFamilia);
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        bitmap = scaleBitmapDown(bitmap, 1200);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

        byte[] imageInByte = stream.toByteArray();

        Image inputImage = new Image();
        inputImage.encodeContent(imageInByte);
        return inputImage;
    }

    public BatchAnnotateImagesRequest setBatchRequest(String TipoSolic, Image inputImage){
        Feature desiredFeature = new Feature();
        desiredFeature.setType(TipoSolic);

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));


        BatchAnnotateImagesRequest batchRequest =  new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));
        return batchRequest;
    }


    public void ProcesarTexto(View View){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BatchAnnotateImagesRequest batchRequest = setBatchRequest("FACE_DETECTION", getImageToProcess());
                try {

                    Vision.Images.Annotate  annotateRequest = vision.images().annotate(batchRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response  = annotateRequest.execute();


                    List<FaceAnnotation> faces = response.getResponses().get(0).getFaceAnnotations();

                            //final StringBuilder message = new StringBuilder("Se ha encontrado los siguientes Objetos:\n\n");
                            // final TextAnnotation text = response.getResponses().get(0).getFullTextAnnotation();
                    /*List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
                    if (labels != null) {
                        for (EntityAnnotation label : labels)
                               message.append(String.format(Locale.US, "%.2f: %s\n",
                                       label.getScore()*100, label.getDescription()));
                    } else {
                        message.append("No hay ningún Objeto");
                    }*/


                    int numberOfFaces = faces.size();
                    String likelihoods = "";
                    for(int i=0; i<numberOfFaces; i++)
                        likelihoods += "\n Rostro " + i + "  "  + faces.get(i).getJoyLikelihood();

                    final String message =   "Esta imagen tiene " + numberOfFaces + " rostros " + likelihoods;

                    /////////
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    Bitmap myBitmap = BitmapFactory.decodeResource(
                            getApplicationContext().getResources(),
                            R.drawable.familia,
                            options
                    );

                    Paint myRectPaint = new Paint();
                    myRectPaint.setStrokeWidth(5);
                    myRectPaint.setColor(Color.RED);
                    myRectPaint.setStyle(Paint.Style.STROKE);

                    final Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(),
                            Bitmap.Config.RGB_565);
                    Canvas tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                    FaceDetector faceDetector = new
                            FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                            .build();

                    if(!faceDetector.isOperational()) {
                        //new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detected!").show();
                        return;
                    }

                    Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                    SparseArray<Face> face = faceDetector.detect(frame);

                    for (int i = 0; i < face.size(); i++) {
                        Face thisFace = face.valueAt(i);
                        float x1 = thisFace.getPosition().x;
                        float y1 = thisFace.getPosition().y;
                        float x2 = x1 + thisFace.getWidth();
                        float y2 = y1 + thisFace.getHeight();
                        tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView)findViewById(R.id.txtTexto);
                            //imageDetail.setText(text.getText());
                            imageDetail.setText(message.toString());
                            imageView=(ImageView) findViewById(R.id.imgFamilia);
                            imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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