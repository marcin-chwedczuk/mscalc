package mscalc;

public interface WinErrorCrossPlatform {
    int E_ACCESSDENIED = 0x80070005;
    int E_FAIL = 0x80004005;
    int E_INVALIDARG = 0x80070057;
    int E_OUTOFMEMORY = 0x8007000E;
    int E_POINTER = 0x80004003;
    int E_UNEXPECTED = 0x8000FFFF;
    int E_BOUNDS = 0x8000000B;
    int S_OK = 0x0;
    int S_FALSE = 0x1;

    static boolean SUCCEEDED(int hr) { return (((int)(hr)) >= 0); }
    static boolean FAILED(int hr)  { return (((int)(hr)) < 0); }

    static int SCODE_CODE(int sc) { return ((sc)&0xFFFF); }
}
