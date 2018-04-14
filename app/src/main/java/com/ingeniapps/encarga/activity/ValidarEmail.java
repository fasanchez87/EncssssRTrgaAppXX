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

public class ValidarEmail extends AppCompatActivity
{
    EditText edit_text_validar_email;
    Button btn_siguiente_validar_email;
    private String emailUsuario;
    public vars vars;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validar_email);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        vars=new vars();

        edit_text_validar_email=(EditText)findViewById(R.id.edit_text_validar_email);
        btn_siguiente_validar_email=(Button)findViewById(R.id.btn_siguiente_validar_email);
        btn_siguiente_validar_email.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                emailUsuario=edit_text_validar_email.getText().toString();

                if (TextUtils.isEmpty(emailUsuario)||!(isValidEmail(emailUsuario)))
                {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Digite un email valido.", Snackbar.LENGTH_LONG).show();
                    view.requestFocus();
                    return;
                }

                WebServiceValidarEmail();
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
    @Override
    public void onBackPressed()
    {
        finish();
    }

    private void WebServiceValidarEmail()
    {
        String _urlWebService=vars.ipServer.concat("/ws/ValidarCorreo");

        progressDialog = new ProgressDialog(new ContextThemeWrapper(ValidarEmail.this,R.style.AppCompatAlertDialogStyle));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Validando...");
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ValidarEmail.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Estado Verificación")
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                Intent intent=new Intent(ValidarEmail.this,ValidarCodigoSeguridad.class);
                                                intent.putExtra("emailUsuario",emailUsuario);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).setCancelable(false).show().getButton(DialogInterface.BUTTON_POSITIVE).
                                        setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                            else
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ValidarEmail.this,R.style.AlertDialogTheme));
                                builder
                                        .setTitle("Estado Verificación")
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ValidarEmail.this,R.style.AlertDialogTheme));
                            builder
                                    .setTitle("Estado Verificación")
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ValidarEmail.this,R.style.AlertDialogTheme));
                        builder
                                .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                                    .setTitle("Estado Verificación")
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
                headers.put("emaUsuario", emailUsuario);
                return headers;
            }

        };

        ControllerSingleton.getInstance().addToReqQueue(jsonObjReq, "VALIDATE_EMAIL");
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ControllerSingleton.getInstance().cancelPendingReq("VALIDATE_EMAIL");
    }
}
