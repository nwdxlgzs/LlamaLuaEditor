package com.LlamaLuaEditor.aluasyntax;

import static com.LlamaLuaEditor.aluasyntax.clib.*;
import static com.LlamaLuaEditor.aluasyntax.lobject.*;
import static com.LlamaLuaEditor.aluasyntax.luaconf.*;
import com.LlamaLuaEditor.aluasyntax.structs.*;
public class ldebug {
    protected ldebug() {
    }

    //    const char *luaG_addinfo (lua_State *L, const char *msg, TString *src,
//                              int line) {
//        char buff[LUA_IDSIZE];
//        if (src)
//            luaO_chunkid(buff, getstr(src), LUA_IDSIZE);
//        else {  /* no source available; use "?" instead */
//            buff[0] = '?'; buff[1] = '\0';
//        }
//        return luaO_pushfstring(L, "%s:%d: %s", buff, line, msg);
//    }
    protected static String luaG_addinfo(lua_State L, String msg, String src, int line) {
        byte[] buff = new byte[LUA_IDSIZE];
        if (src != null) {
            luaO_chunkid(buff, src, LUA_IDSIZE);
        } else {
            buff[0] = '?';
            buff[1] = '\0';
        }
        return String.format("%s:%d: %s", cstring(buff), line, msg);
    }
}
