package com.ingeniapps.encarga.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ingeniapps.encarga.R;
import com.ingeniapps.encarga.vars.vars;
import com.ingeniapps.encarga.sharedPreferences.gestionSharedPreferences;
import com.ingeniapps.encarga.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity
{
    TextView textViewRegistroUsuario,textViewRecordarClaveLogin;
    TextView editTextEmail;
    EditText email;
    EditText clave;
    public vars vars;
    private String emailUsuario,claveUsuario,MyToken,tokenFCM;
    private Button botonLogin;
    gestionSharedPreferences gestionSharedPreferences;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog progressDialog;

    private Boolean guardarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        vars=new vars();
        gestionSharedPreferences=new gestionSharedPreferences(this);

        //COMPROBAMOS LA SESION DEL USUARIO
        guardarSesion=gestionSharedPreferences.getBoolean("GuardarSesion");
        if (guardarSesion==true)
        {
            cargarActivityPrincipal();
        }

        //EVENTOS INTERFAZ
        textViewRegistroUsuario=(TextView) findViewById(R.id.textViewRegistroLogin);
        textViewRegistroUsuario.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
            }
        });

        textViewRecordarClaveLogin = (TextView) findViewById(R.id.textViewRecordarClaveLogin);
        textViewRecordarClaveLogin.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Login.this, ValidarEmail.class);
                startActivity(intent);
            }
        });

        email=(EditText)findViewById(R.id.editTextCorreoUsuario);
        clave=(EditText)findViewById(R.id.editTextClaveUsuario);

        botonLogin=(Button)findViewById(R.id.buttonIngresar);
        botonLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                emailUsuario=email.getText().toString();
                claveUsuario=clave.getText().toString();

                if (TextUtils.isEmpty(emailUsuario)||!(isValidEmail(emailUsuario)))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite un email valido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(claveUsuario))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite su contraseña.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

               WebServiceLogin();
            }
        });

        if(checkPlayServices())
        {
            //revisar que de primerazo obtenga tokenFCM
            //tokenFCM=FirebaseInstanceId.getInstance().getToken();
            if(!TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()))
            {
                gestionSharedPreferences.putString("tokenFCM", "" + FirebaseInstanceId.getInstance().getToken());
                tokenFCM=FirebaseInstanceId.getInstance().getToken();
                //Toast.makeText(this, "Token FCM: " + "" + gestionSharedPreferences.getString("tokenFCM"), Toast.LENGTH_LONG).show();
                //Toast.makeText(this,"Token FCM: "+ tokenFCM,Toast.LENGTH_LONG).show();
            }

            else
            {
                //Toast.makeText(this,"Token FCM: "+"Aún no se ha generado Token FCM", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
            builder
                    .setTitle("GOOGLE PLAY SERVICES")
                    .setMessage("Se ha encontrado un error con los servicios de Google Play, actualizalo y vuelve a ingresar.")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                    setTextColor(getResources().getColor(R.color.colorPrimary));
        }
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

    public void cargarActivityPrincipal()
    {
        Intent intent = new Intent(Login.this, Principal.class);
        startActivity(intent);
        Login.this.finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("login");
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

    private void WebServiceLogin()
    {
        String _urlWebService=vars.ipServer.concat("/ws/Login");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(Login.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Validando usuario...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        JsonObjectRequest jsonObjReq=new JsonObjectRequest(Request.Method.GET, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            boolean status=response.getBoolean("status");
                            String message=response.getString("message");

                            if(status)
                            {
                                gestionSharedPreferences.putBoolean("GuardarSesion", true);
                                gestionSharedPreferences.putString("nomUsuario",""+response.getString("nomUsuario"));
                                gestionSharedPreferences.putString("emaUsuario",""+response.getString("emaUsuario"));
                                gestionSharedPreferences.putString("MyToken",""+response.getString("MyToken"));
                                gestionSharedPreferences.putString("cedUsuario",""+response.getString("cedUsuario"));

                                Intent intent=new Intent(Login.this,Principal.class);
                                startActivity(intent);
                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }
                                finish();

                            }
                            else
                            {
                                if(progressDialog.isShowing())
                                {
                                    progressDialog.dismiss();
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Encarga")
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                            }
                                        }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }
                        catch (JSONException e)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setTitle("Encarga")
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
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
                        if(progressDialog.isShowing())
                        {
                            progressDialog.dismiss();
                        }                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle("Encarga")
                                .setMessage(error.toString())
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                    }
                                }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                setTextColor(getResources().getColor(R.color.colorPrimary));

                        if (error instanceof TimeoutError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NoConnectionError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof NetworkError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                    setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else
                        if (error instanceof ParseError)
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                            builder
                                    .setTitle("Encarga")
                                    .setMessage(error.toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).setCancelable(true).show().getButton(DialogInterface.BUTTON_POSITIVE).
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
                headers.put("email", emailUsuario);
                headers.put("clave", claveUsuario);
                headers.put("tokenFCM", tokenFCM);
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "login");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /*private void WebServiceCerrarSesion(final String emailUsuario)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cerrando sesiónes, espera un momento ...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        String _urlWebService = vars.ipServer.concat("/ws/CerrarSesion");

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, _urlWebService, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            if(response.getBoolean("status"))
                            {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Las sesiones han sido cerradas con éxito. Ingresa de nuevo.", Snackbar.LENGTH_LONG).show();
                            }

                            else
                            {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Erros cerrando sesiones, contactanos por la opción Soporte.", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage(e.getMessage().toString())
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if (error instanceof TimeoutError)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conexión, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NoConnectionError)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Por favor, conectese a la red.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de autentificación en la red, favor contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ServerError)
                        {
                            progressDialog.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error server, sin respuesta del servidor.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof NetworkError)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de red, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
                        }

                        else

                        if (error instanceof ParseError)
                        {
                            progressDialog.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Login.this,R.style.AlertDialogTheme));
                            builder
                                    .setMessage("Error de conversión Parser, contacte a su proveedor de servicios.")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id)
                                        {
                                        }
                                    }).show();
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
                headers.put("emailUsuario", emailUsuario);
                //headers.put("MyToken", sharedPreferences.getString("MyToken"));
                return headers;
            }
        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }*/
}
