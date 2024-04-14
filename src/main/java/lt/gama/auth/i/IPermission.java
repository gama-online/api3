package lt.gama.auth.i;

import lt.gama.model.type.enums.Permission;

import java.util.Set;

public interface IPermission {

    Set<String> getPermissions();

    default boolean checkPermission(Permission permission) {
        return getPermissions() != null && getPermissions().contains(permission.toString());
    }
}
