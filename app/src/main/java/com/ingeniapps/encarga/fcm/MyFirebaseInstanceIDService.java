package com.ingeniapps.encarga.fcm;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.ingeniapps.encarga.app.Config;
import com.ingeniapps.encarga.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.encarga.vars.vars;
import com.ingeniapps.encarga.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//import com.google.android.gms.iid.InstanceID;

/**
 * Created by FABiO on 20/01/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{

    private static final String TAG = "MyFirebaseIIDService";
    vars var;
    private gestionSharedPreferences sharedPreferences;
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh()
    {
        super.onTokenRefresh();
        var = new vars();
        sharedPreferences = new gestionSharedPreferences(this);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        storeRegIdInPref(refreshedToken);
        sharedPreferences.putString("tokenFCM",refreshedToken);

        if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
        {
            guardarTokenFCMInstanceIDService(refreshedToken);
        }
        else
        {
            Log.i("fcm","No es posible actualizar FCM codUsuario is NULL");
        }

        Log.i("fcm","Token Update From IntentIDService: "+sharedPreferences.getString("tokenFCM"));
    }

    private void guardarTokenFCMInstanceIDService(final String refreshedToken)
    {
        String _urlWebServiceUpdateToken = var.ipServer.concat("/ws/UpdateTokenFCM");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebServiceUpdateToken, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status = response.getBoolean("status");
                            String message = response.getString("message");

                            if(status)
                            {
                                //Toast.makeText(MyFirebaseInstanceIDService.this, "Update token FCM to Server now: instance service", Toast.LENGTH_SHORT).show();
                            }

                            else
                            {
                              // Toast.makeText(MyFirebaseInstanceIDService.this, "Error, FCM al Server:UpdateTokenFCM: instance service", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            Log.e(TAG, ""+ e.getMessage().toString());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                    }

                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("idDevice", ""+sharedPreferences.getString("idDevice"));
                headers.put("tokenFCM", ""+FirebaseInstanceId.getInstance().getToken());
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "FCM_Firebase_Cambios");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("FCM_Firebase_Cambios");
    }

    private void storeRegIdInPref(String token)
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.commit();
    }
}