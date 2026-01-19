package com.aimix_aimixapi;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
class AiMixApiApplicationTests {

	@Test
	void testChatGpt() {
		// OpenAI 클라이언트 생성 (환경변수 OPENAI_API_KEY 사용)
		OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("안녕! 너는 페이커를 알아?")
                .model(ChatModel.GPT_4O_MINI)
                .build();
        ChatCompletion chatCompletion = client.chat().completions().create(params);
        log.info("Chat Completion: {}", chatCompletion);
	}

//	@Test
//	void testAdmin() {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		String hash = encoder.encode("qwer3033!@");
//		System.out.println("BCrypt Hash: " + hash);
//	}
}
