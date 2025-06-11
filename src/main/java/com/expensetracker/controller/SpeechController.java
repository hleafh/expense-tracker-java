package com.expensetracker.controller;

import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/speech") // 設定這個 Controller 的基礎路徑
public class SpeechController {

    @PostMapping("/transcribe") // 設定語音轉文字的端點路徑
    public ResponseEntity<?> transcribeAudio(@RequestParam("audioFile") MultipartFile audioFile) {
        // 檢查是否有上傳音訊檔案
        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "請上傳音檔。"));
        }

        try (SpeechClient speechClient = SpeechClient.create()) { // 建立 SpeechClient，並確保資源會自動關閉
            // 將前端傳來的 MultipartFile 轉換為 Google Cloud API 需要的 ByteString
            ByteString audioBytes = ByteString.copyFrom(audioFile.getBytes());

            // 設定語音辨識的相關參數
            // ***非常重要：這裡的 Encoding 和 SampleRateHertz 必須與前端錄製的音訊格式和採樣率完全匹配！***
            // 如果前端使用 'audio/webm; codecs=opus'，請設定為 WEBM_OPUS 和 48000 Hz。
            // 如果前端使用 'audio/wav' (PCM 編碼)，請設定為 LINEAR16 和 16000 Hz 或 44100 Hz。
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS) // 假設前端錄製為 WebM Opus
                    .setSampleRateHertz(48000) // 假設採樣率為 48kHz
                    .setLanguageCode("zh-TW") // 設定為繁體中文，這會大大提升中文辨識精準度
                    .setEnableAutomaticPunctuation(true) // 可選：啟用自動添加標點符號，這在辨識長句時很有用
                    // .setModel("default") // 可選：您可以指定模型，例如 "default"、"command_and_search" 等
                    .build();

            // 建立 RecognitionAudio 物件，包含要辨識的音訊內容
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // 執行語音辨識
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            // 檢查是否有辨識結果
            if (results.isEmpty()) {
                return ResponseEntity.ok().body(Map.of("transcription", "", "message", "未識別到任何語音，請說清楚一點。"));
            }

            // 取得第一個（最有可能的）轉譯結果，通常它會包含最高的信心分數
            SpeechRecognitionAlternative alternative = results.get(0).getAlternatives(0);
            String transcribedText = alternative.getTranscript();

            // 回傳成功的響應，包含轉譯的文字和訊息
            return ResponseEntity.ok().body(Map.of("transcription", transcribedText, "message", "語音識別成功。"));

        } catch (IOException e) {
            // 處理音訊檔案讀取或轉換失敗的情況
            System.err.println("音檔讀取失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "音檔處理失敗，請檢查檔案格式或大小。"));
        } catch (Exception e) {
            // 處理 Google Cloud API 呼叫失敗或憑證問題
            System.err.println("Google Cloud Speech-to-Text 服務錯誤: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "語音識別服務錯誤。請確認您的 Google Cloud 憑證和 API 已正確設定。詳細錯誤：" + e.getMessage()));
        }
    }
}