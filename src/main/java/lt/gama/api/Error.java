package lt.gama.api;

import lt.gama.helpers.StringHelper;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-05-01.
 */
public class Error {

    private Object message;

    private int code;

    private Detail detail;


    @SuppressWarnings("unused")
    protected Error() {}

    public Error(String message, int code) {
        this.message = StringHelper.hasValue(message) ? message : "ERROR";
        this.code = code;
    }

    public Error(String message, int code, String block, int nr) {
        this(message, code);
        this.detail = new Detail(block, nr);
    }

    public Error(List<String> message, int code, String block, int nr) {
        this.message = message;
        this.code = code;
        this.detail = new Detail(block, nr);
    }

    public static class Detail {

        private String block;

        private int nr;


        @SuppressWarnings("unused")
        protected Detail() {}

        public Detail(String block, int nr) {
            this.block = block;
            this.nr = nr;
        }

        // generated

        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
        }

        public int getNr() {
            return nr;
        }

        public void setNr(int nr) {
            this.nr = nr;
        }
    }

    // generated

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "Error{" +
                "message=" + message +
                ", code=" + code +
                ", detail=" + detail +
                '}';
    }
}
