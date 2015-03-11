package save.your.privacy.metadataremover;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
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

        File folder = new File("/sdcard/"+appFolder+"/");
        allFiles = folder.list();
        //   uriAllFiles= new Uri[allFiles.length];
        for(int i=0;i<allFiles.length;i++)
        {
            Log.d("all file path"+i, allFiles[i]+allFiles.length);
        }
        //  Uri uri= Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString()+"/yourfoldername/"+allFiles[0]));


        SCAN_PATH=Environment.getExternalStorageDirectory().toString()+"/"+appFolder+"/"+allFiles[0];
        System.out.println(" SCAN_PATH  " +SCAN_PATH);

        Log.d("SCAN PATH", "Scan Path " + SCAN_PATH);
        Button scanBtn = (Button)findViewById(R.id.btn_folder);
        scanBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startScan();
            }});
        }

        private void startScan()
        {
            Log.d("Connected", "success" + conn);
            if(conn!=null)
            {
                conn.disconnect();
            }
            conn = new MediaScannerConnection(this,this);
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
            mImageCaptureUri = data.getData();
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                path    = mImageCaptureUri.getPath();
                //path = getRealPathFromURI(mImageCaptureUri); //from Gallery
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
                Log.e(TAG, "FileDst: "+fileDst.getAbsolutePath());
                Log.e(TAG, "FileDst path: "+fileDst.getPath());
                Log.e(TAG, "FileDst name: "+fileDst.getName());
                Log.e(TAG, "FileDst parent: "+fileDst.getParent());
                    Log.e(TAG, "FileDst canonical path: "+fileDst.getCanonicalPath());

                /*File fileSrc = new File(mImageCaptureUri.toString());
                Log.e(TAG, "FileSrc: "+fileSrc.getAbsolutePath());
                try {
                    copyFile(fileSrc,fileDst);
                } catch (IOException e) {
                    Log.e(TAG, "Problem copying file");
                    e.printStackTrace();
                }*/
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

        } else {
            mImageCaptureUri =  data.getData();
            path    = mImageCaptureUri.getPath();
            //path = getRealPathFromURI(mImageCaptureUri); //from Camera
            File fileDst = new File(getApplicationContext().getFilesDir(),getName());
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
            Log.e(TAG, "FileDst path: "+fileDst.getPath());
            File fileSrc = new File(mImageCaptureUri.toString());
            Log.e(TAG, "FileSrc: "+fileSrc.getAbsolutePath());
            try {
                copyFile(fileSrc,fileDst);
            } catch (IOException e) {
                Log.e(TAG, "Problem copying file");
            }
            Log.e(TAG, "Remove metadata of "+ fileDst.getAbsolutePath());
            new RemoveMetadata().execute(fileDst.getAbsolutePath());
            Toast.makeText(getApplicationContext(),"Removed metadata from the photo",Toast.LENGTH_LONG).show();
        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String [] proj      = {MediaStore.Images.Media.DATA};
        Cursor cursor       = managedQuery( contentUri, proj, null, null,null);

        if (cursor == null) return null;

        int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);
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

    /*public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(dst);
        OutputStream out = new FileOutputStream(src);
        Log.e(TAG, "copy file 1");
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        Log.e(TAG, "copy file 2");
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        Log.e(TAG, "copy file 3");
        in.close();
        out.close();
        Log.e(TAG, "copy file 4");
    }*/

    public void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
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
                intent.setData(uri);
                startActivity(intent);
            }
        } finally
        {
            conn.disconnect();
            conn = null;
        }
    }
}

