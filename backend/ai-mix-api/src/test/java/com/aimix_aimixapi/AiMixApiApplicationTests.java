package com.aimix_aimixapi;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiMixApiApplicationTests {

	@Test
	void testChatGpt() {
		OpenAIClient client = OpenAIOkHttpClient.builder()
				.apiKey("test-api-key")
				.build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("안녕! 너는 페이커를 알아?")
                .model(ChatModel.GPT_4O_MINI)
                .build();

		assertNotNull(client);
		assertNotNull(params);
	}

//	@Test
//	void testAdmin() {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		String hash = encoder.encode("qwer3033!@");
//		System.out.println("BCrypt Hash: " + hash);
//	}
}
