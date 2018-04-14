package com.ingeniapps.encarga.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

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
import com.ingeniapps.encarga.R;
import com.ingeniapps.encarga.vars.vars;
import com.ingeniapps.encarga.volley.ControllerSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CambiarClave extends AppCompatActivity
{
    EditText edit_text_cambiar_clave;
    EditText edit_text_validar_cambiar_clave;
    Button btn_cambiar_clave;
    private String clave;
    private String confirmarClave;
    private ProgressDialog progressDialog;
    vars vars;
    private String emailUsuario;
    private String codigoRecuperacion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_clave);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras == null)
            {
                emailUsuario = null;
                codigoRecuperacion = null;
            }
            else
            {
                emailUsuario = extras.getString("emailUsuario");
                codigoRecuperacion = extras.getString("codigoRecuperacion");
            }
        }
        else
        {
            emailUsuario = (String) savedInstanceState.getSerializable("emailUsuario");
        }

        vars=new vars();

        edit_text_cambiar_clave=(EditText)findViewById(R.id.edit_text_cambiar_clave);
        edit_text_validar_cambiar_clave=(EditText)findViewById(R.id.edit_text_validar_cambiar_clave);
        btn_cambiar_clave=(Button) findViewById(R.id.btn_cambiar_clave);
        btn_cambiar_clave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                clave=edit_text_cambiar_clave.getText().toString();
                confirmarClave=edit_text_validar_cambiar_clave.getText().toString();

                if (TextUtils.isEmpty(clave))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite la clave.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                if (clave.trim().length()<5)
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "La clave debe contener 5 caracteres.", Snackbar.LENGTH_LONG).show();
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

                WebServiceCambiarClave();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void requestFocus(View view)
    {
        if (view.requestFocus())
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void WebServiceCambiarClave()
    {
        String _urlWebService=vars.ipServer.concat("/ws/ModificarClaveUsuario");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(CambiarClave.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Gestionando cambio...");
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
                            boolean status=response.getBoolean("status");
                            String message=response.getString("message");

                            if(status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambiarClave.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Encarga")
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                finish();
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                            else
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambiarClave.this,R.style.AlertDialogTheme));
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
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambiarClave.this,R.style.AlertDialogTheme));
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
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CambiarClave.this,R.style.AlertDialogTheme));
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
                            progressDialog.dismiss();
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
                        if (error instanceof NoConnectionError)
                        {
                            progressDialog.dismiss();
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
                                    setTextColor(getResources().getColor(R.color.colorPrimary));                        }

                        else

                        if (error instanceof AuthFailureError)
                        {
                            progressDialog.dismiss();
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
                            progressDialog.dismiss();
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
                            progressDialog.dismiss();
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
                            progressDialog.dismiss();
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
                headers.put("clvUsuario", clave);
                headers.put("emaUsuario", emailUsuario);
                headers.put("codigoRecuperacion", codigoRecuperacion);
                return headers;
            }

        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "_REGISTRO");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("_REGISTRO");
    }
}