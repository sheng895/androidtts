//
// Created by ssq on 2/7/2023.
//
#include <string.h>
#include <assert.h>
#include "com_gykj_paddle_lite_demo_tts_CalcMac.h"

//#include "utils/dir_utils.h"
#include "cpp/front/front_interface.h"
//#include <glog/logging.h>
//#include <gflags/gflags.h>
#include <map>


speechnn::FrontEngineInterface *front_inst = nullptr;



JNIEXPORT void JNICALL Java_com_gykj_paddle_lite_demo_tts_CalcMac_Native_1Jni(JNIEnv *env, jclass clazz,jstring text){

//    char *rtn = NULL;
//    jclass clsstring = env->FindClass("java/lang/String");
//    jstring strencode = env->NewStringUTF("GB2312");
//    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
//    jbyteArray barr = (jbyteArray) env->CallObjectMethod(env->NewStringUTF("/src/main/assets/front.conf"), mid, strencode);
//    jsize alen = env->GetArrayLength(barr);
//    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
//    if (alen > 0) {
//        rtn = (char *) malloc(alen + 1);
//        memcpy(rtn, ba, alen);
//        rtn[alen] = 0;
//    }
//    env->ReleaseByteArrayElements(barr, ba, 0);
//    std::string stemp(rtn);
//    free(rtn);
//
//    std::string file_path = stemp;
    const char *str_txt= env->GetStringUTFChars(text, NULL);

     front_inst = new speechnn::FrontEngineInterface(str_txt);
      if ((!front_inst) || (front_inst->init())) {
//          LOG(ERROR) << "Creater tts engine failed!";
          if (front_inst != nullptr) {
              delete front_inst;
          }
          front_inst = nullptr;
      }
  }



JNIEXPORT jstring JNICALL Java_com_gykj_paddle_lite_demo_tts_CalcMac_Native_1JniCalcText(JNIEnv *env, jclass clazz,
                                                                                         jstring text) {
        const char *str_txt= env->GetStringUTFChars(text, NULL);
        std::wstring ws_sentence = speechnn::utf8string2wstring(str_txt);
        // 繁体转简体
        std::wstring sentence_simp;
        front_inst->Trand2Simp(ws_sentence, sentence_simp);
        ws_sentence = sentence_simp;

        std::string s_sentence;
        std::vector<std::wstring> sentence_part;
        std::vector<int> phoneids = {};
        std::vector<int> toneids = {};
        std::string resultphoneids="";

        // 根据标点进行分句
//        LOG(INFO) << "Start to segment sentences by punctuation";
        front_inst->SplitByPunc(ws_sentence, sentence_part);
//        LOG(INFO) << "Segment sentences through punctuation successfully";

        // 分句后获取音素id
//        LOG(INFO) << "Start to get the phoneme and tone id sequence of each sentence";
        for(int i = 0; i < sentence_part.size(); i++) {

//            LOG(INFO) << "Raw sentence is: " << speechnn::wstring2utf8string(sentence_part[i]);
            front_inst->SentenceNormalize(sentence_part[i]);
            s_sentence = speechnn::wstring2utf8string(sentence_part[i]);
//            LOG(INFO) << "After normalization sentence is: " << s_sentence;

            if (0 != front_inst->GetSentenceIds(s_sentence, phoneids, toneids)) {
//                LOG(ERROR) << "TTS inst get sentence phoneids and toneids failed";
                 resultphoneids= limonp::Join(phoneids.begin(), phoneids.end(), ",");
                 return env->NewStringUTF(resultphoneids.c_str());;
            }

        }
        resultphoneids= limonp::Join(phoneids.begin(), phoneids.end(), ",");
        return env->NewStringUTF(resultphoneids.c_str());
}