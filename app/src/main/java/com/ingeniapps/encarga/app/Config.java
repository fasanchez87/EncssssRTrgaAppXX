package com.ingeniapps.encarga.app;

/**
 * Created by FABiO on 07/10/2016.
 */

public class Config
{

    public static final String DEVELOPER_KEY = "AIzaSyDU_6S9mGWQxL0TtnwofusuXAuR8Wdxwg4";


    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_OFERTA = "pushOferta";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String PUSH_NOTIFICATION_DESPACHADO = "pushDespachado";
    public static final String PUSH_NOTIFICATION_CANCELADO = "pushCancelado";
    public static final String PUSH_NOTIFICATION_USO_TICKET = "pushNotificaClienteUso";
    public static final String PUSH_NOTIFICATION_CLIENTE_ANULAR = "pushNotificaClienteAnular";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
}
