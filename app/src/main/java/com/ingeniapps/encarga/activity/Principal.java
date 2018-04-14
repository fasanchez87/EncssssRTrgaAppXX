package com.ingeniapps.encarga.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.encarga.R;
import com.ingeniapps.encarga.app.Config;
import com.ingeniapps.encarga.fragment.Servicio;
import com.ingeniapps.encarga.vars.*;
import com.ingeniapps.encarga.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.encarga.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class Principal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Menu menuDrawer;
    private vars vars;
    private boolean cerrarDrawer=true;
    private DrawerLayout drawer;
    private ProgressDialog progressDialog;
    private NavigationView navigationView;
    private TextView textViewNameUser;
    private TextView textViewEmailUser;
    private gestionSharedPreferences gestionSharedPreferences;
    private boolean isNotifyPush;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public String currentVersion = null;
    Context context;
    private String html="";
    private String versionPlayStore="";
    Dialog dialog;
    private String tokenFCM;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Fabric.with(this, new Crashlytics());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gestionSharedPreferences=new gestionSharedPreferences(this);
        context = this;
        tokenFCM="";
        vars=new vars();
        drawer=(DrawerLayout)findViewById(R.id.drawer_layout);

        if(checkPlayServices())
        {
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
                Log.i("TokenFCM is: ",""+FirebaseInstanceId.getInstance().getToken());
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuDrawer = navigationView.getMenu();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        textViewNameUser = (TextView) navigationView.getHeaderView(0).findViewById(R.id.NombreUserHeaderNavGestion);
        textViewEmailUser = (TextView) navigationView.getHeaderView(0).findViewById(R.id.EmailHeaderNavGestion);
        textViewNameUser.setText("" + gestionSharedPreferences.getString("nomUsuario"));
        textViewEmailUser.setText("" + gestionSharedPreferences.getString("emaUsuario"));

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // checking for type intent filter
                if (intent.getAction().equals(Config.DEVELOPER_KEY))
                {

                    String message=intent.getExtras().getString("message");

                    AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(Principal.this, R.style.AlertDialogTheme));
                    builder
                            .setTitle("MEF")
                            .setMessage(""+message)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            }).setCancelable(false).show();
                }
            }
        };

        try
        {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        if (savedInstanceState==null)
        {
            Bundle extras = getIntent().getExtras();

            if (extras==null)
            {
                isNotifyPush=false;
                navigationView.setCheckedItem(R.id.nav_servicio);
                android.support.v4.app.FragmentManager fragmentManagerr = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransactionn = fragmentManagerr.beginTransaction();
                fragmentTransactionn.replace(R.id.frame_container, new Servicio());
                fragmentTransactionn.commit();
            }
            else
            {
                isNotifyPush = extras.getBoolean("isNotifyPush");

                if(isNotifyPush)
                {
                   /* navigationView.setCheckedItem(R.id.nav_gps);
                    android.support.v4.app.FragmentManager fragmentManagerr = getSupportFragmentManager();
                    android.support.v4.app.FragmentTransaction fragmentTransactionn = fragmentManagerr.beginTransaction();
                    fragmentTransactionn.replace(R.id.frame_container, new gps());
                    fragmentTransactionn.commit();*/
                }
            }
        }
    }

    public static int compareVersions(String version1, String version2)//COMPARAR VERSIONES
    {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++){
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0){
                return compare;
            }
        }
        return 0;
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    /*private void _webServicecheckVersionAppPlayStore()
    {
        String _urlWebService = "https://play.google.com/store/apps/details?id=com.ingeniapps.pide";

        StringRequest jsonObjReq = new StringRequest (Request.Method.GET, _urlWebService,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        html=response;
                        Document document= Jsoup.parse(html);
                        versionPlayStore=document.select("div[itemprop=softwareVersion]").first().ownText();
                        Log.i("softwareVersion","softwareVersion: "+versionPlayStore);

                        if(compareVersions(currentVersion,versionPlayStore) == -1)
                        {
                            if(!((Activity) context).isFinishing())
                            {
                                dialog = new Dialog(Principal.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setCancelable(false);
                                dialog.setContentView(R.layout.custom_dialog);

                                Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                                dialogButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=com.ingeniapps.pide"));
                                        startActivity(intent);
                                    }
                                });

                                dialog.show();
                            }
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
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }*/

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(new android.support.v7.view.ContextThemeWrapper(this, R.style.AlertDialogTheme));
            builder
                    .setTitle("Salir de la Aplicación")
                    .setMessage("¿Deseas salir de la aplicación justo ahora?")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {

                }
            }).show();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //COUNTER DE NOTIFICACIONES
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        /*guardarTokenFCMInstanceIDService(tokenFCM);

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_GPS));*/
      /*  //COUNTER DE NOTIFICACIONES
        _webServicecheckVersionAppPlayStore();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION_DESPACHADO));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION_CANCELADO));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION_CLIENTE_INFORMATIVO_GENERAL));

        NotificationUtils.clearNotifications(this);*/
    }

    private void guardarTokenFCMInstanceIDService(final String refreshedToken)
    {
        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/setToken");

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
                                //Toast.makeText(Principal.this, "Bien", Toast.LENGTH_SHORT).show();
                            }

                            else
                            {
                                //Toast.makeText(Principal.this, "Failed update token to server", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
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
                headers.put("tokenFCM", ""+refreshedToken);
                headers.put("codUsuario", ""+gestionSharedPreferences.getString("codUsuario"));
                headers.put("codSistema", "1");
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        Fragment fragment = null;
        Class fragmentClass;
        switch (item.getItemId())
        {
            case R.id.nav_noticias:
                cerrarDrawer=true;
                fragmentClass = Servicio.class;
                break;

            case R.id.nav_notificaciones:
                cerrarDrawer=true;
                fragmentClass = Servicio.class;
                break;

           /* case R.id.nav_suscripciones:
                cerrarDrawer=false;
                AlertDialog.Builder builder =new AlertDialog.Builder(new ContextThemeWrapper(Principal.this,R.style.AlertDialogTheme));
                builder
                        .setMessage("¿Deseas cerrar sesión?")
                        .setPositiveButton("SI", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                drawer.closeDrawer(GravityCompat.START);
                                //WebServiceCerrarSesion();
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                }).setCancelable(false).show();
                fragmentClass = prueba.class;
                break;*/

            default:
                fragmentClass = Servicio.class;
        }

        try
        {
            fragment = (Fragment) fragmentClass.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
        item.setChecked(true);

        if(cerrarDrawer)
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    /*private void WebServiceCerrarSesion()
    {
        progressDialog = new ProgressDialog(new ContextThemeWrapper(Principal.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cerrando sesión...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebServiceUpdateToken = vars.ipServer.concat("/ws/CerrarSesion");

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
                                progressDialog.dismiss();
                                gestionSharedPreferences.putBoolean("GuardarSesion", false);
                                gestionSharedPreferences.clear();
                                Intent i=new Intent(Principal.this, Login.class);
                                startActivity(i);
                                finish();
                            }

                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(Principal.this, "Error al cerrar sesión, contacta al administrador del servicio.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            //progressBar.setVisibility(View.GONE);
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        progressDialog.dismiss();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("codUsuario", ""+gestionSharedPreferences.getString("codUsuario"));
                return headers;
            }
        };
        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }*/
}
