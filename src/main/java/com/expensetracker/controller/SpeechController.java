package com.expensetracker.controller;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
// 精確地修正這一行，移除多餘的 .v1
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/speech")
public class SpeechController {

    private static final String GOOGLE_CREDENTIALS_JSON_ENV = "GOOGLE_APPLICATION_CREDENTIALS_JSON";

    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribeAudio(@RequestParam("audioFile") MultipartFile audioFile) {
        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "請上傳音檔。"));
        }

        SpeechClient speechClient = null;
        try {
            String credentialsJson = System.getenv(GOOGLE_CREDENTIALS_JSON_ENV);

            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
                );
                SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
                speechClient = SpeechClient.create(speechSettings);
            } else {
                System.out.println("環境變數 " + GOOGLE_CREDENTIALS_JSON_ENV + " 未設置或為空，將嘗試使用預設憑證查找。");
                speechClient = SpeechClient.create();
            }

            ByteString audioBytes = ByteString.copyFrom(audioFile.getBytes());

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("zh-TW")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            if (results.isEmpty()) {
                return ResponseEntity.ok().body(Map.of("transcription", "", "message", "未識別到任何語音，請說清楚一點。"));
            }

            SpeechRecognitionAlternative alternative = results.get(0).getAlternatives(0);
            String transcribedText = alternative.getTranscript();

            return ResponseEntity.ok().body(Map.of("transcription", transcribedText, "message", "語音識別成功。"));

        } catch (IOException e) {
            System.err.println("音檔讀取或憑證處理失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "音檔處理或憑證加載失敗，請檢查檔案或環境設定。"));
        } catch (Exception e) {
            System.err.println("Google Cloud Speech-to-Text 服務錯誤: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "語音識別服務錯誤。請確認您的 Google Cloud 憑證和 API 已正確設定。詳細錯誤：" + e.getMessage()));
        } finally {
            if (speechClient != null) {
                speechClient.close();
            }
        }
    }
}