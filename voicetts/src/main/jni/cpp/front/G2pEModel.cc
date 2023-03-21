#include "G2pEModel.h"
#define MNN_OPEN_TIME_TRACE
#include "MNN/AutoTime.hpp"
#include "MNN/expr/ExprCreator.hpp"
#include "MNN/expr/Executor.hpp"
#include "base/type_conv.h"
#include <iostream>

#define DEBUG_INFO 0

const char* G2pEModel::G2P_FLAG_PAD = "<pad>";
const char* G2pEModel::G2P_FLAG_UNK = "<unk>";
const char* G2pEModel::G2P_FLAG_START = "<s>";
const char* G2pEModel::G2P_FLAG_END = "</s>";

const int G2pEModel::G2P_FLAG_PAD_ID = 0;
const int G2pEModel::G2P_FLAG_UNK_ID = 1;
const int G2pEModel::G2P_FLAG_START_ID = 2;
const int G2pEModel::G2P_FLAG_END_ID = 3;

G2pEModel::G2pEModel(std::string &modelFilename)
{
    model_filename = modelFilename;
    interpreterBuild(modelFilename);
    std::vector<char> graphemes({' ', ' ', ' ','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'});
    std::vector<const char*> phonemes({G2P_FLAG_PAD, G2P_FLAG_UNK, G2P_FLAG_START, G2P_FLAG_END,"AA0", "AA1", "AA2", "AE0", "AE1", "AE2", "AH0", "AH1", "AH2", "AO0", "AO1", "AO2", "AW0", "AW1", "AW2", "AY0", "AY1", "AY2", "B", "CH", "D", "DH","EH0", "EH1", "EH2", "ER0", "ER1", "ER2", "EY0", "EY1", "EY2", "F", "G", "HH","IH0", "IH1", "IH2", "IY0", "IY1", "IY2", "JH", "K", "L", "M", "N", "NG", "OW0", "OW1","OW2", "OY0", "OY1", "OY2", "P", "R", "S", "SH", "T", "TH", "UH0", "UH1", "UH2", "UW","UW0", "UW1", "UW2", "V", "W", "Y", "Z", "ZH"});
    for (int i = 0; i < graphemes.size(); i++) g2ids[graphemes[i]] = i;
    for (int i = 0; i < phonemes.size(); i++) ids2p[i] = phonemes[i];
};


G2pEModel::~G2pEModel()
{
    interpreter_encoder->releaseSession(session_encoder);
    interpreter_decoder->releaseSession(session_decoder);
    MNN::Interpreter::destroy(interpreter_encoder);
    MNN::Interpreter::destroy(interpreter_decoder);
};

void G2pEModel::interpreterBuild(std::string &modelFilename)
{
    MNN::ScheduleConfig config;
    //config.numberThread = 1;
    auto runtimeInfo = MNN::Interpreter::createRuntime({config});
    std::string  encoderpath=modelFilename+"/g2p/g2pE_mobile_encoder.mnn";
    if (!speechnn::isFileExist(encoderpath)) {
        std::cout << "configuration file " << encoderpath << " does not exists. " << std::endl;
    }
    /*创建encoder模型*/
    interpreter_encoder = MNN::Interpreter::createFromFile(encoderpath.c_str());
    session_encoder = interpreter_encoder->createSession(config, runtimeInfo);
    std::string  decoderpath=modelFilename+"/g2p/g2pE_mobile_decoder.mnn";
    /*创建decoder模型*/
    interpreter_decoder = MNN::Interpreter::createFromFile(decoderpath.c_str());
    session_decoder = interpreter_decoder->createSession(config, runtimeInfo);
};

MNN::Tensor* G2pEModel::encode(std::vector<int> &inputTokens)
{
    // inputTensors : [ input_ids, ]
    // outputTensors: [ output_h0, ]
    int input_ids_len = (int)inputTokens.size();
    auto inputTensor = interpreter_encoder->getSessionInput(session_encoder, NULL);
    interpreter_encoder->resizeTensor(inputTensor, {1, input_ids_len});
    interpreter_encoder->resizeSession(session_encoder);
    
    for (int i = 0; i < input_ids_len; i++)
    {
        inputTensor->host<int>()[i] = inputTokens[i];
    }
    
    //inputTensor->copyFromHostTensor(nchwTensor);
    interpreter_encoder->runSession(session_encoder);
    auto encoder_output_tensor = interpreter_encoder->getSessionOutput(session_encoder, NULL);

#if DEBUG_INFO    
    for (auto dim : encoder_output_tensor->shape())
    {
        std::cout << dim << std::endl;
    }
    
    for (int i = 0; i < encoder_output_tensor->shape()[1]; i++)
    {
        std::cout << encoder_output_tensor->host<float>()[i] << " ";
    }
    std::cout << std::endl;
#endif
    
    // delete nchwTensor;
    return encoder_output_tensor;//encoder_output_tensor;
}

std::vector<int> G2pEModel::decode(MNN::Tensor* encoderOutput)
{
    // inputTensors : [ input_id, input_h0, ]
    // outputTensors: [ output_h0, output_logit, output_y, ]
    //interpreter_decoder->runSession(session_decoder);
    int output_max_len = 40;
    std::vector<int> outputTokens{};
    auto inputIDTensor = interpreter_decoder->getSessionInput(session_decoder, "input_id");
    auto inputH0Tensor = interpreter_decoder->getSessionInput(session_decoder, "input_h0");
    
#if DEBUG_INFO  
    std::cout << "encoder hidden dims:" << encoderOutput->shape()[1] << std::endl;
#endif

    interpreter_decoder->resizeTensor(inputIDTensor, {1, 1});
    interpreter_decoder->resizeSession(session_decoder);
    interpreter_decoder->resizeTensor(inputH0Tensor, {1, 1, encoderOutput->shape()[1]});
    interpreter_decoder->resizeSession(session_decoder);
    
#if DEBUG_INFO    
    for (auto dim : inputIDTensor->shape())
    {
        std::cout << "inputIDTensor shape " << dim << std::endl;
    }
#endif
    
    inputIDTensor->host<int>()[0] = G2P_FLAG_START_ID;
    
    for (int i = 0; i < encoderOutput->shape()[1]; i++)
    {
        inputH0Tensor->host<float>()[i] = encoderOutput->host<float>()[i];
    }
    
    while(--output_max_len > 0)
    {
        interpreter_decoder->runSession(session_decoder);
        
        // 获取输入
        auto outputIDTensor = interpreter_decoder->getSessionOutput(session_decoder, "output_y");
        auto outputH0Tensor = interpreter_decoder->getSessionOutput(session_decoder, "output_h0");
        
#if DEBUG_INFO    
        std::cout << outputIDTensor->host<int>()[0] << " ";
#endif

        if (outputIDTensor->host<int>()[0] == G2P_FLAG_END_ID)
        {
            break;
        }
        
        outputTokens.push_back(outputIDTensor->host<int>()[0]);
        
        // 下一轮
        inputIDTensor->host<int>()[0] = outputIDTensor->host<int>()[0];
        
        for (int i = 0; i < encoderOutput->shape()[1]; i++)
        {
            inputH0Tensor->host<float>()[i] = outputH0Tensor->host<float>()[i];
        }
    }
    
    return outputTokens;
}

std::vector<const char*> G2pEModel::g2p(std::string &inputText)
{
    std::vector<int> inputTokens;
    for (int i = 0; i < inputText.size(); i++)
    {
        int id = g2ids[(char)inputText[i]];
        inputTokens.push_back(id);
        //std::cout << id << " ";
    }
    
    inputTokens.push_back(G2P_FLAG_START_ID);
    
    //std::cout << std::endl;
    MNN::Tensor* encoder_output_tensor = encode(inputTokens);
    std::vector<int> outputTokens = decode(encoder_output_tensor); 
    
    std::vector<const char*> outputPhoneme; 
    for (auto token : outputTokens)
    {
        outputPhoneme.push_back(ids2p[token]);
    }
    
    return outputPhoneme;
}

std::vector<int> G2pEModel::test()
{
    std::vector<int> inputTokens{5,3,20,6,7,20,2};
    MNN::Tensor* encoder_output_tensor = encode(inputTokens);
    std::vector<int> outputTokens = decode(encoder_output_tensor);    
    // output should be "K AA1 R D ER0"
    return outputTokens;
}
