package lt.gama.auth.i;

import lt.gama.model.type.enums.Permission;

public interface IAuthService {

	boolean hasPermission(IAuth auth, Permission[] permissions);
}
