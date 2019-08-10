package customview.example.com.textureview;

import android.graphics.Bitmap;

public class SessionData {

    private static SessionData instance;

    public static SessionData getInstance()
    {
        if (instance == null)
        {
            instance = new SessionData();
        }

        return instance;
    }


    private byte[] img;
    private Bitmap imgBitmap;

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }
}
