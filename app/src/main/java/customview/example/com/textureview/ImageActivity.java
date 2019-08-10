package customview.example.com.textureview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {


    private ImageView action_image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        action_image = (ImageView) findViewById(R.id.action_image);

        Bitmap imgBitmap = SessionData.getInstance().getImgBitmap();
        action_image.setImageBitmap(imgBitmap);



    }
}
