package lt.gama.api.response;

import lt.gama.model.type.GamaMoney;

import java.util.*;

/**
 * Gama
 * Created by valdas on 15-08-07.
 */
public class MoneyAccountRemainderResponse {

    private String name;

    private Set<String> usedCurrencies = new HashSet<>();

    private Map<String, GamaMoney> remainder = new HashMap<>();

    /**
     * Return as array to frontend
     * @return map as array
     */
    public Collection<GamaMoney> getRemainders() {
        return remainder == null ? null : remainder.values();
    }

    // generated


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getUsedCurrencies() {
        return usedCurrencies;
    }

    public void setUsedCurrencies(Set<String> usedCurrencies) {
        this.usedCurrencies = usedCurrencies;
    }

    public Map<String, GamaMoney> getRemainder() {
        return remainder;
    }

    public void setRemainder(Map<String, GamaMoney> remainder) {
        this.remainder = remainder;
    }

    @Override
    public String toString() {
        return "MoneyAccountRemainderResponse{" +
                "name='" + name + '\'' +
                ", usedCurrencies=" + usedCurrencies +
                ", remainder=" + remainder +
                '}';
    }
}
