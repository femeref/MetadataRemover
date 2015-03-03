package save.your.privacy.metadataremover;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Uri mImageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_gallery = (Button) findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(this);
        Button btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            path = getRealPathFromURI(mImageCaptureUri); //from Gallery
            new RemoveMetadata().execute(path);
        } else {
            path    = mImageCaptureUri.getPath();
            new RemoveMetadata().execute(path);
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
}
