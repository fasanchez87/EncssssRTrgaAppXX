package com.ingeniapps.encarga.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.encarga.R;
import com.ingeniapps.encarga.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.encarga.vars.vars;
import com.ingeniapps.encarga.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity
{
    //COMPONENTES INTERFAZ
    EditText nomUsuarioRegistro,numDocUsuarioRegistro,telUsuarioRegistro,editTextCorreoUsuarioRegistro,claveUsuario,confirmarClaveUsuario;
    Button buttonRegistroUsuario;
    TextView editTextTerminos;
    Spanned Text;
    //VARIABLES
    public gestionSharedPreferences sharedPrefences;
    private boolean checkTerminos;
    public vars vars;
    private String tokenFCM;
    private ProgressDialog progressDialog;
    //VARIABLE USUARIO
    private String nombre;
    private String cedula;
    private String telefono;
    private String email;
    private String clave;
    private String confirmarClave;
    //IDENTIFICACION DEL TELEFONO
    private String idDevice;
    //VERSION DEL APP INSTALADA
    private String versionActualApp;
    //SISTEMA OPERATIVO
    private String sistemaOperativo;

    gestionSharedPreferences gestionSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gestionSharedPreferences=new gestionSharedPreferences(this);
        //OBTENEMOS TOKEN FCM
        tokenFCM= FirebaseInstanceId.getInstance().getToken();//TOKEN FCM
        //Toast.makeText(this,"Token FCM: "+tokenFCM,Toast.LENGTH_LONG).show();
        //INICIALIZAR VARIABLES
        sharedPrefences=new gestionSharedPreferences(this);
        checkTerminos=false;
        vars=new vars();
        //IDENTIFICACION DISPOSITIVO
        idDevice= InstanceID.getInstance(getApplicationContext()).getId();
        //VERSION APP
        try
        {
            versionActualApp=getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }


        sistemaOperativo="1";
        //INICIALIZAR COMPONENTES DE INTERFAZ
        nomUsuarioRegistro=(EditText)findViewById(R.id.nomUsuarioRegistro);
        numDocUsuarioRegistro=(EditText)findViewById(R.id.numDocUsuarioRegistro);
        telUsuarioRegistro=(EditText)findViewById(R.id.telUsuarioRegistro);
        editTextCorreoUsuarioRegistro=(EditText)findViewById(R.id.editTextCorreoUsuarioRegistro);
        claveUsuario=(EditText)findViewById(R.id.claveUsuario);
        confirmarClaveUsuario=(EditText)findViewById(R.id.confirmarClaveUsuario);
        buttonRegistroUsuario=(Button)findViewById(R.id.buttonRegistroUsuario);
        editTextTerminos=(TextView)findViewById(R.id.editTextTerminos);
        Text = Html.fromHtml("Click para ver terminos y condiciones. <br />" +
                "<a href='http://ingeniapps.com.co///'>Acepto los términos y condiciones.</a>");
        editTextTerminos.setMovementMethod(LinkMovementMethod.getInstance());
        editTextTerminos.setText(Text);
        //EVENTO BOTON REGISTRO
        buttonRegistroUsuario.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                nombre=nomUsuarioRegistro.getText().toString();
                cedula=numDocUsuarioRegistro.getText().toString();
                telefono=telUsuarioRegistro.getText().toString();
                email=editTextCorreoUsuarioRegistro.getText().toString();
                clave=claveUsuario.getText().toString();
                confirmarClave=confirmarClaveUsuario.getText().toString();

                if (TextUtils.isEmpty(nombre))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu nombre.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (nombre.trim().length() < 5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita un nombre valido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(cedula) && cedula.trim().length() < 5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu cédula.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (cedula.trim().length() < 5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita una cédula válida.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(telefono))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu teléfono.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (telefono.trim().length()<4)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita un teléfono valido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }


                if (TextUtils.isEmpty(email))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita tu email.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (!(isValidEmail(email)))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digita un email válido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(confirmarClave))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Confirma tu clave.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(clave))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Dígita tu clave.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (clave.trim().length()<5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Tu clave debe tener al menos (5) dígitos.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if(!confirmarClave.equals(clave))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Las claves no coinciden.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if(!checkTerminos)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                    builder
                            .setTitle("Encarga")
                            .setMessage("Debe aceptar los términos y condiciones para finalizar el registro.")
                            .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int id)
                                {

                                }
                            }).show().getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                    return;
                }

                WebServiceRegistroUsuario();
            }
        });
    }

    public final static boolean isValidEmail(CharSequence target)
    {
        if (target == null)
        {
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public void checkEventTerminos(View v)
    {
        CheckBox checkBox = (CheckBox)v;
        if(checkBox.isChecked())
        {
            checkTerminos=true;
        }
        else
        {
            checkTerminos=false;
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
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

    private void WebServiceRegistroUsuario()
    {
        String _urlWebService=vars.ipServer.concat("/ws/RegistroUsuario");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Registro.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrando...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            Boolean status=response.getBoolean("status");
                            String message=response.getString("message");

                            if(status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Encarga")
                                        .setMessage(message)
                                        .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                Intent intent=new Intent(Registro.this,Login.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                            else
                            if (!status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("ESTADO REGISTRO")
                                        .setMessage(message)
                                        .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }
                        catch (JSONException e)
                        {
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Registro.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle("ESTADO REGISTRO")
                                .setMessage(error.toString())
                                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                    }
                                }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));

                        if (error instanceof TimeoutError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            progressDialog.dismiss();
                            builder
                                    .setTitle("ESTADO REGISTRO")
                                    .setMessage(error.toString())
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    }
                })
        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                headers.put("ideDevice", idDevice);
                headers.put("cedUsuario", cedula);
                headers.put("tokenFCM", tokenFCM);
                headers.put("telUsuario", telefono);
                headers.put("nombresUsuario", nombre);
                headers.put("claveUsuario", clave);
                headers.put("emailUsuario", email);
                headers.put("sistemaOperativo", sistemaOperativo);
                headers.put("versionApp", versionActualApp);
                return headers;
            }

        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /*private void WebServiceVersionAppPlayStore()
    {
        String _urlWebService = "http://carreto.pt/tools/android-store-version/?package=com.ingeniapps.weser";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status = response.getBoolean("status");

                            if(status)
                            {
                                if(compareVersions(versionActualApp,response.getString("version")) == -1)
                                {
                                    if(!((Activity) context).isFinishing())
                                    {
                                        //show dialog
                                        dialog = new Dialog(Inicio.this);
                                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        dialog.setCancelable(false);
                                        dialog.setContentView(R.layout.custom_dialog);

                                        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
                                        //text.setText(msg);

                                        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
                                        dialogButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse("market://details?id=com.ingeniapps.vestra"));
                                                startActivity(intent);
                                            }
                                        });

                                        dialog.show();
                                    }
                                }
                            }
                        }

                        catch (JSONException e)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Inicio.this);
                            builder
                                    .setTitle("ERROR")
                                    .setMessage("Error consultando versiones en Play Store, contacte al admin de Beya.")
                                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {

                                        }
                                    }).setCancelable(true).show();

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
                HashMap<String, String> headers = new HashMap <String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("WWW-Authenticate", "xBasic realm=".concat(""));
                return headers;
            }

        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }*/
}