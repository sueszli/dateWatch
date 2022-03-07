package at.ac.tuwien.sepm.groupphase.backend.domain.token.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


@Service
public class TokenService {

    private static final int SIMPLE_TOKEN_LENGTH = 7;

    private static final char[] SIMPLE_TOKEN_CHARS = new char[]{
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
        'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };


    public String generateUniqueSimpleToken(List<String> alreadyExistingTokens) {
        return generateUniqueSimpleToken(alreadyExistingTokens::contains);
    }

    public String generateUniqueSimpleToken(Function<String, Boolean> alreadyExistsCheck) {
        String token;
        do {
            token = generateSimpleToken();
        } while (alreadyExistsCheck.apply(token));

        return token;
    }

    public List<String> generateUniqueSimpleTokens(int numberOfTokens) {
        var tokens = new ArrayList<String>();
        while(tokens.size() < numberOfTokens) {
            var token = generateUniqueSimpleToken(tokens);
            tokens.add(token);
        }
        return tokens;
    }


    private String generateSimpleToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < SIMPLE_TOKEN_LENGTH; i++) {
            int randomValue = (int) (Math.random() * SIMPLE_TOKEN_CHARS.length);
            token.append(SIMPLE_TOKEN_CHARS[randomValue]);
        }
        return token.toString();
    }
}
