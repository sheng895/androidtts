#include "type_conv.h"
#include <sys/stat.h>

namespace speechnn {
// wstring to string
std::string wstring2utf8string(const std::wstring& wstr)
{
    static std::wstring_convert<std::codecvt_utf8<wchar_t> > strCnv;
    return strCnv.to_bytes(wstr);
//    std::string curLocale = setlocale(LC_ALL, NULL);        // curLocale = "C";
//    setlocale(LC_ALL, "chs");
//    const wchar_t* _Source = wstr.c_str();
//    size_t _Dsize = 2 * wstr.size() + 1;
//    char* _Dest = new char[_Dsize];
//    memset(_Dest, 0, _Dsize);
//    wcstombs(_Dest, _Source, _Dsize);
//    std::string result = _Dest;
//    delete[]_Dest;
//    setlocale(LC_ALL, curLocale.c_str());
//    return result;

}
 
// string to wstring 
std::wstring utf8string2wstring(const std::string& str)
{
    static std::wstring_convert< std::codecvt_utf8<wchar_t> > strCnv;
    return strCnv.from_bytes(str);
    //static std::wstring_convert< std::codecvt_utf8<wchar_t> > strCnv;
    //return strCnv.from_bytes(str);
 /*   std::string strLocale = setlocale(LC_ALL, "");
    size_t nDestSize = mbstowcs(NULL, str.c_str(), 0) + 1;
    wchar_t* wchDest = new wchar_t[nDestSize];
    wmemset(wchDest, 0, nDestSize);
    mbstowcs(wchDest, str.c_str(), nDestSize);
    std::wstring wstrResult = wchDest;
    delete[]wchDest;
    setlocale(LC_ALL, strLocale.c_str());
    return wstrResult;*/
//    setlocale(LC_ALL, "chs");
//    const char* _Source = str.c_str();
//    size_t _Dsize = str.size() + 1;
//    wchar_t* _Dest = new wchar_t[_Dsize];
//    wmemset(_Dest, 0, _Dsize);
//    mbstowcs(_Dest, _Source, _Dsize);
//    std::wstring result = _Dest;
//    delete[]_Dest;
//    setlocale(LC_ALL, "C");
//    return result;
}


bool isFileExist(const std::string name)
{
    struct stat buffer;
    return (stat(name.c_str(), &buffer) == 0);
}



}
