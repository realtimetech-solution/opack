package com.realtimetech.opack;

public class Interlanguage {
    public enum IlType {
        CREATE_OPACK_OBJECT,
        CREATE_OPACK_LIST,

        CREATE_OPACK_NONE,
        CREATE_OPACK_BOOL,
        CREATE_OPACK_NUMBER,
        CREATE_OPACK_STRING,

        MODIFY_OPACK_OBJECT,
        MODIFY_OPACK_ARRAY,

        GET,
        CALL
    }

    public abstract class IlCode {
        public final IlType type;

        public IlCode(IlType type) {
            this.type = type;
        }
    }

    public class IlCodeCreateOpackObject extends IlCode {
        public IlCodeCreateOpackObject() {
            super(IlType.CREATE_OPACK_OBJECT);
        }
    }

    public class IlCodeCreateOpackList extends IlCode {
        public IlCodeCreateOpackList() {
            super(IlType.CREATE_OPACK_LIST);
        }
    }


    public class IlCodeCreateOpackNone extends IlCode {
        public IlCodeCreateOpackNone() {
            super(IlType.CREATE_OPACK_NONE);
        }
    }

    public class IlCodeCreateOpackBool extends IlCode {
        public IlCodeCreateOpackBool() {
            super(IlType.CREATE_OPACK_BOOL);
        }
    }

    public class IlCodeCreateOpackNumber extends IlCode {
        public IlCodeCreateOpackNumber() {
            super(IlType.CREATE_OPACK_NUMBER);
        }
    }

    public class IlCodeCreateOpackString extends IlCode {
        public IlCodeCreateOpackString() {
            super(IlType.CREATE_OPACK_STRING);
        }
    }

    public class IlCodeModifyOpackObject extends IlCode {
        public IlCodeModifyOpackObject() {
            super(IlType.MODIFY_OPACK_OBJECT);
        }
    }

    public class IlCodeModifyOpackArray extends IlCode {
        public IlCodeModifyOpackArray() {
            super(IlType.MODIFY_OPACK_ARRAY);
        }
    }

    public class IlCodeGet extends IlCode {
        public IlCodeGet() {
            super(IlType.GET);
        }
    }

    public class IlCodeCall extends IlCode {
        public IlCodeCall() {
            super(IlType.CALL);
        }
    }

    public class IlCodes {
        final Class<?> targetClass;
        final IlCode[] codes;

        public IlCodes(Class<?> targetClass, IlCode[] codes) {
            this.targetClass = targetClass;
            this.codes = codes;
        }
    }

    public class Context {
        final IlCodes codes;
        int index;

        public Context(IlCodes codes) {
            this.codes = codes;
        }
    }

}
