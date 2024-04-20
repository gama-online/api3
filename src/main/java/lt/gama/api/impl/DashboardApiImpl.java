package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.service.DashboardApi;
import org.springframework.web.bind.annotation.RestController;

/**
 * gama-online
 * Created by valdas on 2016-03-20.
 */
@RestController
public class DashboardApiImpl implements DashboardApi {

//    private final AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();

    @Override
    public APIResult<Void> listReleaseNotes() {

//        String bucket = appIdentityService.getDefaultGcsBucketName();
//        String language = request.getLoginToken().getDefaultCompany().getSettings() != null &&
//                request.getLoginToken().getDefaultCompany().getSettings().getLanguage() != null ?
//                request.getLoginToken().getDefaultCompany().getSettings().getLanguage() : "lt";
//        String prefix =  "release/" + language + "/";
//
//        List<String> notes = new ArrayList<>();
//        Storage storage = StorageOptions.defaultInstance().service();
//        Page<Blob> page = storage.list(bucket,
//                Storage.BlobListOption.prefix(prefix),
//                Storage.BlobListOption.fields(Storage.BlobField.NAME, Storage.BlobField.UPDATED));
//        Iterator<Blob> it = page.iterateAll();
//        while (it.hasNext()) {
//            Blob blob = it.next();
//            if (!prefix.equals(blob.name())) notes.add(blob.name());
//        }
//        return APIResult.Data(notes);

        return APIResult.Data();
    }
}
