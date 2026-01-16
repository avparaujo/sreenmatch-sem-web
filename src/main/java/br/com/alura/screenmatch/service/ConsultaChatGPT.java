package br.com.alura.screenmatch.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

public class ConsultaChatGPT {
    public static String obterTraducao(String texto) {
        OpenAiService service = new OpenAiService("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");

        CompletionRequest requisicao = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt("traduza para o português o texto: " + texto)
                .maxTokens(1000)
                .temperature(0.7)
                .build();

        var resposta = service.createCompletion(requisicao);
        return resposta.getChoices().get(0).getText();
    }
}
