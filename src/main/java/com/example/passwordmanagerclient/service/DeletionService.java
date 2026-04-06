package com.example.passwordmanagerclient.service;

import com.example.passwordmanagerclient.api.deletion.AuthorizedDeletionCompleteReq;
import com.example.passwordmanagerclient.api.deletion.AuthorizedDeletionStartReq;
import com.example.passwordmanagerclient.api.deletion.AuthorizedDeletionStartResp;
import com.example.passwordmanagerclient.api.deletion.EmailDeletionStartReq;
import com.example.passwordmanagerclient.api.error.ApiError;
import com.example.passwordmanagerclient.util.*;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.agreement.srp.SRP6Util;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class DeletionService {

    public static void deleteEmail(String email) {
        try {
            EmailDeletionStartReq emailDeletionStartReq = new EmailDeletionStartReq();
            emailDeletionStartReq.setEmail(email);
            HttpComms.sendPostRequest(emailDeletionStartReq, "/api/deletion/email/start");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OperationResult deleteAuthorized(String masterPassword) {
        try {
            SRP6GroupParameters group = SRP6StandardGroups.rfc5054_3072;
            Digest digest = new SHA256Digest();
            SecureRandom random = new SecureRandom();

            BigInteger a = new BigInteger(256, random);
            BigInteger A = group.getG().modPow(a, group.getN());

            AuthorizedDeletionStartReq startReq = new AuthorizedDeletionStartReq();
            startReq.setDeviceId(AppContext.getDeviceId());
            startReq.setA(Base64.getEncoder().encodeToString(A.toByteArray()));

            HttpResponse<String> responseStart = HttpComms.sendPostRequestWithAuth(startReq, "/api/deletion/authorized/start", AppContext.getJwtToken());

            if (DtoHandler.getStatusCode(responseStart) != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseStart, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            AuthorizedDeletionStartResp startResp = DtoHandler.parseToDto(responseStart, AuthorizedDeletionStartResp.class);

            byte[] saltAuth = Base64.getDecoder().decode(startResp.getSaltAuth());
            BigInteger B = new BigInteger(1, Base64.getDecoder().decode(startResp.getB()));

            BigInteger x = SRP6Util.calculateX(digest, group.getN(), saltAuth,
                    AppContext.getEmail().getBytes(StandardCharsets.UTF_8),
                    masterPassword.getBytes(StandardCharsets.UTF_8));

            BigInteger u = SRP6Util.calculateU(digest, group.getN(), A, B);

            BigInteger k = SRP6Util.calculateK(digest, group.getN(), group.getG());
            BigInteger S = B.subtract(k.multiply(group.getG().modPow(x, group.getN())))
                    .modPow(a.add(u.multiply(x)), group.getN());
            byte[] K = new byte[digest.getDigestSize()];
            digest.update(S.toByteArray(), 0, S.toByteArray().length);
            digest.doFinal(K, 0);

            BigInteger M1 = SRP6Util.calculateM1(digest, group.getN(), A, B, S);

            AuthorizedDeletionCompleteReq completeReq = new AuthorizedDeletionCompleteReq();
            completeReq.setDeviceId(AppContext.getDeviceId());
            completeReq.setM1(Base64.getEncoder().encodeToString(M1.toByteArray()));

            HttpResponse<String> responseComplete = HttpComms.sendPostRequestWithAuth(completeReq, "/api/deletion/authorized/end", AppContext.getJwtToken());

            if (DtoHandler.getStatusCode(responseComplete) != 200) {
                ApiError apiError = DtoHandler.parseToDto(responseComplete, ApiError.class);
                return new OperationResult(apiError.getMessage(), false);
            }

            return new OperationResult("Account deletion successful", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new OperationResult("Account deletion failed", false);
        }
    }

}
