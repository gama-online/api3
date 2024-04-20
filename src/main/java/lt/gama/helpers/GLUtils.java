package lt.gama.helpers;

import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.util.Objects;

public final class GLUtils {

    private GLUtils() {}

    public static boolean isNumbersEqual(GLOperationAccount value1, GLOperationAccount value2) {
        return !Validators.isValid(value1) && !Validators.isValid(value2) ||
                Validators.isValid(value1) && Validators.isValid(value2) &&
                        Objects.equals(value1.getNumber(), value2.getNumber());

    }

    public static boolean isNumbersEqual(GLDC value1, GLDC value2) {
        return !Validators.isPartialValid(value1) && !Validators.isPartialValid(value2) ||
                Validators.isPartialValid(value1) && Validators.isPartialValid(value2) &&
                        isNumbersEqual(value1.getDebit(), value2.getDebit()) &&
                        isNumbersEqual(value1.getCredit(), value2.getCredit());
    }

}
