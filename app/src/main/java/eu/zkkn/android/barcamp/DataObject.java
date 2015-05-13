package eu.zkkn.android.barcamp;

/**
 *
 */
public class DataObject<T> {

    private T data;
    private int errorCode = ErrorCode.NO_ERROR;

    public DataObject(T data) {
        this.data = data;
    }

    public boolean hasError() {
        return errorCode != ErrorCode.NO_ERROR;
    }

    public void setErrorCode(int code) {
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getAndResetErrorCode() {
        int oldErrorCode = getErrorCode();
        setErrorCode(ErrorCode.NO_ERROR);
        return oldErrorCode;
    }

    public T getData() {
        return data;
    }
}
