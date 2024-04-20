package lt.gama.auth.service;

import lt.gama.auth.i.IAuth;
import lt.gama.model.type.enums.Permission;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.service.TranslationService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    public boolean hasPermission(IAuth auth, Permission[] permissions) {
        if (auth == null || !auth.hasValue()) {
            throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.InsufficientPrivileges));
        }
        if (permissions != null && permissions.length > 0) {
            Set<String> requiredPermissions = Arrays.stream(permissions).map(Permission::toString).collect(Collectors.toSet());
            if (!requiredPermissions.isEmpty() && !auth.getPermissions().contains(Permission.ADMIN.toString()) &&
                    Collections.disjoint(auth.getPermissions(), requiredPermissions)) {
                throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.InsufficientPrivileges, auth.getLanguage()));
            }
        }
        return true;
    }
}
