package save.your.privacy.metadataremover;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class MainActivity extends ActionBarActivity implements View.OnClickListener,MediaScannerConnectionClient {

    private Uri mImageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;

    private String appFolder ="MetadataRemover";

    private final String TAG = "MainActivity";


    public String[] allFiles;
    private String SCAN_PATH ;
    private static final String FILE_TYPE="image/*";

    private MediaScannerConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirIfNotExists(appFolder);
        Button btn_gallery = (Button) findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(this);
        Button btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(this);

        Button scanBtn = (Button)findViewById(R.id.btn_folder);
        scanBtn.setOnClickListener(this);
    }

    private void startScan()
    {
        Log.d("Connected", "success" + conn);
        if(conn!=null)
        {
            conn.disconnect();
        }
        conn = new MediaScannerConnection(getApplicationContext(),this);
        conn.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        String path     = "";

        if (requestCode == PICK_FROM_FILE) {
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                File fileDst = new File(Environment.getExternalStorageDirectory()+"/"+appFolder,getName());
                if (fileDst.exists()) {
                    fileDst.delete();
                }
                else
                {
                    try {
                        fileDst.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "Problem creating file");
                    }
                }
                FileOutputStream out = new FileOutputStream(fileDst);
                int read = 0;
                byte[] bytes = new byte[1024];
                while((read=stream.read(bytes)) != -1){
                    out.write(bytes,0,read);
                }
                out.close();
                stream.close();
            new RemoveMetadata().execute(fileDst.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(),"Removed metadata from the image",Toast.LENGTH_LONG).show();

        } else if (requestCode == PICK_FROM_CAMERA){
            try {
                InputStream stream = getContentResolver().openInputStream(mImageCaptureUri);
                File fileDst = new File(Environment.getExternalStorageDirectory()+"/"+appFolder,getName());
                if (fileDst.exists()) {
                    fileDst.delete();
                }
                else
                {
                    try {
                        fileDst.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "Problem creating file");
                    }
                }
                FileOutputStream out = new FileOutputStream(fileDst);
                int read = 0;
                byte[] bytes = new byte[1024];
                while((read=stream.read(bytes)) != -1){
                    out.write(bytes,0,read);
                }
                out.close();
                stream.close();
                new RemoveMetadata().execute(fileDst.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),"Could not remove metadata from the photo",Toast.LENGTH_LONG).show();
                Log.e(TAG, "FileNotFoundException");
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Could not remove metadata from the photo",Toast.LENGTH_LONG).show();
                Log.e(TAG, "IOException");
            } finally {
                File file =new File(mImageCaptureUri.getPath());
                boolean deleted = file.delete();
                if (deleted == false){
                    Toast.makeText(getApplicationContext(),"Could not delete temporary photo file",Toast.LENGTH_LONG).show();
                }
            }

            Toast.makeText(getApplicationContext(),"Removed metadata from the photo",Toast.LENGTH_LONG).show();
        }
    }

    public void onClick(View v){

        switch(v.getId())
        {
            case R.id.btn_gallery:
                Intent intentGallery = new Intent();

                intentGallery.setType("image/*");
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intentGallery, "Complete action using"), PICK_FROM_FILE);
                break;
            case R.id.btn_camera:
                Intent intentCamera    = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file        = new File(Environment.getExternalStorageDirectory(),
                        "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mImageCaptureUri = Uri.fromFile(file);

                try {
                    intentCamera.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intentCamera.putExtra("return-data", true);

                    startActivityForResult(intentCamera, PICK_FROM_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_folder:
                Intent folderIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                File folder = new File(Environment.getExternalStorageDirectory()+"/"+appFolder+"/");
                folderIntent.setData(Uri.fromFile(folder));
                //startActivityForResult(folderIntent, 1);
                startActivity(folderIntent);

                break;
        }
    }

    public boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File folder = new File(Environment.getExternalStorageDirectory(), path);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e(TAG, "Problem creating Image folder");
                ret = false;
            }
            else
            {
                Log.e(TAG, "Folder created");
                folder.toString();
                Log.e(TAG, "Folder: " + folder.toString());
            }
        }
        return ret;
    }

    private String getName(){
        Random generator = new Random();
        int n= 10000;
        n= generator.nextInt(n);
        return "Image-"+n+".jpg";
    }

    @Override
    public void onMediaScannerConnected() {
        Log.d("onMediaScannerConnected","success"+conn);
        conn.scanFile(SCAN_PATH, FILE_TYPE);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        try {
            Log.d("onScanCompleted",uri + "success"+conn);
            System.out.println("URI " + uri);
            if (uri != null)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            }
        } finally
        {
            conn.disconnect();
            conn = null;
        }
    }
}

