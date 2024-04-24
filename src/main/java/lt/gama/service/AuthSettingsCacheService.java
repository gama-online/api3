package lt.gama.service;

import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.service.repo.CompanyRepository;
import org.springframework.stereotype.Service;


@Service
public class AuthSettingsCacheService {

    private final CompanyRepository companyRepository;
    private final CacheService.ICache<Long, CompanySettings> cache;

    @SuppressWarnings("unchecked")
    public AuthSettingsCacheService(CompanyRepository companyRepository, @SuppressWarnings("rawtypes") CacheService cacheService) {
        this.companyRepository = companyRepository;
        this.cache = cacheService.cache("CS");
    }

    public CompanySettings put(long companyId, CompanySettings companySettings) {
        return cache.put(companyId, companySettings);
    }

    public CompanySettings get(long companyId) {
        if (companyId <= 0) return null;
        CompanySettings companySettings = cache.get(companyId);
        if (companySettings == null) {
            var company = companyRepository.findById(companyId);
            companySettings = company.map(CompanySql::getSettings).orElse(null);
            if (companySettings != null) {
                cache.put(companyId, companySettings);
            }
        }
        return companySettings;
    }

    public void remove(long companyId) {
        this.cache.remove(companyId);
    }

    public void removeAll() {
        this.cache.removeAll();
    }
}
