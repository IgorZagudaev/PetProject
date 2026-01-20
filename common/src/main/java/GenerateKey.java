import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class GenerateKey {

    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // или HS512
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("jwt.secret=" + base64Key);
    }

}
