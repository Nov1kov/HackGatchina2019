package ru.tudimsudim.hackgatchina.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import ru.tudimsudim.hackgatchina.R;
import ru.tudimsudim.hackgatchina.model.Issue;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpJavaUtils {

    public static byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static void uploadBitmap(final Context context, final Bitmap bitmap, final Issue issue) {

        //getting the tag from the edittext
        final String url = HttpClient.INSTANCE.getAddress() + "/images";

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String imageUrl = new String(response.data);
                        issue.setImageUrl(imageUrl);
                        String resultMessage = context.getString(R.string.image_uploaded);
                        Toast.makeText(context, imageUrl, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                        Toast.makeText(context, error.getMessage() + " code: " + statusCode, Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(context).add(volleyMultipartRequest);
    }
}
