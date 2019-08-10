package customview.example.com.textureview;

import android.content.Context;
import android.widget.Toast;

public class L
{
    public static L l= null;

    public static L get()
{

    L l= new L();

    return l;
}


    public void toast(Context context, String s)
    {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();


    }

}
