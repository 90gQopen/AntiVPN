package se.k55.antivpn;

import com.moandjiezana.toml.Toml;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LookupService {
    
    static {
        try {
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {

                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
    
    private final String url;
    private final String token;
    
    public LookupService(Toml config) {
        url = config.getString("cus.url");
        token = "12341234";
    }
    
    public CompletableFuture<Boolean> fetch(String ip, UUID uuid){
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL (null, this.url + "?ip=" + ip + "&uuid=" + uuid.toString());
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(300);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty("Authorization", "Bearer " + token);
                System.setProperty("http.keepAlive", "false");

                InputStream content = connection.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(content));
                
                return Boolean.parseBoolean(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
