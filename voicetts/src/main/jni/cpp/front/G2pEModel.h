#ifndef __G2PE_OOV_MNN_H__
#define __G2PE_OOV_MNN_H__

#include "MNN/expr/Module.hpp"
#include <vector>
#include <string>
#include <map>


class G2pEModel
{
public:
    G2pEModel(std::string &modelFilename);
    ~G2pEModel();
    
    std::vector<int> test();
    std::vector<const char*> g2p(std::string &inputText);
    
    MNN::Tensor* encode(std::vector<int> &inputTokens);
    std::vector<int> decode(MNN::Tensor* encoderOutput);

private:
    void interpreterBuild(std::string &modelFilename);

    std::string  model_filename;
    
    MNN::Interpreter* interpreter_encoder;
    MNN::Interpreter* interpreter_decoder;
    MNN::Session* session_encoder;
    MNN::Session* session_decoder;
    
    static const char* G2P_FLAG_PAD;
    static const char* G2P_FLAG_UNK;
    static const char* G2P_FLAG_START;
    static const char* G2P_FLAG_END;
    static const int G2P_FLAG_PAD_ID;
    static const int G2P_FLAG_UNK_ID;
    static const int G2P_FLAG_START_ID;
    static const int G2P_FLAG_END_ID;
    
    std::map<char, int> g2ids;
    std::map<int, const char*> ids2p;
};

#endif //__G2PE_OOV_MNN_H__
