package lt.gama.auth.impl;

import lt.gama.AppProp;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.service.ex.GamaServerErrorException;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class AESCipherService {

	@Autowired
	private Environment env;

	private Key _key;

	private Key key() {
		Key localRef = _key;
		if (localRef == null) {
			synchronized (AESCipherService.class) {
				localRef = _key;
				if (localRef == null) {
					try {
						String encodedKey = Validators.checkNotNull(env.getProperty(AppProp.GAMA_TOKEN_SECRET_KEY.toString()));
						byte[] encodedKeyBytes = java.util.Base64.getDecoder().decode(encodedKey);
						_key = localRef = new SecretKeySpec(encodedKeyBytes, "AES");
					} catch (Exception e) {
						throw new GamaException(e.getMessage(), e);
					}
				}
			}
		}
		return localRef;
	}

	private Cipher cipher() throws GeneralSecurityException {
		return Cipher.getInstance("AES/ECB/PKCS5Padding");
	}

	public String encrypt(String raw) throws GeneralSecurityException  {
		Key key = key();
	    Cipher cipher = cipher();
		byte[] input = raw.getBytes(StandardCharsets.UTF_8);

		// compress
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
			gzip.write(input);
		} catch (IOException e) {
			throw new GeneralSecurityException(e);
		}
		byte[] compressed = out.toByteArray();

		// encode
	    cipher.init(Cipher.ENCRYPT_MODE, key);
	    byte[] cipherText = cipher.doFinal(compressed);

		return Base64.encodeBase64String(cipherText);
	}

	public String decrypt(String enc) throws GeneralSecurityException {
		Key key = key();
	    Cipher cipher = cipher();
		byte[] input = Base64.decodeBase64(enc);

		// decode
		cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] plainText = cipher.doFinal(input);

		// decompress
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(plainText))) {
			int res = 0;
			byte[] buf = new byte[1024];
			while (res >= 0) {
				res = gzip.read(buf, 0, buf.length);
				if (res > 0) {
					out.write(buf, 0, res);
				}
			}
		} catch (IOException e) {
			throw new GeneralSecurityException(e);
		}

		return out.toString();
	}


    /**
     * Creates an SHA-1 hash based on plain password and salt
     *
     * @param salt
     *            - byte array
     * @param password
     *            - plain text password
     * @return SHA-1 hash based on plain password and salt
     * @throws GamaServerErrorException on error
     */
    public String generateHash(byte[] salt, String password) throws GamaServerErrorException {
        if (salt == null || StringHelper.isEmpty(password)) {
            throw new GamaServerErrorException("Wrong Credentials");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt);
            byte[] input = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            digest.reset();
            input = digest.digest(input);
            return Base64.encodeBase64String(input);

        } catch (NoSuchAlgorithmException e) {
            throw new GamaServerErrorException("System error", e);
        }
    }

    private byte[] hashKey() {
        String HASH_KEY = "13659d7ae71fc604b20bb4776c31bd899d8efce7f3d58b9307cb485e50a4c1e0";
        return Base64.decodeBase64(HASH_KEY);
    }

    public String sign(String params) throws GamaServerErrorException {
        try {
            return generateHash(hashKey(), params);
        } catch (GamaServerErrorException e) {
            throw new GamaServerErrorException("Wrong Credentials", e);
        }
    }
}
