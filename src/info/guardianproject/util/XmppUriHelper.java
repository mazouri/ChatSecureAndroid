package info.guardianproject.util;

import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make it easy to build URIs that are compliant with RFC5122/XEP-0147
 *
 * @author hans
 *
 */
public class XmppUriHelper {
    private static final String TAG = "XmppUriHelper";

    public static final String SCHEME = "xmpp";

    // http://xmpp.org/registrar/querytypes.html
    public static final String ACTION_COMMAND = "command";
    public static final String ACTION_DISCO = "disco";
    public static final String ACTION_INVITE = "invite";
    public static final String ACTION_JOIN = "join";
    public static final String ACTION_MESSAGE = "message";
    public static final String ACTION_PUBSUB = "pubsub";
    public static final String ACTION_RECVFILE = "recvfile";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_ROSTER = "roster";
    public static final String ACTION_SENDFILE = "sendfile";
    public static final String ACTION_SUBSCRIBE = "subscribe";
    public static final String ACTION_UNREGISTER = "unregister";
    public static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    public static final String ACTION_VCARD = "vcard";
    //
    public static final String OTR_QUERY_PARAM = "otr-fingerprint";
    //
    public static final String KEY_ACTION = "action";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_RESOURCE = "resource";
    public static final String KEY_FRAGMENT = "fragment";
    public static final String KEY_OTR_FINGERPRINT = OTR_QUERY_PARAM;

    public static String getUri (String address, String otrFingerprint)
    {
        /**
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        builder.appendPath(address);

        if (otrFingerprint != null)
            builder.appendQueryParameter(OTR_QUERY_PARAM, otrFingerprint);


        return builder.toString();
        **/

        StringBuilder builder = new StringBuilder();

        builder.append(SCHEME);
        builder.append(SCHEME_OPERATOR);

        builder.append(address);

        builder.append('?').append(OTR_QUERY_PARAM).append('=');
        builder.append(otrFingerprint);

        return builder.toString();
    }

    public static final Map<String, String> parse(Uri uri) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            if (TextUtils.equals(uri.getScheme(), "xmpp")) {
                if (uri.isOpaque()) {
                    String opaquePart = uri.getSchemeSpecificPart();
                    if (!TextUtils.isEmpty(opaquePart)) {
                        String f[] = opaquePart.split("#");
                        if (f.length > 1)
                            map.put(KEY_FRAGMENT, f[1]);
                        String s[] = f[0].split("\\?");
                        String a[] = s[0].split("/"); // split off XMPP Resource
                        map.put(KEY_ADDRESS, a[0]);
                        if (a.length > 1)
                            map.put(KEY_RESOURCE, a[1]);
                        String[] parameters = null;
                        if (s.length > 1) {
                            parameters = s[1].split("[;&]");
                            for (String t : parameters) {
                                String[] parameter = t.split("=");
                                if (parameter.length > 1)
                                    map.put(parameter[0], parameter[1]);
                                else
                                    map.put(parameter[0], "");
                            }
                        }
                    }
                } else if (uri.isHierarchical()) {
                    String authority = uri.getAuthority();
                    List<String> path = uri.getPathSegments();
                    if (TextUtils.isEmpty(authority) || path.size() > 0) {
                        // totally ignore the "authority" part
                        map.put(KEY_ADDRESS, path.get(0));
                        if (path.size() > 1)
                            map.put(KEY_RESOURCE, path.get(1));
                    } else {
                        /* this supports the original ChatSecure xmpp://to@address.com URLs.
                         * Those URLs are technically incorrect according to RFC5122 since the
                         * "authority" part directly after the // is meant to be the local
                         * sending account, not the remote, receiving account. */
                        map.put(KEY_ADDRESS, authority);
                    }
                    map.put(KEY_OTR_FINGERPRINT,
                            uri.getQueryParameter(XmppUriHelper.OTR_QUERY_PARAM));
                } else {
                    throw new Exception("Uri is neither opaque nor hierarchical!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
